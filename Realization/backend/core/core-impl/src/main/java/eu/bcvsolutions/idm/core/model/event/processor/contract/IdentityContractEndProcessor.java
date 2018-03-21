package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractWorkflowEventProcessor;

/**
 * HR process - end or delete of identity's contract process. The processes is
 * started for contracts that are not valid (meaning validFrom and validTill or
 * disabled by state) and deleted.
 * 
 * @author Radek Tomiška
 *
 */
@Component
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
		OperationResult result = process(contract, (Boolean) event.getProperties().get(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION));
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
		// update identity state
		IdmIdentityDto identity = identityService.get(contract.getIdentity());
		IdentityState newState = identityService.evaluateState(identity.getId());
		
		if (identity.getState() != newState) {
			LOG.info("Change identity [{}] state [{}]", identity.getUsername(), newState);
			//
			identity.setState(newState);
			// is neccessary publish new event with skip recalculation automatic roles
			IdentityEvent identityEvent = new IdentityEvent(IdentityEventType.UPDATE, identity);
			identityEvent.getProperties().put(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION, skipRecalculation);
			identityService.publish(identityEvent);
		}
		//
		// remove all contract roles
		// TODO: remove? It's solved by different process
		if(!contract.isValidNowOrInFuture()) {
			identityRoleService.findAllByContract(contract.getId()).forEach(role -> {
				identityRoleService.delete(role);
			});
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
