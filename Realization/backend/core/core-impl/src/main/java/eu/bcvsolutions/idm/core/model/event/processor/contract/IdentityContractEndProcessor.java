package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
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

	public static final String PROCESSOR_NAME = "identity-contract-end-processor";

	public IdentityContractEndProcessor() {
		super(IdentityContractEventType.UPDATE, IdentityContractEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	protected String getDefaultWorkflowDefinitionKey() {
		return "hrEndContract";
	}

	/**
	 * Identity contracts, that was valid and not excluded - is ecluded now
	 */
	@Override
	protected boolean conditional(EntityEvent<IdmIdentityContractDto> event) {
		// Skip HR process
		if (this.getBooleanProperty(IdmIdentityContractService.SKIP_HR_PROCESSES, event.getProperties())) {
			return false;
		}
		
		IdmIdentityContractDto current = event.getContent();
		IdmIdentityContractDto previous = event.getOriginalSource();
		//
		return previous.isValid() && (!current.isValid() || event.hasType(IdentityContractEventType.DELETE));
	}

	/**
	 * after save
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 200;
	}

}
