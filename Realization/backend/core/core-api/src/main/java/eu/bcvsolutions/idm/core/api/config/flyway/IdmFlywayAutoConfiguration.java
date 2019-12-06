package eu.bcvsolutions.idm.core.api.config.flyway;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;

/**
 * FlywayAutoConfiguration extension added support for multi modular {@link Flyway} configuration.
 * 
 * @author Radek Tomiška
 */
@SuppressWarnings("deprecation") // third party warning
@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "flyway", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter({ DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class IdmFlywayAutoConfiguration extends FlywayAutoConfiguration {
	
	/**
	 * Support for multi modular {@link Flyway} configuration
	 * 
	 * @author Radek Tomiška
	 */
	@Configuration
	@Import(FlywayJpaDependencyConfiguration.class)
	public static class IdmFlywayConfiguration extends FlywayAutoConfiguration.FlywayConfiguration {

		public IdmFlywayConfiguration(
				FlywayProperties properties, 
				DataSourceProperties dataSourceProperties,
				ResourceLoader resourceLoader, 
				ObjectProvider<DataSource> dataSource,
				@FlywayDataSource ObjectProvider<DataSource> flywayDataSource,
				ObjectProvider<FlywayMigrationStrategy> migrationStrategy,
				ObjectProvider<FlywayConfigurationCustomizer> fluentConfigurationCustomizers,
				ObjectProvider<Callback> callbacks, 
				ObjectProvider<FlywayCallback> flywayCallbacks) {
			super(addProperty(properties), 
					dataSourceProperties, resourceLoader, 
					dataSource, flywayDataSource, migrationStrategy, fluentConfigurationCustomizers, callbacks, flywayCallbacks);
		}
		
		private static FlywayProperties addProperty(FlywayProperties properties) {
			properties.setCheckLocation(false);
			//
			return properties;
		}
		
		/**
		 * Creates module dependent {@link Flyway} configuration.
         *
		 * @return
		 */
		public Flyway createFlyway() {
			return super.flyway();
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
