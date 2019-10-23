package eu.bcvsolutions.idm.core.model.event.processor.configuration;

import org.apache.commons.lang.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ConfigurationProcessor;
import eu.bcvsolutions.idm.core.api.service.LoggerManager;
import eu.bcvsolutions.idm.core.model.event.ConfigurationEvent.ConfigurationEventType;

/**
 * Restore logger level after configuration property with logger prefix is removed.
 * 
 * @author Radek Tomiška
 * @since 10.0.0
 */
@Component(ConfigurationDeleteLoggerProcessor.PROCESSOR_NAME)
@Description("Restore logger level after configuration property with logger prefix is removed.")
public class ConfigurationDeleteLoggerProcessor
		extends CoreEventProcessor<IdmConfigurationDto>
		implements ConfigurationProcessor{
	
	public static final String PROCESSOR_NAME = "core-configuration-delete-logger-processor";
	//
	@Autowired private LoggerManager manager;
	
	public ConfigurationDeleteLoggerProcessor() {
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
		String packageName = manager.getPackageName(configuration.getName());
		if (StringUtils.isNotEmpty(packageName)) {
			manager.setLevel(packageName, (Level) null);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 1000;
	}
}