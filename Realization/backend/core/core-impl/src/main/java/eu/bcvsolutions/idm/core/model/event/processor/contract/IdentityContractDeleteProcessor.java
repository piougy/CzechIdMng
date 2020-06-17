package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationDefinitionFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
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
@Component(IdentityContractDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes identity contract.")
public class IdentityContractDeleteProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {

	public static final String PROCESSOR_NAME = "identity-contract-delete-processor";
	//
	@Autowired private IdmIdentityContractService service;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmConceptRoleRequestService conceptRequestService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private IdmContractPositionService contractPositionService;
	@Autowired private IdmContractSliceService contractSliceService;
	@Autowired private IdmDelegationDefinitionService delegationDefinitionService;
	
	public IdentityContractDeleteProcessor() {
		super(IdentityContractEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		Assert.notNull(contract.getId(), "Contract must have a ID!");
		//
		// delete referenced roles
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<>();
		identityRoleService.findAllByContract(contract.getId()).forEach(identityRole -> {
			if (identityRole.getDirectRole() == null) { // sub roles are removed different way (processor on direct identity role)				
				IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
				conceptRoleRequest.setIdentityRole(identityRole.getId());
				conceptRoleRequest.setRole(identityRole.getRole());
				conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
				conceptRoleRequest.setIdentityContract(contract.getId());
				//
				concepts.add(conceptRoleRequest);
			}
		});
		roleRequestService.executeConceptsImmediate(contract.getIdentity(), concepts);
		//
		// Find all concepts and remove relation on role
		IdmConceptRoleRequestFilter conceptRequestFilter = new IdmConceptRoleRequestFilter();
		conceptRequestFilter.setIdentityContractId(contract.getId());
		conceptRequestService.find(conceptRequestFilter, null).getContent().forEach(concept -> {
			String message = null;
			if (concept.getState().isTerminatedState()) {
				message = MessageFormat.format(
						"IdentityContract [{0}] (requested in concept [{1}]) was deleted (not from this role request)!",
						contract.getId(), concept.getId());
			} else {
				message = MessageFormat.format(
						"Request change in concept [{0}], was not executed, because requested IdentityContract [{1}] was deleted (not from this role request)!",
						concept.getId(), contract.getId());
				// Cancel concept and WF
				concept = conceptRequestService.cancel(concept);
			}
			IdmRoleRequestDto request = roleRequestService.get(concept.getRoleRequest());
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
		// delete contract positions
		IdmContractPositionFilter positionFilter = new IdmContractPositionFilter();
		positionFilter.setIdentityContractId(contract.getId());
		contractPositionService.find(positionFilter, null).forEach(position -> {
			contractPositionService.delete(position);
		});
		// delete relation (from slices) on the contract
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setParentContract(contract.getId());
		if(contractSliceService.find(sliceFilter, null).getTotalElements() > 0){
			// This contract is controlled by some slice -> cannot be deleted
			throw new ResultCodeException(CoreResultCode.CONTRACT_IS_CONTROLLED_CANNOT_BE_DELETED, ImmutableMap.of("contractId", contract.getId()));
		}
		//
		// delete all contract's delegations 
		IdmDelegationDefinitionFilter delegationFilter = new IdmDelegationDefinitionFilter();
		delegationFilter.setDelegatorContractId(contract.getId());
		delegationDefinitionService.find(delegationFilter,  null).forEach(delegation -> {
			delegationDefinitionService.delete(delegation);
		});
		
		// delete identity contract
		service.deleteInternal(contract);
		return new DefaultEventResult<>(event, this);
	}
}
