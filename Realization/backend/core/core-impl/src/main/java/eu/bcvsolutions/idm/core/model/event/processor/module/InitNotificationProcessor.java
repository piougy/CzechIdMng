package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;

/**
 * Init notification templates and configuration.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitNotificationProcessor.PROCESSOR_NAME)
@Description("Init notification templates from classpath (file system) and notification configuration from module descriptors.")
public class InitNotificationProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-notification-processor";
	//
	@Autowired private IdmNotificationConfigurationService notificationConfigurationService;
	@Autowired private IdmNotificationTemplateService notificationTemplateService;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		//
		// Save only missing templates, current templates is not redeploys.
		notificationTemplateService.init();
		//
		// Init notification configuration, initialization topic - need exists system templates above.
		notificationConfigurationService.initDefaultTopics();
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 5100;
	}
}
