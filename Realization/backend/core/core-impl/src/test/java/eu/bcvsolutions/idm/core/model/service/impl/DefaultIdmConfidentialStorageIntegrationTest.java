package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmConfidentialStorageValueRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ChangeConfidentialStorageKey;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.CryptService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultCryptService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for "naive" confidential storage (values are persisted in standard database)
 * and test for LRT {@link ChangeConfidentialStorageKey}. The LRT change confidential
 * storage crypt key.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmConfidentialStorageIntegrationTest extends AbstractIntegrationTest {
	
	private static final String STORAGE_KEY_ONE = "test_key_one";
	private static final String STORAGE_KEY_TWO = "test_key_two";
	//
	@Autowired private IdmConfidentialStorageValueRepository repository;
	@Autowired private IdmIdentityRepository identityRepository;
	@Autowired private CryptService cryptService;
	@Autowired private ConfigurableEnvironment configurableEnviroment;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	//
	private DefaultIdmConfidentialStorage confidentalStorage;
	
	
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
	@Transactional
	public void testLoadUnexistedValue() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);		
		
		Serializable storageValue = confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_ONE);	
		
		assertNull(storageValue);
	}
	
	@Transactional
	@Test(expected = IllegalArgumentException.class)
	public void testSaveValueNoOwner() {		
		confidentalStorage.get(null, null, STORAGE_KEY_ONE);	
	}
	
	@Test
	@Transactional
	public void testSaveValue() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), identity.getClass(), STORAGE_KEY_ONE, value);
		Serializable storageValue = confidentalStorage.get(identity.getId(), identity.getClass(), STORAGE_KEY_ONE);
		
		assertEquals(value, storageValue);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	@Transactional
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
	@Transactional
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
	@Transactional
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
	@Transactional
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
	@Transactional
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
	@Transactional
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
	@Transactional
	public void testDeleteSavedValuesByOwner() {
		IdmIdentity identityOne = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		String valueOne = "one";
		confidentalStorage.save(identityOne.getId(), IdmIdentity.class, STORAGE_KEY_ONE, valueOne);		
		String valueTwo = "two";
		confidentalStorage.save(identityOne.getId(), IdmIdentity.class, STORAGE_KEY_TWO, valueTwo);
		
		assertEquals(valueOne, confidentalStorage.get(identityOne.getId(), IdmIdentity.class, STORAGE_KEY_ONE));
		assertEquals(valueTwo, confidentalStorage.get(identityOne.getId(), IdmIdentity.class, STORAGE_KEY_TWO));
		
		confidentalStorage.deleteAll(identityOne.getId(), IdmIdentity.class);
		
		assertNull(confidentalStorage.get(identityOne.getId(), IdmIdentity.class, STORAGE_KEY_ONE));
		assertNull(confidentalStorage.get(identityOne.getId(), IdmIdentity.class, STORAGE_KEY_TWO));
	}
	
	@Test
	@Transactional
	public void testReadWithType() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, value);

		String readValue = confidentalStorage.get(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, String.class);
		
		assertEquals(value, readValue);
	}
	
	@Transactional
	@Test(expected = IllegalArgumentException.class)
	public void testReadWrongType() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, value);

		confidentalStorage.get(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, Integer.class);
	}
	
	@Test
	@Transactional
	public void testReadWrongTypeWithDefaultValue() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String value = "one";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, value);

		Integer defaultValue = 10;
		Integer readValue = confidentalStorage.get(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, Integer.class, defaultValue);
		
		assertEquals(defaultValue, readValue);
	}
	
	@Test
	@Transactional
	public void testLoadUnexistedValueWithDefault() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);

		assertNull(confidentalStorage.get(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, Integer.class));
		
		Integer defaultValue = 10;
		Integer readValue = confidentalStorage.get(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, Integer.class, defaultValue);
		
		assertEquals(defaultValue, readValue);
	}
	
	@Test
	@Transactional
	public void testReadGuardedString() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		String password = "heslo";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, new GuardedString(password).asString());
		
		GuardedString savedPassword = confidentalStorage.getGuardedString(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE);
		
		assertEquals(password, savedPassword.asString());
	}
	
	@Test
	@Transactional
	public void testSaveAndReadGuardedString() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_2);
		
		String password = "heslo_save";
		confidentalStorage.saveGuardedString(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE, new GuardedString(password));
		
		GuardedString savedPassword = confidentalStorage.getGuardedString(identity.getId(), IdmIdentity.class, STORAGE_KEY_ONE);
		
		assertEquals(password, savedPassword.asString());
	}
	
	@Transactional
	@Test(expected = IllegalArgumentException.class)
	public void testOwnerWithoutId() {
		// unpersisted identity
		IdmIdentity owner = new IdmIdentity();
		confidentalStorage.get(owner.getId(), IdmIdentity.class, STORAGE_KEY_ONE);
	}
	
	@Test
	@Transactional
	public void testUnpersistedOwnerWithId() {
		// unpersisted identity
		IdmIdentity owner = new IdmIdentity(UUID.randomUUID());
		assertNull(confidentalStorage.get(owner.getId(), IdmIdentity.class, STORAGE_KEY_ONE));
	}

	@Test
	public void testChangeConfidentialStorageKey() {
		IdmIdentityDto identityOne = getHelper().createIdentity(getHelper().createName(), null);
		IdmIdentityDto identityTwo = getHelper().createIdentity(getHelper().createName(), null);
		IdmIdentityDto identityThree = getHelper().createIdentity(getHelper().createName(), null);
		
		String passwordOne = "testPassword-" + System.currentTimeMillis();
		String passwordTwo = "testPassword-" + System.currentTimeMillis();
		String passwordThree = "testPassword-" + System.currentTimeMillis();
		
		confidentalStorage.saveGuardedString(identityOne.getId(), IdmIdentity.class, identityOne.getUsername(), new GuardedString(passwordOne));
		confidentalStorage.saveGuardedString(identityTwo.getId(), IdmIdentity.class, identityTwo.getUsername(), new GuardedString(passwordTwo));
		confidentalStorage.saveGuardedString(identityThree.getId(), IdmIdentity.class, identityThree.getUsername(), new GuardedString(passwordThree));

		Serializable serializable = confidentalStorage.get(identityOne.getId(), IdmIdentity.class, identityOne.getUsername());
		assertEquals(passwordOne, serializable);
		serializable = confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, identityTwo.getUsername());
		assertEquals(passwordTwo, serializable);
		serializable = confidentalStorage.get(identityThree.getId(), IdmIdentity.class, identityThree.getUsername());
		assertEquals(passwordThree, serializable);

		this.changeCypherKey(getHelper().createName().substring(0, 16));

		// try with old key
		try {
			confidentalStorage.get(identityOne.getId(), IdmIdentity.class, identityOne.getUsername());
			fail();
		} catch (Exception e) {
			assertTrue(e.getCause().getMessage().contains("bad key is used during decryption"));
		}
		
		try {
			confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, identityTwo.getUsername());
			fail();
		} catch (Exception e) {
			assertTrue(e.getCause().getMessage().contains("bad key is used during decryption"));
		}

		try {
			confidentalStorage.get(identityThree.getId(), IdmIdentity.class, identityThree.getUsername());
			fail();
		} catch (Exception e) {
			assertTrue(e.getCause().getMessage().contains("bad key is used during decryption"));
		}

		// change key in LRT
		runChangeConfidentialStorageKeyTask(configurableEnviroment.getProperty(CryptService.APPLICATION_PROPERTIES_KEY));
