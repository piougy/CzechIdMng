package eu.bcvsolutions.idm.core.config;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
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
		Assert.assertNull(config.getUuid(UUID_PARAMETER));
	}
	
	@Test
	public void testValidUuid() {
		ConfigurationMap config = new ConfigurationMap();
		UUID uuid = UUID.randomUUID();
		config.put(UUID_PARAMETER, uuid);
		//
		Assert.assertEquals(uuid, config.getUuid(UUID_PARAMETER));
	}
	
	@Test
	public void testValidUuidAsString() {
		ConfigurationMap config = new ConfigurationMap();
		UUID uuid = UUID.randomUUID();
		config.put(UUID_PARAMETER, uuid.toString());
		//
		Assert.assertEquals(uuid, config.getUuid(UUID_PARAMETER));
	}
	
	@Test(expected = ClassCastException.class)
	public void testInvalidUuid() {
		ConfigurationMap config = new ConfigurationMap();
		config.put(UUID_PARAMETER, "wrong");
		//
		config.getUuid(UUID_PARAMETER);
	}
	
	@Test
	public void testEquals() {
		ConfigurationMap configOne = new ConfigurationMap();
		configOne.put("one", "valueOne");
		ConfigurationMap configTwo = new ConfigurationMap();
		configTwo.put("one", "valueOne");
		//
		Assert.assertTrue(configOne.equals(configTwo));
	}
	
	@Test
	public void testNotEquals() {
		ConfigurationMap configOne = new ConfigurationMap();
		configOne.put("one", "valueOne");
		ConfigurationMap configTwo = new ConfigurationMap();
		configTwo.put("one", "valueOnea");
		//
		Assert.assertFalse(configOne.equals(configTwo));
	}
	
	@Test
	public void testEqualsEntity() {
		ConfigurationMap configOne = new ConfigurationMap();
		UUID uuid = UUID.randomUUID();
		configOne.put("one", new IdmIdentityDto(uuid));
		ConfigurationMap configTwo = new ConfigurationMap();
		configTwo.put("one", new IdmIdentityDto(uuid));
		//
		Assert.assertTrue(configOne.equals(configTwo));
	}
	
	@Test
	public void testNotEqualsEntity() {
		ConfigurationMap configOne = new ConfigurationMap();
		configOne.put("one", new IdmIdentityDto(UUID.randomUUID()));
		ConfigurationMap configTwo = new ConfigurationMap();
		configTwo.put("one", new IdmIdentityDto(UUID.randomUUID()));
		//
		Assert.assertFalse(configOne.equals(configTwo));
	}
	
	@Test
	public void testEqualsBoolean() {
		ConfigurationMap configOne = new ConfigurationMap();
		configOne.put("one", true);
		ConfigurationMap configTwo = new ConfigurationMap();
		configTwo.put("one", true);
		//
		Assert.assertTrue(configOne.equals(configTwo));
	}
	
	@Test
	public void testNotEqualsBoolean() {
		ConfigurationMap configOne = new ConfigurationMap();
		configOne.put("one", true);
		ConfigurationMap configTwo = new ConfigurationMap();
		configTwo.put("one", false);
		//
		Assert.assertFalse(configOne.equals(configTwo));
	}
	
	@Test
	public void testNotEqualsKeys() {
		ConfigurationMap configOne = new ConfigurationMap();
		configOne.put("one", true);
		ConfigurationMap configTwo = new ConfigurationMap();
		configTwo.put("ones", true);
		//
		Assert.assertFalse(configOne.equals(configTwo));
	}
	
	@Test
	public void testEqualsMoreKeys() {
		ConfigurationMap configOne = new ConfigurationMap();
		configOne.put("two", true);
		configOne.put("one", true);
		ConfigurationMap configTwo = new ConfigurationMap();
		configTwo.put("one", true);
		configTwo.put("two", true);
		//
		Assert.assertTrue(configOne.equals(configTwo));
	}
	
	@Test
	public void testEqualsDeepLists() {
		ConfigurationMap configOne = new ConfigurationMap();
		configOne.put("one", Lists.newArrayList("one", "two"));
		ConfigurationMap configTwo = new ConfigurationMap();
		configTwo.put("one", Lists.newArrayList("one", "two"));
		//
		Assert.assertTrue(configOne.equals(configTwo));
	}
	
	@Test
	public void testNotEqualsDeepListsDifferentOrder() {
		ConfigurationMap configOne = new ConfigurationMap();
		configOne.put("one", Lists.newArrayList("two", "one"));
		ConfigurationMap configTwo = new ConfigurationMap();
		configTwo.put("one", Lists.newArrayList("one", "two"));
		//
		Assert.assertFalse(configOne.equals(configTwo));
	}
	
	@Test
	public void testToMap() {	
		ConfigurationMap config = new ConfigurationMap();
		config.put("one", true);
		//
		Assert.assertEquals(true, config.toMap().get("one"));
	}
}
