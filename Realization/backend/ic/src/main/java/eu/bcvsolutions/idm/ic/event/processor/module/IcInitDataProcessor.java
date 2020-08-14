package eu.bcvsolutions.idm.ic.event.processor.module;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.logging.impl.JDKLogger;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.ic.IcModuleDescriptor;

/**
 * Initialize ic module - connector logging.
 * 
 * @author Radek Tomiška
 * @author Vít Švanda
 * @since 10.5.0
 */
@Component(IcInitDataProcessor.PROCESSOR_NAME)
@Description("Initialize ic module - connector logging.")
public class IcInitDataProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IcInitDataProcessor.class);
	public static final String PROCESSOR_NAME = "ic-init-data-processor";
	public static final String PROPERTY_CONNID_LOGGER_IMPLEMENTATION = "org.identityconnectors.common.logging.class";
	public static final String PROPERTY_SET_LOGGER = "setSpiClass";
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		LOG.info("Initialization [{}] module...", IcModuleDescriptor.MODULE_ID);

		// ConnId using logger implementation, what cannot be configured (printing
		// always). We need change the implementation to JDKLogger.
		// Second way how configuring that property, is create property file
		// 'connectors.properties' in the java home (jre/lib), witch will contains same
		// property.
		System.setProperty(PROPERTY_CONNID_LOGGER_IMPLEMENTATION, JDKLogger.class.getName());
		
		// VŠ: I had to use this hard code. Because logger in Connid is cached and calls before this initialisation.
		try {
			Method spiClassMethod = Arrays
					.asList(Log.class.getDeclaredMethods())
					.stream()
					.filter(propertyDescriptor -> {
						return PROPERTY_SET_LOGGER.equals(propertyDescriptor.getName());
					})
					.findFirst()
					.orElse(null);
			spiClassMethod.setAccessible(true);
			spiClassMethod.invoke(Log.class, new Object[]{ null });
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new CoreException(e);
		}
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// before role and identity (data) will be created.
		return CoreEvent.DEFAULT_ORDER - 5000;
	}
}
