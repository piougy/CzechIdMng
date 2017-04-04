package eu.bcvsolutions.idm.core.config;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for added ConfigurationMap methods 
 * 
 * @author Radek Tomiška
 *
 */
public class ConfigurationMapUnitTest extends AbstractUnitTest {

	private static final String UUID_PARAMETER = "UUID";
	
	@Test
	public void testEmptyUuid() {
		ConfigurationMap config = new ConfigurationMap();
		//
		assertNull(config.getUuid(UUID_PARAMETER));
	}
	
	@Test
	public void testValidUuid() {
		ConfigurationMap config = new ConfigurationMap();
		UUID uuid = UUID.randomUUID();
		config.put(UUID_PARAMETER, uuid);
		//
		assertEquals(uuid, config.getUuid(UUID_PARAMETER));
	}
	
	@Test
	public void testValidUuidAsString() {
		ConfigurationMap config = new ConfigurationMap();
		UUID uuid = UUID.randomUUID();
		config.put(UUID_PARAMETER, uuid.toString());
		//
		assertEquals(uuid, config.getUuid(UUID_PARAMETER));
	}
	
	@Test(expected = ClassCastException.class)
	public void testInvalidUuid() {
		ConfigurationMap config = new ConfigurationMap();
		config.put(UUID_PARAMETER, "wrong");
		//
		config.getUuid(UUID_PARAMETER);
	}
}
