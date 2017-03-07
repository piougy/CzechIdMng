package eu.bcvsolutions.idm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
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
	
	@Test
	public void testCreateConfigurationWithUuid() {
		UUID id = UUID.randomUUID();
		IdmConfiguration configuration = new IdmConfiguration(id);
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
		IdmConfiguration configuration = new IdmConfiguration(id);
		configuration.setName("test-property-one");
		configuration.setValue("one");
		//
		assertTrue(configurationService.isNew(configuration));
	}
	
	@Test
	public void testIsNewWithoutId() {
		IdmConfiguration configuration = new IdmConfiguration();
		configuration.setName("test-property-one");
		configuration.setValue("one");
		//
		assertTrue(configurationService.isNew(configuration));
	}
	
	@Test
	public void testIsNotNew() {
		IdmConfiguration configuration = new IdmConfiguration();
		configuration.setName("test-property-two");
		configuration.setValue("one");
		//
		assertTrue(configurationService.isNew(configuration));
		//
		configuration = configurationService.save(configuration);
		//
		assertFalse(configurationService.isNew(configuration));
		//
		IdmConfiguration clone = new IdmConfiguration(configuration.getId());
		//
		assertFalse(configurationService.isNew(clone));
	}
}
