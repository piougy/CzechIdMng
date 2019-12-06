package eu.bcvsolutions.idm.core.model.event.processor.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ConfigurationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.event.ConfigurationEvent.ConfigurationEventType;

/**
 * Deletes configuration property - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 10.0.0
 */
@Component(ConfigurationDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes configuration property - ensures referential integrity.")
public class ConfigurationDeleteProcessor
		extends CoreEventProcessor<IdmConfigurationDto>
		implements ConfigurationProcessor{
	
	public static final String PROCESSOR_NAME = "core-configuration-delete-processor";
	@Autowired private IdmConfigurationService service;
	
	public ConfigurationDeleteProcessor() {
		super(ConfigurationEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmConfigurationDto> process(EntityEvent<IdmConfigurationDto> event) {
		IdmConfigurationDto configuration = event.getContent();
		//		
		service.deleteInternal(configuration);
		//
		return new DefaultEventResult<>(event, this);
	}
}