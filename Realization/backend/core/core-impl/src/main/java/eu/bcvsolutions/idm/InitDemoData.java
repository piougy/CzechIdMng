package eu.bcvsolutions.idm;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitDemoDataProcessor;

/**
 * Initialize demo data for application.
 * 
 * @author Radek Tomiška 
 * @deprecated @since 10.5.0 use {@link InitDemoDataProcessor}
 */
@Deprecated
public class InitDemoData implements ApplicationListener<ContextRefreshedEvent> {

	public static final String PARAMETER_DEMO_DATA_ENABLED = InitDemoDataProcessor.PARAMETER_DEMO_DATA_ENABLED;
	public static final String PARAMETER_DEMO_DATA_CREATED = InitDemoDataProcessor.PARAMETER_DEMO_DATA_CREATED;
	public static final String FORM_ATTRIBUTE_PHONE = InitDemoDataProcessor.FORM_ATTRIBUTE_PHONE;
	public static final String FORM_ATTRIBUTE_WWW = InitDemoDataProcessor.FORM_ATTRIBUTE_WWW;
	public static final String FORM_ATTRIBUTE_UUID = InitDemoDataProcessor.FORM_ATTRIBUTE_UUID;
	public static final String FORM_ATTRIBUTE_PASSWORD = InitDemoDataProcessor.FORM_ATTRIBUTE_PASSWORD;
	public static final String FORM_ATTRIBUTE_DATETIME = InitDemoDataProcessor.FORM_ATTRIBUTE_DATETIME;
	public static final String FORM_ATTRIBUTE_DATE = InitDemoDataProcessor.FORM_ATTRIBUTE_DATE;
	public static final String FORM_ATTRIBUTE_LETTER = InitDemoDataProcessor.FORM_ATTRIBUTE_LETTER;
	/**
	 * @deprecated @since 10.5.0 - use {@link RoleConfiguration#getDefaultRoleCode()}
	 */
	@Deprecated
	public static final String DEFAULT_ROLE_NAME = RoleConfiguration.DEFAULT_DEFAULT_ROLE;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// deprecated
	}
}
