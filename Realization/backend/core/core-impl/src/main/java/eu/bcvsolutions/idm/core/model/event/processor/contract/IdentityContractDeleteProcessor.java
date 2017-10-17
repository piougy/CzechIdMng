package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Deletes identity contract - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes identity contract.")
public class IdentityContractDeleteProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {

	public static final String PROCESSOR_NAME = "identity-contract-delete-processor";
	private final IdmIdentityContractService service;
	private final IdmIdentityRoleService identityRoleService;
	private final IdmConceptRoleRequestService conceptRequestService;
	private final IdmRoleRequestService roleRequestService;
	private final IdmContractGuaranteeService contractGuaranteeService;
	
	@Autowired
	public IdentityContractDeleteProcessor(
			IdmIdentityContractService service,
			IdmIdentityRoleService identityRoleService,
			IdmConceptRoleRequestService conceptRequestService,
			IdmRoleRequestService roleRequestService,
			IdmContractGuaranteeService contractGuaranteeService) {
		super(IdentityContractEventType.DELETE);
		//
		Assert.notNull(service);
		Assert.notNull(identityRoleService);
		Assert.notNull(conceptRequestService);
		Assert.notNull(roleRequestService);
		Assert.notNull(contractGuaranteeService);
		//
		this.service = service;
		this.identityRoleService = identityRoleService;
		this.conceptRequestService = conceptRequestService;
		this.roleRequestService = roleRequestService;
		this.contractGuaranteeService = contractGuaranteeService;	
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		//
		// delete referenced roles
		identityRoleService.findAllByContract(contract.getId()).forEach(identityRole -> {
			identityRoleService.delete(identityRole);
		});
		
		// Find all concepts and remove relation on role
		IdmConceptRoleRequestFilter conceptRequestFilter = new IdmConceptRoleRequestFilter();
		conceptRequestFilter.setIdentityContractId(contract.getId());
		conceptRequestService.find(conceptRequestFilter, null).getContent().forEach(concept -> {
			IdmRoleRequestDto request = roleRequestService.get(concept.getRoleRequest());
			String message = null;
			if (concept.getState().isTerminatedState()) {
				message = MessageFormat.format(
						"IdentityContract [{0}] (requested in concept [{1}]) was deleted (not from this role request)!",
						contract.getId(), concept.getId());
			} else {
				message = MessageFormat.format(
						"Request change in concept [{0}], was not executed, because requested IdentityContract [{1}] was deleted (not from this role request)!",
						concept.getId(), contract.getId());
				concept.setState(RoleRequestState.CANCELED);
			}
			roleRequestService.addToLog(request, message);
			conceptRequestService.addToLog(concept, message);
			concept.setIdentityContract(null);

			roleRequestService.save(request);
			conceptRequestService.save(concept);
		});		
		// delete contract guarantees
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setIdentityContractId(contract.getId());
		contractGuaranteeService.find(filter, null).forEach(guarantee -> {
			contractGuaranteeService.delete(guarantee);
		});
		// delete identity contract
		service.deleteInternal(contract);
		//
		return new DefaultEventResult<>(event, this);
	}
}
