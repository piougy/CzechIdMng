package eu.bcvsolutions.idm.core.api.config.flyway;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * FlywayAutoConfiguration extension added support for multi modular {@link Flyway} configuration.
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "flyway", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter({ DataSourceAutoConfiguration.class,
	HibernateJpaAutoConfiguration.class })
public class IdmFlywayAutoConfiguration extends FlywayAutoConfiguration {
	
	/**
	 * Support for multi modular {@link Flyway} configuration
	 * 
	 * @author Radek Tomiška
	 *
	 */
	@Configuration
	@Import(FlywayJpaDependencyConfiguration.class)
	public static class IdmFlywayConfiguration extends FlywayAutoConfiguration.FlywayConfiguration {

		/**
		 * Creates module dependent {@link Flyway} configuration.
		 * @return
		 */
		public Flyway createFlyway() {
			return super.flyway();
		}
		
		
		/**
		 *  We are using location with dynamic jdbc database name.
		 *  TODO: move database resolving here and enable this check?
		 */
		@Deprecated
		@PostConstruct
		@Override
		public void checkLocationExists() {
		}
		
		/**
		 * Default Flyway configuration is not needed.
		 */
		@Deprecated
		@Override
		public Flyway flyway() {
			return null;
		}
		
		/**
		 * Default Flyway configuration is not needed.
		 */
		@Deprecated
		@Override
		public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
			return null;
		}
	}

}
