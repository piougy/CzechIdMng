package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.api.event.processor.RoleRequestProcessor;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;

/**
 * Publish role request change event. Makes execution of request async.
 * 
 * @author Vít Švanda
 *
 */
@Component(RoleRequestPublishChangeProcessor.PROCESSOR_NAME)
@Description("Publish role request change event. Makes execution of request async.")
public class RoleRequestPublishChangeProcessor 
		extends AbstractPublishEntityChangeProcessor<IdmRoleRequestDto>
		implements RoleRequestProcessor {

	public static final String PROCESSOR_NAME = "role-request-publish-change-processor";
	
	public RoleRequestPublishChangeProcessor() {
		super(RoleRequestEventType.EXCECUTE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	protected EntityEvent<IdmRoleRequestDto> setAdditionalEventProperties(EntityEvent<IdmRoleRequestDto> event) {
		event = super.setAdditionalEventProperties(event);
		// we need to set super entity owner - identity contracts should not be processed concurrently for given identity
		event.setSuperOwnerId(event.getContent().getApplicant());
		//
		return event;
	}
}
