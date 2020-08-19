package eu.bcvsolutions.idm.acc;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.acc.event.processor.module.AccInitUserRoleProcessor;

/**
 * Initialization ACC module application data.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @deprecated @since 10.5.0 - see {@link AccInitUserRoleProcessor}
 */
@Deprecated
@Component(InitAccApplicationData.NAME)
@DependsOn(InitApplicationData.NAME)
public class InitAccApplicationData implements ApplicationListener<ContextRefreshedEvent> {

	public static final String NAME = "initAccApplicationData";
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}
	
	/**
	 * Initialize data for acc module
	 */
	protected void init() {
		// deprecated
	}
}
