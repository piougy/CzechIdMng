package eu.bcvsolutions.idm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Create record with uuid generated externally
 * 
 * @author Radek Tomiška
 *
 */
public class PreserveUuidIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private IdmConfigurationService configurationService;
	
	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testCreateConfigurationWithUuid() {
		UUID id = UUID.randomUUID();
		IdmConfigurationDto configuration = new IdmConfigurationDto(id);
		configuration.setName("test-property-one");
		configuration.setValue("one");
		//
		configuration = configurationService.save(configuration);
		//
		assertEquals(id, configuration.getId());		
	}
	
	@Test
	public void testIsNewWithId() {
		UUID id = UUID.randomUUID();
		IdmConfigurationDto configuration = new IdmConfigurationDto(id);
		configuration.setName("test-property-one");
		configuration.setValue("one");
		//
		assertTrue(configurationService.isNew(configuration));
	}
	
	@Test
	public void testIsNewWithoutId() {
		IdmConfigurationDto configuration = new IdmConfigurationDto();
		configuration.setName("test-property-one");
		configuration.setValue("one");
		//
		assertTrue(configurationService.isNew(configuration));
	}
	
	@Test
	public void testIsNotNew() {
		IdmConfigurationDto configuration = new IdmConfigurationDto();
		configuration.setName("test-property-two");
		configuration.setValue("one");
		//
		assertTrue(configurationService.isNew(configuration));
		//
		configuration = configurationService.save(configuration);
		//
		assertFalse(configurationService.isNew(configuration));
		//
		IdmConfigurationDto clone = new IdmConfigurationDto(configuration.getId());
		//
		assertFalse(configurationService.isNew(clone));
	}
}