//		assertEquals(OperationState.EXECUTED, result.getState());
		
		serializable = confidentalStorage.get(identityOne.getId(), IdmIdentity.class, identityOne.getUsername());
		assertEquals(passwordOne, serializable);
		serializable = confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, identityTwo.getUsername());
		assertEquals(passwordTwo, serializable);
		serializable = confidentalStorage.get(identityThree.getId(), IdmIdentity.class, identityThree.getUsername());
		assertEquals(passwordThree, serializable);

		reinitializingKey();
	}
	
	@Test(expected = ResultCodeException.class)
	public void runChangeConfidetialKeyWithoutKey() {
		runChangeConfidentialStorageKeyTask(null);
	}
	
	@Test(expected = ResultCodeException.class)
	public void runChangeConfidetialKeyEmptyKey() {
		runChangeConfidentialStorageKeyTask("");
	}
	
	@Test(expected = ResultCodeException.class)
	public void runChangeConfidetialKeySmallKey() {
		runChangeConfidentialStorageKeyTask("123456789");
	}

	/**
	 * Create and run task for change confidential storage key.
	 * Task {@link ChangeConfidentialStorageKey}
	 *
	 * @param oldKey
	 * @return
	 */
	private void runChangeConfidentialStorageKeyTask(String oldKey) {
		ChangeConfidentialStorageKey task = new ChangeConfidentialStorageKey();
		Map<String, Object> properties = new HashMap<>();
		properties.put(ChangeConfidentialStorageKey.PARAMETER_OLD_CONFIDENTIAL_KEY, oldKey);
		task.init(properties);
		//
		Boolean executed = longRunningTaskManager.executeSync(task);
		assertTrue(executed);
	}

	/**
	 * Reinitialize key for crypt
	 */
	private void reinitializingKey() {
		DefaultCryptService defaultCryptService = AutowireHelper.getBean(DefaultCryptService.class);
		try {
			Method initMethod = defaultCryptService.getClass().getDeclaredMethod("init");
			initMethod.setAccessible(true);
			initMethod.invoke(defaultCryptService);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			fail("Problem with call method 'init', Exception: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Change key for crypt in {@link DefaultCryptService}
	 *
	 * @param newKey
	 */
	private void changeCypherKey(String newKey) {
		DefaultCryptService defaultCryptService = AutowireHelper.getBean(DefaultCryptService.class);
		try {
			FieldUtils.writeField(defaultCryptService, "key", new SecretKeySpec(newKey.getBytes(), "AES"), true);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			fail("Problem with set field 'key', Exception: " + e.getLocalizedMessage());
		}
	}
}
