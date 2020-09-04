package eu.bcvsolutions.idm.core.api.config.flyway;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Resolves used jdbc database dynamically - {@value #WILDCARD_DBNAME} in location could be used.
 * 
 * @author Radek Tomiška
 */
@Component
public class IdmFlywayMigrationStrategy implements FlywayMigrationStrategy {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmFlywayMigrationStrategy.class);
	public static final String WILDCARD_DBNAME = "${dbName}";
	public static final String POSTGRESQL_DBNAME = "postgresql";
	public static final String MSSQL_DBNAME = "sqlserver";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void migrate(Flyway flyway) {
		String dbName = resolveDbName(flyway);
		LOG.info("Flyway resolved [{}] jdbc database", dbName);
		Flyway
			.configure()
			.configuration(flyway.getConfiguration())
			.locations(resolveLocations(dbName, flyway.getConfiguration().getLocations()))
			// We wrongly added core 8.00.001 script (to history) to avoid 
			// flyway error (IllegalArgumentException: Comparison method violates its general contract!),
			// this created this new issue => has to be set to true.
			.ignoreIgnoredMigrations(true)
			// When wrong change script is added and replace with new one (delete wrong, add new one with new number),
			// then mission removed change script will be ignored.
			.ignoreMissingMigrations(true)
			.load()
			.migrate();
	}
	
	/**
	 * Resolve dbName, which is used by {@Flyway} datasource.
	 * 
	 * @param flyway
	 * @return
	 */
	public String resolveDbName(Configuration flyway) {
		Connection connection = JdbcUtils.openConnection(flyway.getDataSource(), 1);
		//
		try {
			DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
			//
			String databaseProductName = databaseMetaData.getDatabaseProductName().toLowerCase().replace(" ", "");
			if (databaseProductName.contains(MSSQL_DBNAME)) {
				// product name for mssql was changed since flyway 6 => map product name to our folder name
				databaseProductName = MSSQL_DBNAME;
			}
            return databaseProductName;
        } catch (SQLException ex) {
            throw new FlywaySqlException("Error while determining database product name", ex);
        } finally {
        	JdbcUtils.closeConnection(connection);
        }
	}
	
	/**
	 * Returns location with given dbName
	 * 
	 * @param dbName
	 * @param rawLocations
	 * @return
	 */
	public String[] resolveLocations(final String dbName, Location[] rawLocations) {
		Assert.notEmpty(rawLocations, String.format("Locations are required for database [%s].", dbName));
		//
		return Arrays.stream(rawLocations)
				.map(location -> {
					return location.getPath().replace(WILDCARD_DBNAME, dbName);	
				})
				.toArray(String[]::new);
	}
}
