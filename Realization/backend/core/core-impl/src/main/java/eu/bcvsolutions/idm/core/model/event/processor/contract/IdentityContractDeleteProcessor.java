package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
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
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;

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
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractDeleteProcessor.class);
	//
	@Autowired private IdmIdentityContractService service;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmConceptRoleRequestService conceptRequestService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private IdmContractPositionService contractPositionService;
	@Autowired private IdmContractSliceService contractSliceService;
	@Autowired private IdmDelegationDefinitionService delegationDefinitionService;
	@Autowired private EntityStateManager entityStateManager;
	
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
		UUID contractId = contract.getId();
		Assert.notNull(contractId, "Contract must have a ID!");
		boolean forceDelete = getBooleanProperty(PROPERTY_FORCE_DELETE, event.getProperties());
		//
		// check contract can be deleted - cannot be deleted, when is controlled by slices
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setParentContract(contractId);
		if (contractSliceService.find(sliceFilter, null).getTotalElements() > 0){
			// This contract is controlled by some slice -> cannot be deleted.
			// Cannot be enforced => contract cannot be deleted at all.
			throw new ResultCodeException(
					CoreResultCode.CONTRACT_IS_CONTROLLED_CANNOT_BE_DELETED, 
					ImmutableMap.of("contractId", contractId)
			);
		}
		//
		// Find all concepts and remove relation on contract
		IdmConceptRoleRequestFilter conceptRequestFilter = new IdmConceptRoleRequestFilter();
		conceptRequestFilter.setIdentityContractId(contractId);
		conceptRequestService.find(conceptRequestFilter, null).getContent().forEach(concept -> {
			String message = null;
			if (concept.getState().isTerminatedState()) {
				message = MessageFormat.format(
						"IdentityContract [{0}] (requested in concept [{1}]) was deleted (not from this role request)!",
						contractId, concept.getId());
			} else {
				message = MessageFormat.format(
						"Request change in concept [{0}], was not executed, because requested IdentityContract [{1}] was deleted (not from this role request)!",
						concept.getId(), contractId);
				// Cancel concept and WF
				concept = conceptRequestService.cancel(concept);
			}
			IdmRoleRequestDto request = roleRequestService.get(concept.getRoleRequest());
			roleRequestService.addToLog(request, message);
			conceptRequestService.addToLog(concept, message);
			roleRequestService.save(request);
			conceptRequestService.save(concept);
		});
		//
		// delete referenced roles
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<>();
		identityRoleService.findAllByContract(contractId).forEach(identityRole -> {
			// Sub roles are removed different way (processor on direct identity role),
			// but automatic roles has to be removed in the same request.
			if (identityRole.getDirectRole() == null) {			
				IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
				conceptRoleRequest.setIdentityRole(identityRole.getId());
				conceptRoleRequest.setRole(identityRole.getRole());
				conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
				conceptRoleRequest.setIdentityContract(contractId); // ignore not found
				//
				concepts.add(conceptRoleRequest);
			}
		});
		if (forceDelete) { // ~ async with force
			IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
			roleRequest.setApplicant(contract.getIdentity());
			roleRequest.setConceptRoles(concepts);
			//
			RoleRequestEvent requestEvent = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest);
			requestEvent.setPriority(PriorityType.HIGH);
			//
			roleRequestService.startConcepts(requestEvent, event);
		} else {
			// ~ sync
			roleRequestService.executeConceptsImmediate(contract.getIdentity(), concepts);
		}
		// delete contract guarantees
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setIdentityContractId(contractId);
		contractGuaranteeService.find(filter, null).forEach(guarantee -> {
			contractGuaranteeService.delete(guarantee);
		});
		// delete contract positions
		IdmContractPositionFilter positionFilter = new IdmContractPositionFilter();
		positionFilter.setIdentityContractId(contractId);
		contractPositionService.find(positionFilter, null).forEach(position -> {
			contractPositionService.delete(position);
		});
		//
		// delete all contract's delegations 
		IdmDelegationDefinitionFilter delegationFilter = new IdmDelegationDefinitionFilter();
		delegationFilter.setDelegatorContractId(contractId);
		delegationDefinitionService.find(delegationFilter,  null).forEach(delegation -> {
			delegationDefinitionService.delete(delegation);
		});
		
		// delete identity contract
		if (forceDelete) {
			LOG.debug("Contract [{}] should be deleted by caller after all asynchronus processes are completed.", contractId);
			//
			// dirty flag only - will be processed after asynchronous events ends
			IdmEntityStateDto stateDeleted = new IdmEntityStateDto();
			stateDeleted.setEvent(event.getId());
			stateDeleted.setResult(
					new OperationResultDto.Builder(OperationState.RUNNING)
						.setModel(new DefaultResultModel(CoreResultCode.DELETED))
						.build()
			);
			entityStateManager.saveState(contract, stateDeleted);
			//
			// set disabled
			contract.setState(ContractState.DISABLED);
			service.saveInternal(contract);
		} else {
			service.deleteInternal(contract);
		}
		return new DefaultEventResult<>(event, this);
	}
}
