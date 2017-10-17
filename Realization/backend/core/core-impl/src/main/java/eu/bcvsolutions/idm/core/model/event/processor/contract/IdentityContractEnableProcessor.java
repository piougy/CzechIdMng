package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractWorkflowEventProcessor;

/**
 * HR process - enable identity's contract process. The processes is started
 * for contracts that are both valid (meaning validFrom and validTill and disabled state) and
 * not excluded.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("HR process - enable identity's contract process. The processes is started"
		+ " for contracts that are both valid (meaning validFrom and validTill and disabled state) and"
		+ " not excluded.")
public class IdentityContractEnableProcessor
		extends AbstractWorkflowEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {
	
	public static final String PROCESSOR_NAME = "identity-contract-enable-processor";
	
	public IdentityContractEnableProcessor() {
		super(IdentityContractEventType.CREATE, IdentityContractEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	protected String getDefaultWorkflowDefinitionKey() {
		return "hrEnableContract";
	}
	
	/**
	 * Identity contracts, that was valid and not excluded - is ecluded now
	 */
	@Override
	protected boolean conditional(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto current = event.getContent();
		IdmIdentityContractDto previous = event.getOriginalSource();
		//
		return (previous == null || !previous.isValid() || previous.getState() == ContractState.EXCLUDED) && current.isValid();
	}
	
	/**
	 * after save
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 200;
	}

}
