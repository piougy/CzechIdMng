package eu.bcvsolutions.idm.vs.config.flyway;

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

/**
 * DB migration for vs module
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "flyway", name = "enabled", matchIfMissing = false)
@AutoConfigureAfter(IdmFlywayAutoConfiguration.IdmFlywayConfiguration.class)
@EnableConfigurationProperties(FlywayProperties.class)
@PropertySource("classpath:/flyway-vs.properties")
public class VirtualSystemFlywayConfig extends AbstractFlywayConfiguration {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualSystemFlywayConfig.class);

	@Bean
	@DependsOn("flywayCore")
	@ConditionalOnMissingBean(name = "flywayModuleVirtualSystem")
	@ConditionalOnExpression("${flyway.enabled:true} && '${flyway.vs.locations}'!=''")
	@ConfigurationProperties(prefix = "flyway.vs")
	public Flyway flywayModuleVirtualSystem() {
		Flyway flyway = super.createFlyway();		
		log.info("Starting flyway migration for vs module [{}]: ", flyway.getTable());
		return flyway;
	}
}
