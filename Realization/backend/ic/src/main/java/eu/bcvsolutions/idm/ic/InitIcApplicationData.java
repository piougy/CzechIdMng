package eu.bcvsolutions.idm.ic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.logging.impl.JDKLogger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.core.api.exception.CoreException;

/**
 * Initialization IC module application data. This component depends on
 * {@link InitApplicationData}
 * 
 * @author svandav
 *
 */

@Component(InitIcApplicationData.NAME)
@DependsOn(InitApplicationData.NAME)
public class InitIcApplicationData implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitIcApplicationData.class);
	public static final String NAME = "initIcApplicationData";
	public static final String PROPERTY_CONNID_LOGGER_IMPLEMENTATION = "org.identityconnectors.common.logging.class";
	public static final String PROPERTY_SET_LOGGER = "setSpiClass";

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		LOG.info("Initialization [{}] module...", IcModuleDescriptor.MODULE_ID);

		// ConnId using logger implementation, what cannot be configured (printing
		// always). We need change the implementation to JDKLogger.
		// Second way how configuring that property, is create property file
		// 'connectors.properties' in the java home (jre/lib), witch will contains same
		// property.
		System.setProperty(PROPERTY_CONNID_LOGGER_IMPLEMENTATION, JDKLogger.class.getName());
		
		// VŠ: I had to use this hard code. Because logger in Connid is cached and calls before this initialisation.
		try {
			Method spiClassMethod = Arrays.asList(Log.class.getDeclaredMethods()).stream().filter(propertyDescriptor -> {
				return PROPERTY_SET_LOGGER.equals(propertyDescriptor.getName());
			}).findFirst().orElse(null);
			spiClassMethod.setAccessible(true);
			spiClassMethod.invoke(Log.class, new Object[]{ null });
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new CoreException(e);
		} 
	}

}
