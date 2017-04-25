package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.dto.filter.ContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;

/**
 * Deletes identity contract - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes identity contract.")
public class IdentityContractDeleteProcessor extends CoreEventProcessor<IdmIdentityContract> {

	public static final String PROCESSOR_NAME = "identity-contract-delete-processor";
	private final IdmIdentityContractRepository repository;
	private final IdmIdentityRoleService identityRoleService;
	private final IdmConceptRoleRequestService conceptRequestService;
	private final IdmRoleRequestService roleRequestService;
	private final IdmContractGuaranteeService contractGuaranteeService;
	
	@Autowired
	public IdentityContractDeleteProcessor(
			IdmIdentityContractRepository repository,
			IdmIdentityRoleService identityRoleService,
			IdmConceptRoleRequestService conceptRequestService,
			IdmRoleRequestService roleRequestService,
			IdmContractGuaranteeService contractGuaranteeService) {
		super(IdentityContractEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(identityRoleService);
		Assert.notNull(conceptRequestService);
		Assert.notNull(roleRequestService);
		Assert.notNull(contractGuaranteeService);
		//
		this.repository = repository;
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
	public EventResult<IdmIdentityContract> process(EntityEvent<IdmIdentityContract> event) {
		IdmIdentityContract contract = event.getContent();
		//
		// delete referenced roles
		identityRoleService.getRoles(contract).forEach(identityRole -> {
			identityRoleService.delete(identityRole);
		});
		
		// Find all concepts and remove relation on role
		ConceptRoleRequestFilter conceptRequestFilter = new ConceptRoleRequestFilter();
		conceptRequestFilter.setIdentityContractId(contract.getId());
		conceptRequestService.findDto(conceptRequestFilter, null).getContent().forEach(concept -> {
			IdmRoleRequestDto request = roleRequestService.getDto(concept.getRoleRequest());
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
		ContractGuaranteeFilter filter = new ContractGuaranteeFilter();
		filter.setIdentityContractId(contract.getId());
		contractGuaranteeService.findDto(filter, null).forEach(guarantee -> {
			contractGuaranteeService.delete(guarantee);
		});
		// delete identity contract
		repository.delete(contract);
		//
		return new DefaultEventResult<>(event, this);
	}
}
