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

/**
 * Enable IdM module.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(ModuleEnableProcessor.PROCESSOR_NAME)
@Description("Enable  IdM module.")
public class ModuleEnableProcessor 
		extends CoreEventProcessor<ModuleDescriptorDto> 
		implements ModuleProcessor {
	
	public static final String PROCESSOR_NAME = "core-module-enable-processor";
	//
	@Autowired private ModuleService moduleService;
	@Autowired private ConfigurationService configurationService;

	public ModuleEnableProcessor() {
		super(ModuleDescriptorEventType.ENABLE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		ModuleDescriptorDto moduleDescriptor = event.getContent();
		// enable module
		String propertyName = moduleService.getModuleConfigurationProperty(moduleDescriptor.getId(), ConfigurationService.PROPERTY_ENABLED);
		configurationService.setBooleanValue(propertyName, true);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}
}
