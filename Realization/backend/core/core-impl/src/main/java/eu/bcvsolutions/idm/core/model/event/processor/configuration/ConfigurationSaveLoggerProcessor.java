package eu.bcvsolutions.idm.core.model.event.processor.configuration;

import org.apache.commons.lang.StringUtils;
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
 * Set logger level after configuration property with logger prefix is saved.
 * 
 * @author Radek Tomiška
 * @since 10.0.0
 */
@Component(ConfigurationSaveLoggerProcessor.PROCESSOR_NAME)
@Description("Set logger level after configuration property with logger prefix is saved.")
public class ConfigurationSaveLoggerProcessor
		extends CoreEventProcessor<IdmConfigurationDto> 
		implements ConfigurationProcessor {
	
	public static final String PROCESSOR_NAME = "core-configuration-save-logger-processor";
	//
	@Autowired private LoggerManager manager;
	
	public ConfigurationSaveLoggerProcessor() {
		super(ConfigurationEventType.UPDATE, ConfigurationEventType.CREATE);
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
			manager.setLevel(configuration);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 1000;
	}
}
