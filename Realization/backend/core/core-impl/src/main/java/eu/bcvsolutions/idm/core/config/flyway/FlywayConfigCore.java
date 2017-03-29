package eu.bcvsolutions.idm.core.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

import eu.bcvsolutions.idm.core.api.config.flyway.AbstractFlywayConfiguration;
import eu.bcvsolutions.idm.core.api.config.flyway.IdmFlywayAutoConfiguration;
import eu.bcvsolutions.idm.core.api.config.flyway.IdmFlywayPostProcessor;

/**
 * Core flyway config
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "flyway", name = "enabled", matchIfMissing = false)
@AutoConfigureAfter(IdmFlywayAutoConfiguration.IdmFlywayConfiguration.class)
@EnableConfigurationProperties(FlywayProperties.class)
@PropertySource("classpath:/flyway-core.properties")
public class FlywayConfigCore extends AbstractFlywayConfiguration {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlywayConfigCore.class);
	public static final String NAME = "flywayCore";
	
	@Bean
	@ConditionalOnMissingBean(name = NAME)
	@DependsOn(IdmFlywayPostProcessor.NAME)
	@ConditionalOnExpression("${flyway.enabled:true} && '${flyway.core.locations}'!=''")
	@ConfigurationProperties(prefix = "flyway.core")
	public Flyway flywayCore() {
		Flyway flyway = super.createFlyway();		
		log.info("Starting flyway migration for module core [{}]: ", flyway.getTable());
		return flyway;
	}
}