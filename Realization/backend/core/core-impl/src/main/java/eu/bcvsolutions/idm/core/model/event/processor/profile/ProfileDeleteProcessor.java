package eu.bcvsolutions.idm.core.model.event.processor.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ProfileProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.event.ProfileEvent.ProfileEventType;

/**
 * Deletes profile - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
@Component
@Description("Deletes profile from repository.")
public class ProfileDeleteProcessor
		extends CoreEventProcessor<IdmProfileDto>
		implements ProfileProcessor {
	
	public static final String PROCESSOR_NAME = "core-profile-delete-processor";
	//
	@Autowired private IdmProfileService service;
	@Autowired private AttachmentManager attachmentManager;
	
	public ProfileDeleteProcessor() {
		super(ProfileEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmProfileDto> process(EntityEvent<IdmProfileDto> event) {
		IdmProfileDto profile = event.getContent();
		Assert.notNull(profile.getId(), "Profile id is required!");
		//
		// remove image attachment for this identity
		attachmentManager.deleteAttachments(profile);
		//		
		service.deleteInternal(profile);
		//
		return new DefaultEventResult<>(event, this);
	}
}