package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.ModuleDescriptorEvent.ModuleDescriptorEventType;
import eu.bcvsolutions.idm.core.api.event.processor.ModuleProcessor;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.exception.ModuleNotDisableableException;

/**
 * Disable IdM module.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(ModuleDisableProcessor.PROCESSOR_NAME)
@Description("Disable IdM module.")
public class ModuleDisableProcessor 
		extends CoreEventProcessor<ModuleDescriptorDto> 
		implements ModuleProcessor {
	
	public static final String PROCESSOR_NAME = "core-module-disable-processor";
	//
	@Autowired private ModuleService moduleService;
	@Autowired private ConfigurationService configurationService;

	public ModuleDisableProcessor() {
		super(ModuleDescriptorEventType.DISABLE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		ModuleDescriptorDto moduleDescriptor = event.getContent();
		String moduleId = moduleDescriptor.getId();
		// validation
		if (!moduleDescriptor.isDisableable()) {
			throw new ModuleNotDisableableException(moduleId);
		}
		// disable module
		String propertyName = moduleService.getModuleConfigurationProperty(moduleId, ConfigurationService.PROPERTY_ENABLED);
		configurationService.setBooleanValue(propertyName, false);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}
}
