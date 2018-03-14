package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractPublishEntityChangeProcessor;

/**
 * Publish contract change event
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Publish contract change event.")
public class IdentityContractPublishChangeProcessor 
		extends AbstractPublishEntityChangeProcessor<IdmIdentityContractDto>
        implements IdentityContractProcessor {

	public static final String PROCESSOR_NAME = "identity-contract-publish-change-processor";
	
	public IdentityContractPublishChangeProcessor() {
		super(IdentityContractEventType.CREATE, IdentityContractEventType.UPDATE, CoreEventType.EAV_SAVE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
