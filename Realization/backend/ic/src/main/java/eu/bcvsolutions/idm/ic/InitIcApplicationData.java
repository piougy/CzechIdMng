package eu.bcvsolutions.idm.ic;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.ic.event.processor.module.IcInitDataProcessor;

/**
 * Initialization IC module application data. This component depends on
 * {@link InitApplicationData}
 * 
 * @author svandav
 * @deprecated @since 10.5.0 - use {@link IcInitDataProcessor}
 */
@Deprecated
@Component(InitIcApplicationData.NAME)
@DependsOn(InitApplicationData.NAME)
public class InitIcApplicationData implements ApplicationListener<ContextRefreshedEvent> {

	public static final String NAME = "initIcApplicationData";
	public static final String PROPERTY_CONNID_LOGGER_IMPLEMENTATION = IcInitDataProcessor.PROPERTY_CONNID_LOGGER_IMPLEMENTATION;
	public static final String PROPERTY_SET_LOGGER = IcInitDataProcessor.PROPERTY_SET_LOGGER;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// deprecated
	}

}
