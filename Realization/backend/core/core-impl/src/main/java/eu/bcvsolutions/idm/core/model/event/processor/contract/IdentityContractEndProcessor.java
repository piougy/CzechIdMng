package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractWorkflowEventProcessor;

/**
 * HR process - end or delete of identity's contract process. The processes is
 * started for contracts that are not valid (meaning validFrom and validTill or
 * disabled by state) and deleted.
 * 
 * @author Radek Tomiška
 *
 */
@Component(IdentityContractEndProcessor.PROCESSOR_NAME)
@Description("HR process - end or delete of identity's contract process. The processes is started"
		+ " for contracts that are not valid (meaning validFrom and validTill or disabled by state) and deleted.")
public class IdentityContractEndProcessor extends AbstractWorkflowEventProcessor<IdmIdentityContractDto>
		implements IdentityContractProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractEndProcessor.class);
	//
	public static final String PROCESSOR_NAME = "identity-contract-end-processor";
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;

	public IdentityContractEndProcessor() {
		super(IdentityContractEventType.UPDATE, IdentityContractEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	protected String getDefaultWorkflowDefinitionKey() {
		// "hrEndContract" can be configured as example, 
		// default business logic is implemented in process method, if no definition is configured
		return null;
	}

	/**
	 * Identity contracts, that was valid and not excluded - is ecluded now
	 */
	@Override
	public boolean conditional(EntityEvent<IdmIdentityContractDto> event) {
		// Skip HR process
		if (this.getBooleanProperty(IdmIdentityContractService.SKIP_HR_PROCESSES, event.getProperties())) {
			return false;
		}
		//
		IdmIdentityContractDto current = event.getContent();
		IdmIdentityContractDto previous = event.getOriginalSource();
		//
		return previous.isValid() && (!current.isValid() || event.hasType(IdentityContractEventType.DELETE));
	}
	
	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		if (!StringUtils.isEmpty(getWorkflowDefinitionKey())) { 
			// wf is configured - execute wf instance
			return super.process(event);
		}
		//
		IdmIdentityContractDto contract = event.getContent();
		OperationResult result = process(
				contract, 
				(Boolean) event.getProperties().get(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION),
				event.getPriority() // propagate event priority
			);
		return new DefaultEventResult.Builder<>(event, this).setResult(result).build();
	}
	
	/**
	 * Check identity state after contract ended
	 * 
	 * @param contract
	 * @param skipRecalculation Skip automatic role recalculation
	 * @return
	 */
	public OperationResult process(IdmIdentityContractDto contract, Boolean skipRecalculation) {
		return process(contract, skipRecalculation, null);
	}
	
	/**
	 * Check identity state after contract ended
	 * 
	 * @param contract
	 * @param skipRecalculation Skip automatic role recalculation
	 * @return
	 */
	private OperationResult process(IdmIdentityContractDto contract, Boolean skipRecalculation, PriorityType priority) {
		// update identity state
		IdmIdentityDto identity = identityService.get(contract.getIdentity());
		IdentityState newState = identityService.evaluateState(identity.getId());
		
		if (identity.getState() != newState) {
			LOG.info("Change identity [{}] state [{}]", identity.getUsername(), newState);
			//
			identity.setState(newState);
			// is necessary publish new event with skip recalculation automatic roles
			IdentityEvent identityEvent = new IdentityEvent(IdentityEventType.UPDATE, identity);
			identityEvent.getProperties().put(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION, skipRecalculation);
			if (priority != null) {
				identityEvent.setPriority(priority);
			}
			identityService.publish(identityEvent);
		}
		//
		// remove all contract roles
		if (!contract.isValidNowOrInFuture()) {
			List<IdmIdentityRoleDto> contractRoles = identityRoleService.findAllByContract(contract.getId());
			List<IdmConceptRoleRequestDto> concepts = new ArrayList<>(contractRoles.size());
			for(IdmIdentityRoleDto identityRole : contractRoles) {
				if (identityRole.getDirectRole() != null) {
					LOG.debug("Sub role will be removed by direct role removal");
					//
					continue;
				}
				if (identityRole.getAutomaticRole() != null) {
					LOG.debug("Automatic role will be removed by role request"
							+ " - automatic roles for invalid contracts are not evaluated.");
				}
				IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
				conceptRoleRequest.setIdentityRole(identityRole.getId());
				conceptRoleRequest.setRole(identityRole.getRole());
				conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
				conceptRoleRequest.setIdentityContract(contract.getId());
				//
				concepts.add(conceptRoleRequest);
			}
			if (!concepts.isEmpty()) {
				IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
				roleRequest.setState(RoleRequestState.CONCEPT);
				roleRequest.setExecuteImmediately(true); // without approval
				roleRequest.setApplicant(contract.getIdentity());
				roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
				roleRequest = roleRequestService.save(roleRequest);
				//
				for (IdmConceptRoleRequestDto concept : concepts) {
					concept.setRoleRequest(roleRequest.getId());
					//
					conceptRoleRequestService.save(concept);
				}
				//
				// start event with skip check authorities
				RoleRequestEvent requestEvent = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest);
				requestEvent.getProperties().put(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, Boolean.TRUE);
				if (priority != null) {
					requestEvent.setPriority(priority);
				}
				// prevent to start asynchronous event before previous update event is completed. 
				requestEvent.setSuperOwnerId(identity.getId());
				//
				roleRequestService.startRequestInternal(requestEvent);
			}
		}
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}

	/**
	 * after save
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 200;
	}

}
