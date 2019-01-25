package eu.bcvsolutions.idm.acc.provisioning;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test equality of value from IdM and system in provisioning
 * 
 * @author Vít Švanda
 *
 */
@Service
public class ProvisioningValuesEqualityTest extends AbstractIntegrationTest {

	@Autowired
	private ProvisioningService provisioningService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testEqualsLists( ) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setMultivalued(true);
		
		Object idmValue = Lists.newArrayList("A","B");
		Object systemValue = Lists.newArrayList("A", "B");
		// Same value, same order -> true
		assertTrue(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
		
		systemValue = Lists.newArrayList("B", "A");
		// Same value, different order -> true
		assertTrue(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
		
		systemValue = Lists.newArrayList("B", "A", "A");
		// Same value, different order, duplicated value -> false
		assertFalse(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
	}
	
	@Test
	public void testEqualsListsNoMultivalueAttribute( ) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setMultivalued(false);
		
		Object idmValue = Lists.newArrayList("A","B");
		Object systemValue = Lists.newArrayList("A", "B");
		// Same value, same order -> true
		assertTrue(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
		
		systemValue = Lists.newArrayList("B", "A");
		// Same value, different order -> false
		assertFalse(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
		
		systemValue = Lists.newArrayList("B", "A", "A");
		// Same value, different order, duplicated value -> false
		assertFalse(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
	}
	
	/**
	 *  Multivalued values are equals, when value from system is null and value in
	 *	IdM is empty list
	 */
	@Test
	public void testIdmValueIsEmptySystemValueIsNull( ) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setMultivalued(true);
		
		Object idmValue = Lists.newArrayList();
		Object systemValue = null;
		assertTrue(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
	}
	
	/**
	 *  Multivalued values are equals, when value from IdM is null and value in
	 *	system is empty list
	 */
	@Test
	public void testIdmValueIsNullSystemValueIsEmpty( ) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setMultivalued(true);
		
		Object idmValue = null;
		Object systemValue = Lists.newArrayList();
		assertTrue(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
	}
	
	@Test
	public void testNoEqualsLists( ) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setMultivalued(true);
		
		Object idmValue = Lists.newArrayList("A","B");
		Object systemValue = Lists.newArrayList("C", "B");
		// Different values -> false
		assertFalse(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
	}
	
	@Test
	public void testNoEqualsValues( ) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setMultivalued(false);
		
		Object idmValue = 100;
		Object systemValue = "100";
		// Different values -> false
		assertFalse(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
	}
	
}
