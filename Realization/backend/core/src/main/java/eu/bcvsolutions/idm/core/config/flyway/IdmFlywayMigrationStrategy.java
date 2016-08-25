package eu.bcvsolutions.idm.core.config.flyway;

import java.sql.Connection;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.stereotype.Component;

@Component
public class IdmFlywayMigrationStrategy implements FlywayMigrationStrategy {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmFlywayMigrationStrategy.class);
	
	@Override
	public void migrate(Flyway flyway) {
		Connection connection = JdbcUtils.openConnection(flyway.getDataSource());
		DbSupport dbSupport = DbSupportFactory.createDbSupport(connection, false);
		String dbName = dbSupport.getDbName();
		log.info("Flyway resolved [{}] jdbc database", dbName);
		// Arrays.stream(flyway.getLocations()).map(localtion -> return null);
		String[] locations = flyway.getLocations();
		for (int i = 0; i < locations.length; i++) {
			locations[i] = locations[i].replace("${dbName}", dbName);
		}
		flyway.setLocations(locations);	
		//
		flyway.migrate();
	}
}
