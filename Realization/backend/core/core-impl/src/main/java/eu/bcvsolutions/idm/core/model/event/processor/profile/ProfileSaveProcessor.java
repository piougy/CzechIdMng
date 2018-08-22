package eu.bcvsolutions.idm.core.model.event.processor.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ProfileProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.model.event.ProfileEvent.ProfileEventType;

/**
 * Persists profiles
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
@Component
@Description("Persists tokens.")
public class ProfileSaveProcessor
		extends CoreEventProcessor<IdmProfileDto> 
		implements ProfileProcessor {
	
	public static final String PROCESSOR_NAME = "core-profile-save-processor";
	//
	@Autowired private IdmProfileService service;
	
	public ProfileSaveProcessor() {
		super(ProfileEventType.UPDATE, ProfileEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmProfileDto> process(EntityEvent<IdmProfileDto> event) {
		IdmProfileDto profile = event.getContent();
		profile = service.saveInternal(profile);
		event.setContent(profile);
		//
		return new DefaultEventResult<>(event, this);
	}
}
