package eu.bcvsolutions.idm.core.api.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Module dependent {@link Flyway} configuration.
 * 
 * @author Radek Tomiška
 */
public abstract class AbstractFlywayConfiguration {

	@Autowired
	IdmFlywayAutoConfiguration.IdmFlywayConfiguration flywayConfiguration;

	/**
	 * Returns {@link Flyway} configured to specific module
	 * 
	 * @return
	 */
	public Flyway createFlyway() {
		return flywayConfiguration.createFlyway();
	}
}