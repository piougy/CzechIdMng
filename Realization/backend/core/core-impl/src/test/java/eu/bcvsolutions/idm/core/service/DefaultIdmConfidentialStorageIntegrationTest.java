package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmConfidentialStorageValueRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmConfidentialStorage;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.CryptService;
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
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private CryptService cryptService;
	
	private DefaultIdmConfidentialStorage confidentalStorage;
	
	private static final String STORAGE_KEY_ONE = "test_key_one";
	private static final String STORAGE_KEY_TWO = "test_key_two";
	
	@Before
	public void initStorage() {
		confidentalStorage = new DefaultIdmConfidentialStorage(repository, cryptService);
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}
	
	@After
	public void clearData() {
		repository.deleteByKey(STORAGE_KEY_ONE);
		repository.deleteByKey(STORAGE_KEY_TWO);
		super.logout();
	}
	
	@Test
	public void testLoadUnexistedValue() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);		
		
		Serializable storageValue = confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_ONE);	
		
		assertNull(storageValue);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSaveValueNoOwner() {		
		confidentalStorage.get(null, null, STORAGE_KEY_ONE);	
	}
	
	@Test
	public void testSaveValue() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), identity.getClass(), STORAGE_KEY_ONE, value);
		Serializable storageValue = confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_ONE);
		
		assertEquals(value, storageValue);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSaveValues() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		ArrayList<String> values = Lists.newArrayList("one", "two", "three");
		confidentalStorage.save(identity.getId(), identity.getClass(), STORAGE_KEY_ONE, values);		
		Serializable storageValue = confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_ONE);	
		
		assertEquals(values.getClass(), storageValue.getClass());
		assertArrayEquals(values.toArray(new String[]{}), ((List<String>) storageValue).toArray(new String[]{}));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSaveMoreKeys() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), identity.getClass(), STORAGE_KEY_ONE, value);		
		ArrayList<String> values = Lists.newArrayList("one", "two", "three");
		confidentalStorage.save(identity.getId(), identity.getClass(), STORAGE_KEY_TWO, values);		
		
		Serializable storageValueOne = confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_ONE);
		Serializable storageValueTwo = confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_TWO);
		
		assertEquals(value, storageValueOne);
			
		assertEquals(values.getClass(), storageValueTwo.getClass());
		assertArrayEquals(values.toArray(new String[]{}), ((List<String>) storageValueTwo).toArray(new String[]{}));
	}
	
	@Test
	public void testEditSavedValues() {	
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), identity.getClass(), STORAGE_KEY_ONE, value);
		
		assertEquals(value, confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_ONE));
		
		value = "one_update";
		confidentalStorage.save(identity.getId(), identity.getClass(), STORAGE_KEY_ONE, value);
		
		assertEquals(value, confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_ONE));
	}
	
	@Test
	public void testSaveValueDifferentOwner() {	
		IdmIdentity identityOne = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		IdmIdentity identityTwo = identityRepository.findOneByUsername(InitTestData.TEST_USER_2);
		
		String valueOne = "one";
		confidentalStorage.save(identityOne.getId(), identityOne.getClass(), STORAGE_KEY_ONE, valueOne);		
		String valueTwo = "two";
		confidentalStorage.save(identityTwo.getId(), identityOne.getClass(), STORAGE_KEY_ONE, valueTwo);
		
		assertEquals(valueOne, confidentalStorage.get(identityOne.getId(), identityOne.getClass(), STORAGE_KEY_ONE));
		assertEquals(valueTwo, confidentalStorage.get(identityTwo.getId(), identityTwo.getClass(), STORAGE_KEY_ONE));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testOverrideSavedValues() {	
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), identity.getClass(), STORAGE_KEY_ONE, value);

		assertEquals(value, confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_ONE));
		
		ArrayList<String> values = Lists.newArrayList("one", "two", "three");
		confidentalStorage.save(identity.getId(), identity.getClass(), STORAGE_KEY_ONE, values);	
		
		Serializable storageValue = confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_ONE);
		assertEquals(values.getClass(), storageValue.getClass());
		assertArrayEquals(values.toArray(new String[]{}), ((List<String>) storageValue).toArray(new String[]{}));
	}
	
	@Test
	public void testDeleteSavedValues() {
		IdmIdentity identityOne = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		IdmIdentity identityTwo = identityRepository.findOneByUsername(InitTestData.TEST_USER_2);
		
		String valueOne = "one";
		confidentalStorage.save(identityOne.getId(), IdmIdentity.class, STORAGE_KEY_ONE, valueOne);		
		String valueTwo = "two";
		confidentalStorage.save(identityTwo.getId(), IdmIdentity.class, STORAGE_KEY_ONE, valueTwo);
		
		assertEquals(valueOne, confidentalStorage.get(identityOne.getId(), IdmIdentity.class, STORAGE_KEY_ONE));
		assertEquals(valueTwo, confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, STORAGE_KEY_ONE));
		
		confidentalStorage.delete(identityOne.getId(), IdmIdentity.class, STORAGE_KEY_ONE);
		
		assertNull(confidentalStorage.get(identityOne.getId(), IdmIdentity.class, STORAGE_KEY_ONE));
		assertEquals(valueTwo, confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, STORAGE_KEY_ONE));
		
		confidentalStorage.delete(identityTwo.getId(), IdmIdentity.class, STORAGE_KEY_ONE);
		
		assertNull(confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, STORAGE_KEY_ONE));
	}
	
	@Test
	public void testReadWithType() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, value);

		String readValue = confidentalStorage.get(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, String.class);
		
		assertEquals(value, readValue);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testReadWrongType() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, value);

		confidentalStorage.get(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, Integer.class);
	}
	
	@Test
	public void testReadWrongTypeWithDefaultValue() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, value);

		Integer defaultValue = 10;
		Integer readValue = confidentalStorage.get(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, Integer.class, defaultValue);
		
		assertEquals(defaultValue, readValue);
	}
	
	@Test
	public void testLoadUnexistedValueWithDefault() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);

		assertNull(confidentalStorage.get(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, Integer.class));
		
		Integer defaultValue = 10;
		Integer readValue = confidentalStorage.get(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, Integer.class, defaultValue);
		
		assertEquals(defaultValue, readValue);
	}
	
	@Test
	public void testReadGuardedString() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String password = "heslo";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, new GuardedString(password).asString());
		
		GuardedString savedPassword = confidentalStorage.getGuardedString(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE);
		
		assertEquals(password, savedPassword.asString());
	}
	
	@Test
	public void testSaveAndReadGuardedString() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_2);
		
		String password = "heslo_save";
		confidentalStorage.saveGuardedString(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, new GuardedString(password));
		
		GuardedString savedPassword = confidentalStorage.getGuardedString(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE);
		
		assertEquals(password, savedPassword.asString());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOwnerWithoutId() {
		// unpersisted identity
		IdmIdentity owner = new IdmIdentity();
		confidentalStorage.get(owner.getId(), IdmIdentity.class, STORAGE_KEY_ONE);
	}
	
	@Test
	public void testUnpersistedOwnerWithId() {
		// unpersisted identity
		IdmIdentity owner = new IdmIdentity(UUID.randomUUID());
		assertNull(confidentalStorage.get(owner.getId(), IdmIdentity.class, STORAGE_KEY_ONE));
	}
}
