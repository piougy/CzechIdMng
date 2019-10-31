package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmAuditFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConfidentialStorageValueFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmConfidentialStorageValueService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.audit.rest.impl.IdmAuditController;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ChangeConfidentialStorageKeyTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.CryptService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultCryptService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for "naive" confidential storage (values are persisted in standard database)
 * and test for LRT {@link ChangeConfidentialStorageKeyTaskExecutor}. The LRT change confidential
 * storage crypt key.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmConfidentialStorageIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ConfigurableEnvironment configurableEnviroment;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	@Autowired private ApplicationContext context;
	@Autowired private IdmConfidentialStorageValueService conidentialStorageValueService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmAuditService auditService;
	@Autowired private IdmAuditController auditController;
	//
	private DefaultIdmConfidentialStorage confidentalStorage;
	
	
	@Before
	public void initStorage() {
		confidentalStorage = context.getAutowireCapableBeanFactory().createBean(DefaultIdmConfidentialStorage.class);
	}
	
	@Test
	public void testOwnerType() {
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		//
		Assert.assertNotNull(confidentalStorage.getOwnerType(owner));
		Assert.assertEquals(confidentalStorage.getOwnerType(owner), confidentalStorage.getOwnerType(owner.getClass()));
	}
	
	@Test
	@Transactional
	public void testLoadUnexistedValue() {
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);		
		
		Serializable storageValue = confidentalStorage.get(identity.getId(), identity.getClass(), storageKeyOne);	
		
		Assert.assertNull(storageValue);
	}
	
	@Transactional
	@Test(expected = IllegalArgumentException.class)
	public void testSaveValueNoOwner() {		
		confidentalStorage.get(null, null, getHelper().createName());	
	}
	
	@Test
	@Transactional
	public void testSaveValue() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		
		String value = "one";
		String storageKeyOne = getHelper().createName();
		confidentalStorage.save(identity.getId(), identity.getClass(), storageKeyOne, value);
		Serializable storageValue = confidentalStorage.get(identity.getId(), identity.getClass(), storageKeyOne);
		
		Assert.assertEquals(value, storageValue);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	@Transactional
	public void testSaveValues() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);	
		String storageKeyOne = getHelper().createName();
		
		ArrayList<String> values = Lists.newArrayList("one", "two", "three");
		confidentalStorage.save(identity.getId(), identity.getClass(), storageKeyOne, values);		
		Serializable storageValue = confidentalStorage.get(identity.getId(), identity.getClass(), storageKeyOne);	
		
		Assert.assertEquals(values.getClass(), storageValue.getClass());
		Assert.assertArrayEquals(values.toArray(new String[]{}), ((List<String>) storageValue).toArray(new String[]{}));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	@Transactional
	public void testSaveMoreKeys() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);	
		String storageKeyOne = getHelper().createName();
		String storageKeyTwo = getHelper().createName();
		
		String value = "one";
		confidentalStorage.save(identity.getId(), identity.getClass(), storageKeyOne, value);		
		ArrayList<String> values = Lists.newArrayList("one", "two", "three");
		confidentalStorage.save(identity.getId(), identity.getClass(), storageKeyTwo, values);		
		
		Serializable storageValueOne = confidentalStorage.get(identity.getId(), identity.getClass(), storageKeyOne);
		Serializable storageValueTwo = confidentalStorage.get(identity.getId(), identity.getClass(), storageKeyTwo);
		
		Assert.assertEquals(value, storageValueOne);
			
		Assert.assertEquals(values.getClass(), storageValueTwo.getClass());
		Assert.assertArrayEquals(values.toArray(new String[]{}), ((List<String>) storageValueTwo).toArray(new String[]{}));
	}
	
	@Test
	@Transactional
	public void testEditSavedValues() {	
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);	
		
		String value = "one";
		confidentalStorage.save(identity.getId(), identity.getClass(), storageKeyOne, value);
		
		Assert.assertEquals(value, confidentalStorage.get(identity.getId(), identity.getClass(), storageKeyOne));
		
		value = "one_update";
		confidentalStorage.save(identity, storageKeyOne, value);
		
		Assert.assertEquals(value, confidentalStorage.get(identity, storageKeyOne));
	}
	
	@Test
	@Transactional
	public void testSaveValueDifferentOwner() {	
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);	
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);	
		
		String valueOne = "one";
		confidentalStorage.save(identityOne.getId(), identityOne.getClass(), storageKeyOne, valueOne);		
		String valueTwo = "two";
		confidentalStorage.save(identityTwo.getId(), identityOne.getClass(), storageKeyOne, valueTwo);
		
		Assert.assertEquals(valueOne, confidentalStorage.get(identityOne.getId(), identityOne.getClass(), storageKeyOne));
		Assert.assertEquals(valueTwo, confidentalStorage.get(identityTwo.getId(), identityTwo.getClass(), storageKeyOne));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	@Transactional
	public void testOverrideSavedValues() {	
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);	
		
		String value = "one";
		confidentalStorage.save(identity.getId(), identity.getClass(), storageKeyOne, value);

		Assert.assertEquals(value, confidentalStorage.get(identity.getId(), identity.getClass(), storageKeyOne));
		
		ArrayList<String> values = Lists.newArrayList("one", "two", "three");
		confidentalStorage.save(identity.getId(), identity.getClass(), storageKeyOne, values);	
		
		Serializable storageValue = confidentalStorage.get(identity.getId(), identity.getClass(), storageKeyOne);
		Assert.assertEquals(values.getClass(), storageValue.getClass());
		Assert.assertArrayEquals(values.toArray(new String[]{}), ((List<String>) storageValue).toArray(new String[]{}));
	}
	
	@Test
	@Transactional
	public void testDeleteSavedValues() {
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);	
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);	
		
		String valueOne = "one";
		confidentalStorage.save(identityOne.getId(), IdmIdentity.class, storageKeyOne, valueOne);		
		String valueTwo = "two";
		confidentalStorage.save(identityTwo.getId(), IdmIdentity.class, storageKeyOne, valueTwo);
		
		Assert.assertEquals(valueOne, confidentalStorage.get(identityOne.getId(), IdmIdentity.class, storageKeyOne));
		Assert.assertEquals(valueTwo, confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, storageKeyOne));
		
		confidentalStorage.delete(identityOne.getId(), IdmIdentity.class, storageKeyOne);
		
		Assert.assertNull(confidentalStorage.get(identityOne.getId(), IdmIdentity.class, storageKeyOne));
		Assert.assertEquals(valueTwo, confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, storageKeyOne));
		
		confidentalStorage.delete(identityTwo, storageKeyOne);
		
		Assert.assertNull(confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, storageKeyOne));
	}
	
	@Test
	@Transactional
	public void testDeleteSavedValuesByOwnerId() {
		String storageKeyOne = getHelper().createName();
		String storageKeyTwo = getHelper().createName();
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);	
		String valueOne = "one";
		confidentalStorage.save(identityOne.getId(), IdmIdentity.class, storageKeyOne, valueOne);		
		String valueTwo = "two";
		confidentalStorage.save(identityOne.getId(), IdmIdentity.class, storageKeyTwo, valueTwo);
		
		Assert.assertEquals(valueOne, confidentalStorage.get(identityOne.getId(), IdmIdentity.class, storageKeyOne));
		Assert.assertEquals(valueTwo, confidentalStorage.get(identityOne.getId(), IdmIdentity.class, storageKeyTwo));
		
		confidentalStorage.deleteAll(identityOne.getId(), IdmIdentity.class);
		
		Assert.assertNull(confidentalStorage.get(identityOne.getId(), IdmIdentity.class, storageKeyOne));
		Assert.assertNull(confidentalStorage.get(identityOne.getId(), IdmIdentity.class, storageKeyTwo));
	}
	
	@Test
	@Transactional
	public void testDeleteSavedValuesByOwner() {
		String storageKeyOne = getHelper().createName();
		String storageKeyTwo = getHelper().createName();
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);	
		String valueOne = "one";
		confidentalStorage.save(identityOne, storageKeyOne, valueOne);		
		String valueTwo = "two";
		confidentalStorage.save(identityOne.getId(), IdmIdentity.class, storageKeyTwo, valueTwo);
		
		Assert.assertEquals(valueOne, confidentalStorage.get(identityOne.getId(), IdmIdentity.class, storageKeyOne));
		Assert.assertEquals(valueTwo, confidentalStorage.get(identityOne.getId(), IdmIdentity.class, storageKeyTwo));
		
		confidentalStorage.deleteAll(identityOne);
		
		Assert.assertNull(confidentalStorage.get(identityOne.getId(), IdmIdentity.class, storageKeyOne));
		Assert.assertNull(confidentalStorage.get(identityOne.getId(), IdmIdentity.class, storageKeyTwo));
	}
	
	@Test
	@Transactional
	public void testReadWithType() {
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);	
		
		String value = "one";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, storageKeyOne, value);
		//
		String readValue = confidentalStorage.get(identity.getId(), IdmIdentity.class, storageKeyOne, String.class);
		Assert.assertEquals(value, readValue);
		//
		readValue = confidentalStorage.get(identity, storageKeyOne, String.class);
		Assert.assertEquals(value, readValue);
	}
	
	@Transactional
	@Test(expected = IllegalArgumentException.class)
	public void testReadWrongType() {
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);	
		
		String value = "one";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, storageKeyOne, value);

		confidentalStorage.get(identity.getId(), IdmIdentity.class, storageKeyOne, Integer.class);
	}
	
	@Test
	@Transactional
	public void testReadWrongTypeWithDefaultValue() {
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);	
		
		String value = "one";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, storageKeyOne, value);

		Integer defaultValue = 10;
		Integer readValue = confidentalStorage.get(identity.getId(), IdmIdentity.class, storageKeyOne, Integer.class, defaultValue);
		
		Assert.assertEquals(defaultValue, readValue);
		
		readValue = confidentalStorage.get(identity, storageKeyOne, Integer.class, defaultValue);
		
		Assert.assertEquals(defaultValue, readValue);
	}
	
	@Test
	@Transactional
	public void testLoadUnexistedValueWithDefault() {
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);	

		Assert.assertNull(confidentalStorage.get(identity.getId(), IdmIdentity.class, storageKeyOne, Integer.class));
		
		Integer defaultValue = 10;
		Integer readValue = confidentalStorage.get(identity.getId(), IdmIdentity.class, storageKeyOne, Integer.class, defaultValue);
		
		Assert.assertEquals(defaultValue, readValue);
	}
	
	@Test
	@Transactional
	public void testReadGuardedString() {
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);	
		
		String password = "heslo";
		confidentalStorage.save(identity.getId(), IdmIdentity.class, storageKeyOne, new GuardedString(password).asString());
		
		GuardedString savedPassword = confidentalStorage.getGuardedString(identity, storageKeyOne);
		
		Assert.assertEquals(password, savedPassword.asString());
	}
	
	@Test
	@Transactional
	public void testSaveAndReadGuardedString() {
		String storageKeyOne = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		
		String password = "heslo_save";
		confidentalStorage.saveGuardedString(identity.getId(), IdmIdentity.class, storageKeyOne, new GuardedString(password));
		
		GuardedString savedPassword = confidentalStorage.getGuardedString(identity.getId(), IdmIdentity.class, storageKeyOne);
		
		Assert.assertEquals(password, savedPassword.asString());
	}
	
	@Transactional
	@Test(expected = IllegalArgumentException.class)
	public void testOwnerWithoutId() {
		String storageKeyOne = getHelper().createName();
		// unpersisted identity
		IdmIdentity owner = new IdmIdentity();
		confidentalStorage.get(owner.getId(), IdmIdentity.class, storageKeyOne);
	}
	
	@Test
	@Transactional
	public void testUnpersistedOwnerWithId() {
		String storageKeyOne = getHelper().createName();
		// unpersisted identity
		IdmIdentity owner = new IdmIdentity(UUID.randomUUID());
		Assert.assertNull(confidentalStorage.get(owner.getId(), IdmIdentity.class, storageKeyOne));
	}

	@Test
	@Transactional
	public void testChangeConfidentialStorageKey() {
		IdmIdentityDto identityOne = getHelper().createIdentity(getHelper().createName(), null);
		IdmIdentityDto identityTwo = getHelper().createIdentity(getHelper().createName(), null);
		IdmIdentityDto identityThree = getHelper().createIdentity(getHelper().createName(), null);
		
		String passwordOne = "testPassword-" + getHelper().createName();
		String passwordTwo = "testPassword-" + getHelper().createName();
		String passwordThree = "testPassword-" + getHelper().createName();
		
		confidentalStorage.saveGuardedString(identityOne.getId(), IdmIdentity.class, identityOne.getUsername(), new GuardedString(passwordOne));
		confidentalStorage.saveGuardedString(identityTwo.getId(), IdmIdentity.class, identityTwo.getUsername(), new GuardedString(passwordTwo));
		confidentalStorage.saveGuardedString(identityThree, identityThree.getUsername(), new GuardedString(passwordThree));

		Serializable serializable = confidentalStorage.get(identityOne.getId(), IdmIdentity.class, identityOne.getUsername());
		Assert.assertEquals(passwordOne, serializable);
		serializable = confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, identityTwo.getUsername());
		Assert.assertEquals(passwordTwo, serializable);
		serializable = confidentalStorage.get(identityThree.getId(), IdmIdentity.class, identityThree.getUsername());
		Assert.assertEquals(passwordThree, serializable);

		String newKey = getHelper().createName();
		newKey = newKey.substring(newKey.length() - 16);
		
		this.changeCypherKey(newKey);

		// try with old key
		try {
			confidentalStorage.get(identityOne.getId(), IdmIdentity.class, identityOne.getUsername());
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getCause() instanceof BadPaddingException);
		}
		
		try {
			confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, identityTwo.getUsername());
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getCause() instanceof BadPaddingException);
		}

		try {
			confidentalStorage.get(identityThree.getId(), IdmIdentity.class, identityThree.getUsername());
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getCause() instanceof BadPaddingException);
		}

		// change key in LRT
		runChangeConfidentialStorageKeyTask(configurableEnviroment.getProperty(CryptService.APPLICATION_PROPERTIES_KEY));
		
		serializable = confidentalStorage.get(identityOne.getId(), IdmIdentity.class, identityOne.getUsername());
		Assert.assertEquals(passwordOne, serializable);
		serializable = confidentalStorage.get(identityTwo.getId(), IdmIdentity.class, identityTwo.getUsername());
		Assert.assertEquals(passwordTwo, serializable);
		serializable = confidentalStorage.get(identityThree.getId(), IdmIdentity.class, identityThree.getUsername());
		Assert.assertEquals(passwordThree, serializable);

		reinitializingKey();
	}
	
	@Transactional
	@Test(expected = ResultCodeException.class)
	public void testRunChangeConfidetialKeyWithoutKey() {
		runChangeConfidentialStorageKeyTask(null);
	}
	
	@Transactional
	@Test(expected = ResultCodeException.class)
	public void testRunChangeConfidetialKeyEmptyKey() {
		runChangeConfidentialStorageKeyTask("");
	}
	
	@Transactional
	@Test(expected = ResultCodeException.class)
	public void testRunChangeConfidetialKeySmallKey() {
		runChangeConfidentialStorageKeyTask("123456789");
	}

	@Test
	public void testRemoveOwnerAndCheckAudit() {
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString) null);

		confidentalStorage.saveGuardedString(identity.getId(), IdmIdentity.class, identity.getUsername(),
				new GuardedString(identity.getUsername()));

		IdmConfidentialStorageValueFilter filter = new IdmConfidentialStorageValueFilter();
		filter.setOwnerId(identity.getId());
		List<IdmConfidentialStorageValueDto> values = conidentialStorageValueService.find(filter, null).getContent();
		IdmConfidentialStorageValueDto storageValueDto = values.get(0);
		Assert.assertEquals(1, values.size());
		storageValueDto = conidentialStorageValueService.get(storageValueDto.getId());
		Assert.assertNotNull(storageValueDto);

		IdmAuditFilter auditFilter = new IdmAuditFilter();
		auditFilter.setEntityId(storageValueDto.getId());
		List<IdmAuditDto> audits = auditService.find(auditFilter, null).getContent();
		Assert.assertEquals(1, audits.size());

		confidentalStorage.delete(identity, identity.getUsername());
		identityService.delete(identity);

		values = conidentialStorageValueService.find(filter, null).getContent();
		Assert.assertEquals(0, values.size());
		audits = auditService.find(auditFilter, null).getContent();
		Assert.assertEquals(2, audits.size());

		for (IdmAuditDto audit : audits) {
			Assert.assertEquals(storageValueDto.getId(), audit.getEntityId());
		}

		audits = auditController.find(auditFilter, null, null).getContent();
		Assert.assertEquals(2, audits.size());
		for (IdmAuditDto audit : audits) {
			Assert.assertEquals(storageValueDto.getId(), audit.getEntityId());
		}

	}

	@Test
	public void testNonExistingId() {
		IdmConfidentialStorageValueDto storageValueDto = conidentialStorageValueService.get(UUID.randomUUID());
		Assert.assertNull(storageValueDto);
	}

	/**
	 * Create and run task for change confidential storage key.
	 * Task {@link ChangeConfidentialStorageKeyTaskExecutor}
	 *
	 * @param oldKey
	 * @return
	 */
	private void runChangeConfidentialStorageKeyTask(String oldKey) {
		ChangeConfidentialStorageKeyTaskExecutor task = new ChangeConfidentialStorageKeyTaskExecutor();
		Map<String, Object> properties = new HashMap<>();
		properties.put(ChangeConfidentialStorageKeyTaskExecutor.PARAMETER_OLD_CONFIDENTIAL_KEY, oldKey);
		task.init(properties);
		//
		Boolean executed = longRunningTaskManager.executeSync(task);
		Assert.assertTrue(executed);
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
			Assert.fail("Problem with call method 'init', Exception: " + e.getLocalizedMessage());
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
			Assert.fail("Problem with set field 'key', Exception: " + e.getLocalizedMessage());
		}
	}
}
