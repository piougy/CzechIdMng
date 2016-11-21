package eu.bcvsolutions.idm.icf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration for ICF module
 * @author svandav
 *
 */
@Configuration
@PropertySource("classpath:/module-icf.properties")
public class ConfigIcf {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigIcf.class);
}