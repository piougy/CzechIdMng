package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractPublishEntityChangeProcessor;

/**
 * Publish identity change event
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Publish identity change event.")
public class IdentityPublishChangeProcessor 
		extends AbstractPublishEntityChangeProcessor<IdmIdentityDto>
        implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-publish-change-processor";
	
	public IdentityPublishChangeProcessor() {
		super(IdentityEventType.CREATE, IdentityEventType.UPDATE, CoreEventType.EAV_SAVE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
