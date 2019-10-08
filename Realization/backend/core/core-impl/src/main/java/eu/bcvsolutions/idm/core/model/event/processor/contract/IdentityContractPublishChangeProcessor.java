package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

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
	
	@Override
	protected EntityEvent<IdmIdentityContractDto> setAdditionalEventProperties(EntityEvent<IdmIdentityContractDto> event) {
		event = super.setAdditionalEventProperties(event);
		// we need to set super entity owner - identity contracts should not be processed concurrently for given identity
		event.setSuperOwnerId(event.getContent().getIdentity());
		//
		return event;
	}
}
