package eu.bcvsolutions.idm.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import eu.bcvsolutions.idm.core.api.config.flyway.IdmFlywayMigrationStrategy;
import eu.bcvsolutions.idm.core.test.AbstractUnitTest;

/**
 * Resolving location by jdbc database name
 * 
 * @author Radek Tomiška
 *
 */
public class IdmFlywayMigrationStrategyTest extends AbstractUnitTest {

	private IdmFlywayMigrationStrategy flywayMigrationStrategy = new IdmFlywayMigrationStrategy();
	
	/**
	 * Resolving location by jdbc database name
	 */
	@Test
	public void resolveLocationsTest() {
		String dbName = "oracle";
		String locationPrefixOne = "eu/bcvsolutions/core/";
		String locationPrefixTwo = "eu/bcvsolutions/module/";
		String[] rawLocations = new String[]{locationPrefixOne + IdmFlywayMigrationStrategy.WILDCARD_DBNAME, locationPrefixTwo + IdmFlywayMigrationStrategy.WILDCARD_DBNAME}; 
		List<String> locations = Arrays.asList(flywayMigrationStrategy.resolveLocations(dbName, rawLocations));
		
		assertEquals(2, locations.size());
		assertTrue(locations.contains(locationPrefixOne + dbName));
		assertTrue(locations.contains(locationPrefixTwo + dbName));
	}
}
