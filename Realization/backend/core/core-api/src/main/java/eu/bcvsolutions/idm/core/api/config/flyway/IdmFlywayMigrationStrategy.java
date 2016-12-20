package eu.bcvsolutions.idm.core.api.config.flyway;

import java.sql.Connection;
import java.util.Arrays;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Resolves used jdbc database dynamically - {@value #WILDCARD_DBNAME} in location could be used.
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class IdmFlywayMigrationStrategy implements FlywayMigrationStrategy {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmFlywayMigrationStrategy.class);
	public static final String WILDCARD_DBNAME = "${dbName}";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void migrate(Flyway flyway) {
		String dbName = resolveDbName(flyway);
		log.info("Flyway resolved [{}] jdbc database", dbName);
		flyway.setLocations(resolveLocations(dbName, flyway.getLocations()));	
		//
		flyway.migrate();
	}
	
	/**
	 * Resolve dbName, which is used by {@Flyway} datasource.
	 * 
	 * @param flyway
	 * @return
	 */
	public String resolveDbName(FlywayConfiguration flyway) {
		Connection connection = JdbcUtils.openConnection(flyway.getDataSource());
		DbSupport dbSupport = DbSupportFactory.createDbSupport(connection, false);
		return dbSupport.getDbName();
	}
	
	/**
	 * Returns location with given dbName
	 * 
	 * @param dbName
	 * @param rawLocations
	 * @return
	 */
	public String[] resolveLocations(final String dbName, String[] rawLocations) {
		Assert.notEmpty(rawLocations);
		//
		return Arrays.stream(rawLocations)
				.map(location -> {
					return location.replace(WILDCARD_DBNAME, dbName);	
					})
				.toArray(String[]::new);
	}
}
