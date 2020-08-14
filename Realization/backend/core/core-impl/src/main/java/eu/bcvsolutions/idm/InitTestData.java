package eu.bcvsolutions.idm;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import eu.bcvsolutions.idm.core.model.event.processor.module.InitTestDataProcessor;

/**
 * Initialize demo data for application
 * 
 * @author Radek Tomiška 
 * @deprecated @since 10.5.0 use {@link InitTestDataProcessor}
 */
@Deprecated
public class InitTestData implements ApplicationListener<ContextRefreshedEvent> {
	
	public static final String TEST_ADMIN_USERNAME = InitTestDataProcessor.TEST_ADMIN_USERNAME;
	public static final String TEST_ADMIN_PASSWORD = InitTestDataProcessor.TEST_ADMIN_PASSWORD;
	public static final String TEST_USER_1 = InitTestDataProcessor.TEST_USER_1;
	public static final String TEST_USER_2 = InitTestDataProcessor.TEST_USER_2;
	public static final String TEST_ADMIN_ROLE = InitApplicationData.ADMIN_ROLE;
	public static final String TEST_USER_ROLE = InitTestDataProcessor.TEST_USER_ROLE;
	public static final String TEST_CUSTOM_ROLE = InitTestDataProcessor.TEST_CUSTOM_ROLE;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// deprecated - see  {@link InitTestDataProcessor}
	}
}
