package eu.bcvsolutions.idm.acc.provisioning;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultProvisioningService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test equality of value from IdM and system in provisioning
 * 
 * @author Vít Švanda
 *
 */
public class ProvisioningValuesEqualityTest extends AbstractUnitTest {

	@Mock private SysSystemEntityService systemEntityService;
	//
	private DefaultProvisioningService provisioningService;
	
	@Before
	public void init() {
		provisioningService = new DefaultProvisioningService(Lists.newArrayList(), systemEntityService);
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
	
	@Test
	public void testEqualsArrays( ) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setClassType(byte[].class.getName());
		
		Object idmValue = "ABC".getBytes();
		Object systemValue ="ABC".getBytes();
		
		// Same arrays -> true
		assertTrue(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
	}
	
	@Test
	public void testNoEqualsArrays( ) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setClassType(byte[].class.getName());
		
		Object idmValue = "ABCD".getBytes();
		Object systemValue ="ABC".getBytes();
		
		// Not same arrays -> false
		assertFalse(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testHashMapEquals( ) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setMultivalued(true);
		
		Map<String, String> valueOne = new HashMap<>();
		valueOne.put("one", "one");
		valueOne.put("two", "two");
		valueOne.put("three", "three");
		
		Map<String, String> valueTwo = new HashMap<>();
		valueTwo.put("one", "one");
		valueTwo.put("two", "two");
		valueTwo.put("three", "three");
		
		Map<String, String> valueThree = new HashMap<>();
		valueThree.put("one", "one");
		valueThree.put("two", "two");
		valueThree.put("three", "three");
		
		Map<String, String> valueFour = new HashMap<>();
		valueFour.put("one", "one");
		valueFour.put("two", "two");
		valueFour.put("three", new String("three"));
		
		Object idmValue = Lists.newArrayList(valueOne, valueTwo);
		Object systemValue = Lists.newArrayList(valueThree, valueFour);
		
		// Same keys ane values in map -> true
		assertTrue(provisioningService.isAttributeValueEquals(idmValue, systemValue, schemaAttribute));
	}
	
}
