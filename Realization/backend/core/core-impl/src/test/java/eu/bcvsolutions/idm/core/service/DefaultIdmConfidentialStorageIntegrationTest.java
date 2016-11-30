package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmConfidentialStorageValueRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmConfidentialStorage;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for "naive" confidential storage (values are persisted in standard database)
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmConfidentialStorageIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private IdmConfidentialStorageValueRepository repository;
	
	@Autowired
	private IdmIdentityService identityService;
	
	private DefaultIdmConfidentialStorage confidentalStorage;
	
	private static final String STORAGE_KEY_ONE = "test_key_one";
	private static final String STORAGE_KEY_TWO = "test_key_two";
	
	@Before
	public void initStorage() {
		confidentalStorage = new DefaultIdmConfidentialStorage(repository);
	}
	
	@After
	public void clearData() {
		repository.deleteByKey(STORAGE_KEY_ONE);
		repository.deleteByKey(STORAGE_KEY_TWO);
	}
	
	@Test
	public void testLoadUnexistedValue() {
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);		
		
		Serializable storageValue = confidentalStorage.get(identity, STORAGE_KEY_ONE);	
		
		assertNull(storageValue);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSaveValueNoOwner() {		
		confidentalStorage.get(null, STORAGE_KEY_ONE);	
	}
	
	@Test
	public void testSaveValue() {
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity, STORAGE_KEY_ONE, value);
		Serializable storageValue = confidentalStorage.get(identity, STORAGE_KEY_ONE);
		
		assertEquals(value, storageValue);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSaveValues() {
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		
		ArrayList<String> values = Lists.newArrayList("one", "two", "three");
		confidentalStorage.save(identity, STORAGE_KEY_ONE, values);		
		Serializable storageValue = confidentalStorage.get(identity, STORAGE_KEY_ONE);	
		
		assertEquals(values.getClass(), storageValue.getClass());
		assertArrayEquals(values.toArray(new String[]{}), ((List<String>) storageValue).toArray(new String[]{}));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSaveMoreKeys() {
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity, STORAGE_KEY_ONE, value);		
		ArrayList<String> values = Lists.newArrayList("one", "two", "three");
		confidentalStorage.save(identity, STORAGE_KEY_TWO, values);		
		
		Serializable storageValueOne = confidentalStorage.get(identity, STORAGE_KEY_ONE);
		Serializable storageValueTwo = confidentalStorage.get(identity, STORAGE_KEY_TWO);
		
		assertEquals(value, storageValueOne);
			
		assertEquals(values.getClass(), storageValueTwo.getClass());
		assertArrayEquals(values.toArray(new String[]{}), ((List<String>) storageValueTwo).toArray(new String[]{}));
	}
	
	@Test
	public void testEditSavedValues() {	
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity, STORAGE_KEY_ONE, value);
		
		assertEquals(value, confidentalStorage.get(identity, STORAGE_KEY_ONE));
		
		value = "one_update";
		confidentalStorage.save(identity, STORAGE_KEY_ONE, value);
		
		assertEquals(value, confidentalStorage.get(identity, STORAGE_KEY_ONE));
	}
	
	@Test
	public void testSaveValueDifferentOwner() {	
		IdmIdentity identityOne = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentity identityTwo = identityService.getByUsername(InitTestData.TEST_USER_2);
		
		String valueOne = "one";
		confidentalStorage.save(identityOne, STORAGE_KEY_ONE, valueOne);		
		String valueTwo = "two";
		confidentalStorage.save(identityTwo, STORAGE_KEY_ONE, valueTwo);
		
		assertEquals(valueOne, confidentalStorage.get(identityOne, STORAGE_KEY_ONE));
		assertEquals(valueTwo, confidentalStorage.get(identityTwo, STORAGE_KEY_ONE));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testOverrideSavedValues() {	
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity, STORAGE_KEY_ONE, value);

		assertEquals(value, confidentalStorage.get(identity, STORAGE_KEY_ONE));
		
		ArrayList<String> values = Lists.newArrayList("one", "two", "three");
		confidentalStorage.save(identity, STORAGE_KEY_ONE, values);	
		
		Serializable storageValue = confidentalStorage.get(identity, STORAGE_KEY_ONE);
		assertEquals(values.getClass(), storageValue.getClass());
		assertArrayEquals(values.toArray(new String[]{}), ((List<String>) storageValue).toArray(new String[]{}));
	}
	
	@Test
	public void testDeleteSavedValues() {
		IdmIdentity identityOne = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentity identityTwo = identityService.getByUsername(InitTestData.TEST_USER_2);
		
		String valueOne = "one";
		confidentalStorage.save(identityOne, STORAGE_KEY_ONE, valueOne);		
		String valueTwo = "two";
		confidentalStorage.save(identityTwo, STORAGE_KEY_ONE, valueTwo);
		
		assertEquals(valueOne, confidentalStorage.get(identityOne, STORAGE_KEY_ONE));
		assertEquals(valueTwo, confidentalStorage.get(identityTwo, STORAGE_KEY_ONE));
		
		confidentalStorage.delete(identityOne, STORAGE_KEY_ONE);
		
		assertNull(confidentalStorage.get(identityOne, STORAGE_KEY_ONE));
		assertEquals(valueTwo, confidentalStorage.get(identityTwo, STORAGE_KEY_ONE));
		
		confidentalStorage.delete(identityTwo, STORAGE_KEY_ONE);
		
		assertNull(confidentalStorage.get(identityTwo, STORAGE_KEY_ONE));
	}
	
	@Test
	public void testReadWithType() {
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity, STORAGE_KEY_ONE, value);

		String readValue = confidentalStorage.get(identity, STORAGE_KEY_ONE, String.class);
		
		assertEquals(value, readValue);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testReadWrongType() {
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity, STORAGE_KEY_ONE, value);

		confidentalStorage.get(identity, STORAGE_KEY_ONE, Integer.class);
	}
	
	@Test
	public void testReadWrongTypeWithDefaultValue() {
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity, STORAGE_KEY_ONE, value);

		Integer defaultValue = 10;
		Integer readValue = confidentalStorage.get(identity, STORAGE_KEY_ONE, Integer.class, defaultValue);
		
		assertEquals(defaultValue, readValue);
	}
	
	@Test
	public void testLoadUnexistedValueWithDefault() {
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);

		assertNull(confidentalStorage.get(identity, STORAGE_KEY_ONE, Integer.class));
		
		Integer defaultValue = 10;
		Integer readValue = confidentalStorage.get(identity, STORAGE_KEY_ONE, Integer.class, defaultValue);
		
		assertEquals(defaultValue, readValue);
	}
	
	@Test
	public void testReadGuardedString() {
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		
		String password = "heslo";
		confidentalStorage.save(identity, STORAGE_KEY_ONE, new GuardedString(password).asString());
		
		GuardedString savedPassword = confidentalStorage.getGuardedString(identity, STORAGE_KEY_ONE);
		
		assertEquals(password, savedPassword.asString());
	}
	
	@Test
	public void testSaveAndReadGuardedString() {
		IdmIdentity identity = identityService.getByUsername(InitTestData.TEST_USER_2);
		
		String password = "heslo_save";
		confidentalStorage.saveGuardedString(identity, STORAGE_KEY_ONE, new GuardedString(password));
		
		GuardedString savedPassword = confidentalStorage.getGuardedString(identity, STORAGE_KEY_ONE);
		
		assertEquals(password, savedPassword.asString());
	}
}
