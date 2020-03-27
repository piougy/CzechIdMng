package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseCodeList;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAllAutomaticRoleByAttributeTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAutomaticRoleByAttributeTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for automatic roles by attribute and their rules
 * 
 * @author Ondrej Kopr
 *
 */
@FixMethodOrder
public class DefaultIdmAutomaticRoleAttributeIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired
	private IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private IdmLongRunningTaskService longRunningTaskService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmEmailLogRepository emailLogRepository;
	@Autowired
	private IdmNotificationLogRepository notificationRepository;

	/**
	 * Delete all identities for improve performance of this test. In some cases
	 * taken this test more then 40m (because previous tests created and not deleted
	 * many identities).
	 * 
	 * This delete all identities instead first 20 (we don't want delete demo and
	 * test data)!
	 */
	@Before
	public void purgeData() {
		// First delete notification ... because here is integrity problem!
		emailLogRepository.deleteAll();
		notificationRepository.deleteAll();

		List<IdmIdentityDto> identities = identityService
				.find(PageRequest.of(0, Integer.MAX_VALUE, new Sort(Direction.ASC, IdmIdentity_.created.getName())))
				.getContent();
		// This delete all identities instead first 20 (we don't want delete demo and
		// test data)!
		for (int i = 20; i < identities.size(); i++) {
			IdmIdentityDto dto = identities.get(i);
			identityService.delete(dto);
		}
	}
	
	@After
	public void logout() {
		automaticRoleAttributeService.find(null).forEach(autoRole -> {
			automaticRoleAttributeService.delete(autoRole);
		});
	}

	@Test
	public void testAutomaticRoleCrud() {
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getHelper().createName());
		IdmAutomaticRoleAttributeDto savedAutomaticRole = automaticRoleAttributeService.save(automaticRole);
		//
		assertNotNull(savedAutomaticRole);
		assertNotNull(savedAutomaticRole.getId());
		assertEquals(automaticRole.getRole(), savedAutomaticRole.getRole());
		assertEquals(automaticRole.getName(), savedAutomaticRole.getName());
		//
		try {
			// update isn't allowed
			savedAutomaticRole.setName(getHelper().createName());
			automaticRoleAttributeService.save(savedAutomaticRole);
			fail();
		} catch (Exception e) {
			// success
		}
		//
		automaticRoleAttributeService.delete(savedAutomaticRole);
		//
		IdmAutomaticRoleAttributeDto automaticRoleNull = automaticRoleAttributeService.get(savedAutomaticRole.getId());
		assertNull(automaticRoleNull);
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleGreaterThen() {
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getHelper().createName());
		automaticRole = automaticRoleAttributeService.save(automaticRole);

		IdmAutomaticRoleAttributeRuleDto rule = new IdmAutomaticRoleAttributeRuleDto();
		rule.setComparison(AutomaticRoleAttributeRuleComparison.GREATER_THAN_OR_EQUAL);
		rule.setType(AutomaticRoleAttributeRuleType.IDENTITY);
		rule.setValue("10");
		rule.setAttributeName(IdmIdentity_.username.getName());
		rule.setAutomaticRoleAttribute(automaticRole.getId());
		automaticRoleAttributeRuleService.save(rule);
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleLessThen() {
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getHelper().createName());
		automaticRole = automaticRoleAttributeService.save(automaticRole);

		IdmAutomaticRoleAttributeRuleDto rule = new IdmAutomaticRoleAttributeRuleDto();
		rule.setComparison(AutomaticRoleAttributeRuleComparison.LESS_THAN_OR_EQUAL);
		rule.setType(AutomaticRoleAttributeRuleType.CONTRACT);
		rule.setValue("10");
		rule.setAttributeName(IdmIdentityContract_.position.getName());
		rule.setAutomaticRoleAttribute(automaticRole.getId());
		automaticRoleAttributeRuleService.save(rule);
	}

	@Test(expected = ResultCodeException.class)
	public void testIncompatibleContainsMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.CONTAINS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);
	}

	@Test(expected = ResultCodeException.class)
	public void testIncompatibleEndWithMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.END_WITH, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);
	}

	@Test(expected = ResultCodeException.class)
	public void testIncompatibleGreaterThanMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.GREATER_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);
	}

	@Test(expected = ResultCodeException.class)
	public void testIncompatibleLessThanMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.LESS_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);
	}

	@Test(expected = ResultCodeException.class)
	public void testIncompatibleNotContainsMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_CONTAINS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);
	}

	@Test(expected = ResultCodeException.class)
	public void testIncompatibleNotEndWithMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_END_WITH, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);
	}

	@Test(expected = ResultCodeException.class)
	public void testIncompatibleNotEqualsMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_EQUALS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);
	}

	@Test(expected = ResultCodeException.class)
	public void testIncompatibleNotStartWithMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_START_WITH, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);
	}

	@Test(expected = ResultCodeException.class)
	public void testIncompatibleStartWithMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.START_WITH, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleMultipleWithContains() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.CONTAINS, getHelper().createName());
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleMultipleWithEndWith() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.END_WITH, getHelper().createName());
	}

	@Test
	public void testCreateCompatibleRuleMultipleWithEquals() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.EQUALS, getHelper().createName());
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleMultipleWithGreater() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.GREATER_THAN_OR_EQUAL, "10");
	}

	@Test
	public void testCreateIncompatibleRuleMultipleWithIsEmpty() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.IS_EMPTY, null);
	}

	@Test
	public void testCreateIncompatibleRuleMultipleWithIsNotEmpty() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, null);
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleMultipleWithLess() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.LESS_THAN_OR_EQUAL, "50");
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleMultipleWithNotContains() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.NOT_CONTAINS, getHelper().createName());
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleMultipleWithNotEndWith() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.NOT_END_WITH, getHelper().createName());
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleMultipleWithNotEquals() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.NOT_EQUALS, getHelper().createName());
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleMultipleWithNotStartWith() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.NOT_START_WITH, getHelper().createName());
	}

	@Test(expected = ResultCodeException.class)
	public void testCreateIncompatibleRuleMultipleWithStartWith() {
		createRuleForContractEav(AutomaticRoleAttributeRuleComparison.START_WITH, getHelper().createName());
	}

	@Test
	public void testFilterHasRules() {
		long totalElements = automaticRoleAttributeService.find(null).getTotalElements();
		assertEquals(0, totalElements);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getHelper().createName());
		automaticRole = automaticRoleAttributeService.save(automaticRole);
		//
		IdmAutomaticRoleFilter filter = new IdmAutomaticRoleFilter();
		filter.setHasRules(true);
		totalElements = automaticRoleAttributeService.find(filter, null).getNumberOfElements();
		assertEquals(0, totalElements);
		//
		filter.setHasRules(false);
		List<IdmAutomaticRoleAttributeDto> content = automaticRoleAttributeService.find(filter, null).getContent();
		assertEquals(1, content.size());
		IdmAutomaticRoleAttributeDto found = content.get(0);
		assertEquals(automaticRole.getId(), found.getId());
		//
		automaticRoleAttributeService.deleteInternal(found);
		//
		automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getHelper().createName());
		automaticRole = automaticRoleAttributeService.save(automaticRole);
		//
		IdmAutomaticRoleAttributeRuleDto rule1 = new IdmAutomaticRoleAttributeRuleDto();
		rule1.setComparison(AutomaticRoleAttributeRuleComparison.EQUALS);
		rule1.setType(AutomaticRoleAttributeRuleType.IDENTITY);
		rule1.setValue("test");
		rule1.setAttributeName(IdmIdentity_.username.getName());
		rule1.setAutomaticRoleAttribute(automaticRole.getId());
		automaticRoleAttributeRuleService.save(rule1);
		//
		filter = new IdmAutomaticRoleFilter();
		filter.setHasRules(true);
		content = automaticRoleAttributeService.find(filter, null).getContent();
		assertEquals(1, content.size());
		found = content.get(0);
		assertEquals(automaticRole.getId(), found.getId());
		//
		// try add next rules
		IdmAutomaticRoleAttributeRuleDto rule2 = new IdmAutomaticRoleAttributeRuleDto();
		rule2.setComparison(AutomaticRoleAttributeRuleComparison.EQUALS);
		rule2.setType(AutomaticRoleAttributeRuleType.IDENTITY);
		rule2.setValue("test");
		rule2.setAttributeName(IdmIdentity_.username.getName());
		rule2.setAutomaticRoleAttribute(automaticRole.getId());
		automaticRoleAttributeRuleService.save(rule2);
		//
		IdmAutomaticRoleAttributeRuleDto rule3 = new IdmAutomaticRoleAttributeRuleDto();
		rule3.setComparison(AutomaticRoleAttributeRuleComparison.EQUALS);
		rule3.setType(AutomaticRoleAttributeRuleType.IDENTITY);
		rule3.setValue("test");
		rule3.setAttributeName(IdmIdentity_.username.getName());
		rule3.setAutomaticRoleAttribute(automaticRole.getId());
		automaticRoleAttributeRuleService.save(rule3);
		//
		filter = new IdmAutomaticRoleFilter();
		filter.setHasRules(true);
		content = automaticRoleAttributeService.find(filter, null).getContent();
		assertEquals(1, content.size());
		found = content.get(0);
		assertEquals(automaticRole.getId(), found.getId());
	}
	
	@Test
	public void testFilterRuleType() {
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getHelper().createName());
		automaticRole = automaticRoleAttributeService.save(automaticRole);
		//
		IdmAutomaticRoleFilter filter = new IdmAutomaticRoleFilter();
		filter.setRuleType(AutomaticRoleAttributeRuleType.CONTRACT);
		List<IdmAutomaticRoleAttributeDto> content = automaticRoleAttributeService.find(filter, null).getContent();
		assertEquals(0, content.size());
		//
		IdmAutomaticRoleAttributeRuleDto rule1 = new IdmAutomaticRoleAttributeRuleDto();
		rule1.setComparison(AutomaticRoleAttributeRuleComparison.EQUALS);
		rule1.setType(AutomaticRoleAttributeRuleType.IDENTITY);
		rule1.setValue("test");
		rule1.setAttributeName(IdmIdentity_.username.getName());
		rule1.setAutomaticRoleAttribute(automaticRole.getId());
		automaticRoleAttributeRuleService.save(rule1);
		//
		filter = new IdmAutomaticRoleFilter();
		filter.setRuleType(AutomaticRoleAttributeRuleType.CONTRACT);
		content = automaticRoleAttributeService.find(filter, null).getContent();
		assertEquals(0, content.size());
		//
		// try add next rules
		IdmAutomaticRoleAttributeRuleDto rule2 = new IdmAutomaticRoleAttributeRuleDto();
		rule2.setComparison(AutomaticRoleAttributeRuleComparison.EQUALS);
		rule2.setType(AutomaticRoleAttributeRuleType.CONTRACT);
		rule2.setValue("test");
		rule2.setAttributeName(IdmIdentityContract_.description.getName());
		rule2.setAutomaticRoleAttribute(automaticRole.getId());
		automaticRoleAttributeRuleService.save(rule2);
		//
		filter = new IdmAutomaticRoleFilter();
		filter.setRuleType(AutomaticRoleAttributeRuleType.CONTRACT);
		content = automaticRoleAttributeService.find(filter, null).getContent();
		assertEquals(1, content.size());
		IdmAutomaticRoleAttributeDto found = content.get(0);
		assertEquals(automaticRole.getId(), found.getId());
	}
	
	@Test
	public void testEmptyContract() {
		String testValue = "123!@#" + System.currentTimeMillis();
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, testValue);
		//
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		// remove contract
		IdmIdentityContractDto primeContract = identityContractService.getPrimeContract(identity.getId());
		identityContractService.delete(primeContract);
		primeContract = identityContractService.getPrimeContract(identity.getId());
		assertNull(primeContract);
		//
		identity.setDescription(testValue);
		identity = identityService.save(identity);
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		// change value and recalculate
		identity.setDescription(testValue + "-test");
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// chnage back
		identity.setDescription(testValue);
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}

	@Test
	public void testAutomaticRoleRuleCrud() {
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getHelper().createName());
		automaticRole = automaticRoleAttributeService.save(automaticRole);
		//
		IdmAutomaticRoleAttributeRuleDto rule1 = new IdmAutomaticRoleAttributeRuleDto();
		rule1.setComparison(AutomaticRoleAttributeRuleComparison.EQUALS);
		rule1.setType(AutomaticRoleAttributeRuleType.IDENTITY);
		rule1.setValue("test");
		rule1.setAttributeName(IdmIdentity_.username.getName());
		rule1.setAutomaticRoleAttribute(automaticRole.getId());
		IdmAutomaticRoleAttributeRuleDto rule1Saved = automaticRoleAttributeRuleService.save(rule1);
		//
		assertNotNull(rule1Saved.getId());
		assertEquals(rule1.getComparison(), rule1Saved.getComparison());
		assertEquals(rule1.getValue(), rule1Saved.getValue());
		assertEquals(rule1.getAttributeName(), rule1Saved.getAttributeName());
		assertEquals(rule1.getAutomaticRoleAttribute(), rule1Saved.getAutomaticRoleAttribute());
		//
		IdmAutomaticRoleAttributeRuleDto rule2 = new IdmAutomaticRoleAttributeRuleDto();
		rule2.setComparison(AutomaticRoleAttributeRuleComparison.EQUALS);
		rule2.setType(AutomaticRoleAttributeRuleType.CONTRACT);
		rule2.setAttributeName(IdmIdentityContract_.description.getName());
		rule2.setValue("test2");
		rule2.setAutomaticRoleAttribute(automaticRole.getId());
		IdmAutomaticRoleAttributeRuleDto rule2Saved = automaticRoleAttributeRuleService.save(rule2);
		//
		assertNotNull(rule2Saved.getId());
		assertEquals(rule2.getComparison(), rule2Saved.getComparison());
		assertEquals(rule2.getValue(), rule2Saved.getValue());
		assertEquals(rule2.getAttributeName(), rule2Saved.getAttributeName());
		assertEquals(rule2.getAutomaticRoleAttribute(), rule2Saved.getAutomaticRoleAttribute());
		//
		// update is allowed
		rule1Saved.setAttributeName(IdmIdentity_.description.getName());
		IdmAutomaticRoleAttributeRuleDto updatedRule1 = automaticRoleAttributeRuleService.save(rule1Saved);
		assertEquals(rule1Saved.getComparison(), updatedRule1.getComparison());
		assertEquals(rule1Saved.getValue(), updatedRule1.getValue());
		assertEquals(rule1Saved.getAttributeName(), updatedRule1.getAttributeName());
		assertEquals(rule1Saved.getAutomaticRoleAttribute(), updatedRule1.getAutomaticRoleAttribute());
		//
		IdmAutomaticRoleAttributeRuleFilter filter = new IdmAutomaticRoleAttributeRuleFilter();
		filter.setAutomaticRoleAttributeId(automaticRole.getId());
		List<IdmAutomaticRoleAttributeRuleDto> content = automaticRoleAttributeRuleService.find(filter, null)
				.getContent();
		assertEquals(2, content.size());
		//
		automaticRoleAttributeRuleService.delete(rule1Saved);
		content = automaticRoleAttributeRuleService.find(filter, null).getContent();
		assertEquals(1, content.size());
		IdmAutomaticRoleAttributeRuleDto findAutomaticRole = automaticRoleAttributeRuleService.get(rule1Saved.getId());
		assertNull(findAutomaticRole);
	}

	@Test
	public void testAssingByIdentityAttrWithRecalcualte() {
		String testValue = "123!@#" + System.currentTimeMillis();
		IdmIdentityDto identity = getHelper().createIdentity();
		identity.setDescription(testValue);
		identity = identityService.save(identity);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, testValue);
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// add new one
		
		this.recalculateSync(automaticRole.getId());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertNotNull(identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getId(), identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getRole(), identityRoleDto.getRole());
		//
		// change value and recalculate
		identity.setDescription(testValue + "-test");
		identity = identityService.save(identity);
		//
		// recalculate isn't needed, is done when save identity contract
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAssingByIdentityAttrWithoutRecalcualte() {
		String testValue = "123!@#" + System.currentTimeMillis();
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, testValue);
		//
		IdmIdentityDto identity = getHelper().createIdentity();
		identity.setDescription(testValue);
		identity = identityService.save(identity);
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertNotNull(identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getId(), identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getRole(), identityRoleDto.getRole());
		//
		// change value and recalculate
		identity.setDescription(testValue + "-test");
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAssingByContractAttrWithRecalcualte() {
		String testValue = "123!@#" + System.currentTimeMillis();
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		primeContract.setPosition(testValue);
		primeContract = identityContractService.save(primeContract);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), null, testValue);
		//
		// add new one
		this.recalculateSync(automaticRole.getId());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertNotNull(identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getId(), identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getRole(), identityRoleDto.getRole());
		//
		// change value and recalculate
		primeContract.setPosition(testValue + "test");
		primeContract = identityContractService.save(primeContract);
		//
		// recalculate isn't needed, is done when save identity
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAssingByContractAttrWithoutRecalcualte() {
		String testValue = "123!@#" + System.currentTimeMillis();
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), null, testValue);
		//
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		primeContract.setPosition(testValue);
		primeContract = identityContractService.save(primeContract);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertNotNull(identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getId(), identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getRole(), identityRoleDto.getRole());
		//
		// change value and recalculate
		primeContract.setPosition(testValue + "test");
		primeContract = identityContractService.save(primeContract);
		//
		// recalculate isn't needed, is done when save identity
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAssingByIdentityEavAttrWithRecalcualte() {
		String testValue = "123!@#" + System.currentTimeMillis();
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.SHORTTEXT);
		getHelper().setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.SHORTTEXT);
		//
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue);
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// add new one
		this.recalculateSync(automaticRole.getId());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertNotNull(identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getId(), identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getRole(), identityRoleDto.getRole());
		//
		// change value and recalculate
		getHelper().setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue + "-test", PersistentType.SHORTTEXT);
		//
		// recalculate isn't needed, is done when save identity contract
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAssingByContractEavAttrWithoutRecalcualte() {
		String testValue = "123!@#" + System.currentTimeMillis();
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		//
		IdmFormAttributeDto createEavAttribute = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.SHORTTEXT);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, createEavAttribute.getId(), testValue);
		//
		primeContract.setPosition(testValue);
		primeContract = identityContractService.save(primeContract);
		//
		// we need to save eav value, resave identity doesn't enough - we need event eav_save
		getHelper().setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testValue, PersistentType.SHORTTEXT);
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertNotNull(identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getId(), identityRoleDto.getAutomaticRole());
		assertEquals(automaticRole.getRole(), identityRoleDto.getRole());
		//
		// change value and recalculate
		getHelper().setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testValue + "-test", PersistentType.SHORTTEXT);
		//
		// recalculate isn't needed, is done when save identity
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testMoreRulesIdentity() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		//
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		//
		String email = "test@example.tld";
		String username = getHelper().createName();
		String firstName = "firstName";
		//
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.email.getName(), null, email);
		//
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, username);
		//
		this.recalculateSync(automaticRole.getId());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		identity.setUsername(username);
		identity = identityService.save(identity);
		//
		// identity hasn't equals email
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		identity.setUsername(username + "test");
		identity.setEmail(email);
		identity = identityService.save(identity);
		//
		// identity hasn't equals username
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		IdmAutomaticRoleAttributeRuleDto firstNameRule = getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.firstName.getName(), null, firstName);
		//
		identity.setUsername(username);
		identity = identityService.save(identity);
		//
		// identity hasn't equals firstname (added new rule)
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		identity.setFirstName(firstName);
		identity = identityService.save(identity);
		//
		// identity has equals all
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		identity.setFirstName(firstName + "1");
		identity = identityService.save(identity);
		//
		// identity hasn't equals firstname
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		automaticRoleAttributeRuleService.delete(firstNameRule);
		//
		// must recalculate
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		this.recalculateSync(automaticRole.getId());
		//
		// after recalculate is identity passed all rules
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testSameRoleAssignedMoreThanOne() {
		String testEmail = "testing-email-" + System.currentTimeMillis() + "@example.tld";
		String testDescription = "testing-description-" + System.currentTimeMillis();
		//
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		//
		getHelper().createIdentityRole(primeContract, role);
		//
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		IdmAutomaticRoleAttributeDto automaticRole2 = getHelper().createAutomaticRole(role.getId());
		//
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.email.getName(), null, testEmail);
		//
		getHelper().createAutomaticRoleRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, testDescription);
		//
		// only one role, assigned by direct add
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRole = identityRoles.get(0);
		assertNull(identityRole.getAutomaticRole());
		assertEquals(role.getId(), identityRole.getRole());
		//
		identity.setEmail(testEmail);
		identity = identityService.save(identity);
		//
		// 1 classic role, 1 automatic role
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());
		int classicRoleCount = 0;
		for (IdmIdentityRoleDto idenityRole : identityRoles) {
			assertEquals(role.getId(), idenityRole.getRole());
			if (idenityRole.getAutomaticRole() == null) {
				classicRoleCount++;
			}
		}
		// must exist one classic role
		assertEquals(1, classicRoleCount);
		//
		identity.setDescription(testDescription);
		identity = identityService.save(identity);
		//
		// 1 classic role, 2 automatic role
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(3, identityRoles.size());
		classicRoleCount = 0;
		for (IdmIdentityRoleDto idenityRole : identityRoles) {
			assertEquals(role.getId(), idenityRole.getRole());
			if (idenityRole.getAutomaticRole() == null) {
				classicRoleCount++;
			}
		}
		// must exist one classic role
		assertEquals(1, classicRoleCount);
		//
		identity.setEmail(null);
		identity = identityService.save(identity);
		// 1 classic role, 1 automatic role
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());
		//
		identityRoleService.delete(identityRole);
		//
		// 1 automatic role
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		// some databases padding/trim text (MsSQL)
		identity.setDescription(testDescription + " " + System.currentTimeMillis());
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleIntegerEav() {
		Integer testValue = 123456;
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.INT);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		getHelper().setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.INT);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleLongEav() {
		Long testValue = 123456l;
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.LONG);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		getHelper().setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.LONG);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDoubleEav() {
		Double testValue = 123456d;
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.DOUBLE);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		getHelper().setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.DOUBLE);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDateEav() {
		LocalDate testValue = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1514764800), ZoneId.systemDefault()).toLocalDate();
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.DATE);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		getHelper().setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.DATE);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDateTimeEav() {
		ZonedDateTime testValue = ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.DATETIME);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		getHelper().setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.DATETIME);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleBooleanEav() {
		Boolean testValue = true;
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.BOOLEAN);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		getHelper().setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.BOOLEAN);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDisableAttribute() {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.disabled.getName(), null, "true");
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		identity.setState(IdentityState.DISABLED);
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleContractExterneAttribute() {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.externe.getName(), null, "true");
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		primeContract.setExterne(true);
		primeContract = identityContractService.save(primeContract);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		primeContract.setExterne(false);
		primeContract = identityContractService.save(primeContract);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleContractExterneAttributeCodelist() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		//
		IdmFormAttributeDto environmentAttribute = new IdmFormAttributeDto();
		environmentAttribute.setCode(getHelper().createName());
		environmentAttribute.setName(getHelper().createName());
		environmentAttribute.setPersistentType(PersistentType.CODELIST);
		environmentAttribute.setFaceType(BaseCodeList.ENVIRONMENT);
		IdmFormDefinitionDto formDefinitionCodeList = formService.getDefinition(IdmIdentityContract.class);
		environmentAttribute = formService.saveAttribute(IdmIdentityContract.class, environmentAttribute);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, environmentAttribute.getCode(), environmentAttribute.getId(), "test");
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		IdmFormValueDto value = new IdmFormValueDto(environmentAttribute);
		value.setValue("test");
		formService.saveValues(primeContract, formDefinitionCodeList, Lists.newArrayList(value));
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		value.setValue(null);
		formService.saveValues(primeContract, formDefinitionCodeList, Lists.newArrayList(value));
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleContractExterneAttributeEnumeration() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		//
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		attribute.setCode(getHelper().createName());
		attribute.setName(getHelper().createName());
		attribute.setPersistentType(PersistentType.ENUMERATION);
		attribute.setFaceType(BaseFaceType.OPERATION_STATE_ENUM);
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentityContract.class);
		attribute = formService.saveAttribute(IdmIdentityContract.class, attribute);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute.getCode(), attribute.getId(), "test");
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		IdmFormValueDto value = new IdmFormValueDto(attribute);
		value.setValue("test");
		formService.saveValues(primeContract, formDefinition, Lists.newArrayList(value));
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		value.setValue(null);
		formService.saveValues(primeContract, formDefinition, Lists.newArrayList(value));
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleContractState() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRole.getId(), 
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, 
				IdmIdentityContract_.state.getName(), 
				null, 
				ContractState.EXCLUDED.name());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		primeContract.setState(ContractState.EXCLUDED);
		primeContract = identityContractService.save(primeContract);
		
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		primeContract.setState(null);
		primeContract = identityContractService.save(primeContract);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleContractMainAttribute() {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.main.getName(), null, "false");
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		primeContract.setMain(false);
		primeContract = identityContractService.save(primeContract);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		primeContract.setMain(true);
		primeContract = identityContractService.save(primeContract);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testTwoAutomaticRoleMoreRules() {
		String testEmail = "testing-email-" + System.currentTimeMillis() + "@example.tld";
		String testEavContractValue = "testing-eav-value-" + System.currentTimeMillis();
		String testEavIdentityValue = "testing-eav-value-" + System.currentTimeMillis();
		String testPositionName = "testing-position-name-" + System.currentTimeMillis();
		//
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto role2 = getHelper().createRole();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		//
		// create two eav attributes (for identity and contract)
		IdmFormAttributeDto createEavAttribute = 	getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.SHORTTEXT);
		getHelper().setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testEavContractValue + "-not-passed", PersistentType.SHORTTEXT);
		IdmFormAttributeDto createEavAttribute2 = 	getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.SHORTTEXT);
		getHelper().setEavValue(identity, createEavAttribute2, IdmIdentity.class, testEavIdentityValue + "-not-passed", PersistentType.SHORTTEXT);
		//
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		IdmAutomaticRoleAttributeDto automaticRole2 = getHelper().createAutomaticRole(role2.getId());
		//
		// rules for first automatic role
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.email.getName(), null, testEmail);
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, createEavAttribute.getId(), testEavContractValue);
		//
		// rules for second automatic role
		getHelper().createAutomaticRoleRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), null, testPositionName);
		getHelper().createAutomaticRoleRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute2.getId(), testEavIdentityValue);
		//
		// rules are not passed
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// set attribute for only one part from each rules
		primeContract.setPosition(testPositionName);
		primeContract = identityContractService.save(primeContract);
		identity.setEmail(testEmail);
		identity = identityService.save(identity);
		//
		// still zero, only one part of rules are passed
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		getHelper().setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testEavContractValue, PersistentType.SHORTTEXT);
		//
		// one automatic roles has passed all rules
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRole = identityRoles.get(0);
		assertEquals(automaticRole.getRole(), identityRole.getRole());
		assertEquals(automaticRole.getId(), identityRole.getAutomaticRole());
		//
		identity.setEmail(testEmail + "-not-passed");
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		getHelper().setEavValue(identity, createEavAttribute2, IdmIdentity.class, testEavIdentityValue, PersistentType.SHORTTEXT);
		// passed second automatic role
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		identityRole = identityRoles.get(0);
		assertEquals(automaticRole2.getRole(), identityRole.getRole());
		assertEquals(automaticRole2.getId(), identityRole.getAutomaticRole());
		//
		identity.setEmail(testEmail);
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());
		for (IdmIdentityRoleDto identityRol : identityRoles) {
			if (identityRol.getRole().equals(role.getId())) {
				assertEquals(automaticRole.getRole(), identityRol.getRole());
				assertEquals(automaticRole.getId(), identityRol.getAutomaticRole());
			} else {
				assertEquals(automaticRole2.getRole(), identityRol.getRole());
				assertEquals(automaticRole2.getId(), identityRol.getAutomaticRole());
			}
		}
		//
		// try delete
		automaticRoleAttributeService.delete(automaticRole2);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		identityRole = identityRoles.get(0);
		assertEquals(automaticRole.getRole(), identityRole.getRole());
		assertEquals(automaticRole.getId(), identityRole.getAutomaticRole());
		//
		automaticRoleAttributeService.delete(automaticRole);
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testRemoveLastRuleWithCheck() {
		String eavCode = "testingEav";
		Long testEavContractValue = System.currentTimeMillis();
		UUID testEavIdentityValue = UUID.randomUUID();
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		
		// create two eav attributes (for identity and contract)
		IdmFormAttributeDto eavAttributeIdentity = getHelper().createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentity.class, PersistentType.UUID);
		getHelper().setEavValue(identity, eavAttributeIdentity, IdmIdentity.class, testEavIdentityValue, PersistentType.UUID);
		IdmFormAttributeDto eavAttributeContract = 	getHelper().createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.LONG);
		getHelper().setEavValue(primeContract, eavAttributeContract, IdmIdentityContract.class, testEavContractValue, PersistentType.LONG);

		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		IdmAutomaticRoleAttributeRuleDto rule1 = getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, eavAttributeIdentity.getId(), testEavIdentityValue.toString());
		IdmAutomaticRoleAttributeRuleDto rule2 = getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract.getId(), testEavContractValue.toString());
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		
		this.recalculateSync(automaticRole.getId());
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		
		automaticRoleAttributeRuleService.delete(rule1);
		
		this.recalculateSync(automaticRole.getId());
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		
		// in process will be start LRT with async remove all identity roles
		automaticRoleAttributeRuleService.delete(rule2);
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testRemoveLastRuleWithoutCheck() {
		String eavCode = "testingEav";
		Long testEavIdentityValue = System.currentTimeMillis();
		UUID testEavContractValue = UUID.randomUUID();
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		
		// create two eav attributes (for identity and contract)
		IdmFormAttributeDto eavAttributeIdentity = getHelper().createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentity.class, PersistentType.LONG);
		getHelper().setEavValue(identity, eavAttributeIdentity, IdmIdentity.class, testEavIdentityValue, PersistentType.LONG);
		IdmFormAttributeDto eavAttributeContract = 	getHelper().createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.UUID);
		getHelper().setEavValue(primeContract, eavAttributeContract, IdmIdentityContract.class, testEavContractValue, PersistentType.UUID);

		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		IdmAutomaticRoleAttributeRuleDto rule1 = getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, eavAttributeIdentity.getId(), testEavIdentityValue.toString());
		IdmAutomaticRoleAttributeRuleDto rule2 = getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract.getId(), testEavContractValue.toString());
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		
		this.recalculateSync(automaticRole.getId());
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		
		automaticRoleAttributeRuleService.delete(rule1);
		
		this.recalculateSync(automaticRole.getId());
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		
		automaticRoleAttributeRuleService.delete(rule2);
		
		// in this case we not able remove the last automatic role from identity
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testMoreContracts() {
		UUID testValue1 = UUID.randomUUID();
		Long testValue2 = System.currentTimeMillis();
		Boolean testValue3 = Boolean.FALSE;
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity, null, null, LocalDate.now().plusDays(2));
		IdmIdentityContractDto contract3 = getHelper().createIdentityContact(identity, null, LocalDate.now().minusDays(5), LocalDate.now().plusDays(5));
		
		IdmIdentityContractDto primeContractCheck = getHelper().getPrimeContract(identity.getId());
		assertEquals(primeContract.getId(), primeContractCheck.getId());
		
		IdmFormAttributeDto eavAttributeContract1 = getHelper().createEavAttribute(getHelper().createName(), IdmIdentityContract.class, PersistentType.UUID);
		getHelper().setEavValue(primeContract, eavAttributeContract1, IdmIdentityContract.class, testValue1, PersistentType.UUID);
		
		IdmFormAttributeDto eavAttributeContract2 = getHelper().createEavAttribute(getHelper().createName(), IdmIdentityContract.class, PersistentType.LONG);
		getHelper().setEavValue(contract2, eavAttributeContract2, IdmIdentityContract.class, testValue2, PersistentType.LONG);
		
		IdmFormAttributeDto eavAttributeContract3 = getHelper().createEavAttribute(getHelper().createName(), IdmIdentityContract.class, PersistentType.BOOLEAN);
		getHelper().setEavValue(contract3, eavAttributeContract3, IdmIdentityContract.class, testValue3, PersistentType.BOOLEAN);
		
		IdmRoleDto role1 = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole1 = getHelper().createAutomaticRole(role1.getId());
		getHelper().createAutomaticRoleRule(automaticRole1.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract1.getId(), testValue1.toString());
		
		IdmRoleDto role2 = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole2 = getHelper().createAutomaticRole(role2.getId());
		getHelper().createAutomaticRoleRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract2.getId(), testValue2.toString());
		
		IdmRoleDto role3 = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole3 = getHelper().createAutomaticRole(role3.getId());
		getHelper().createAutomaticRoleRule(automaticRole3.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract3.getId(), testValue3.toString());
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		
		this.recalculateSync(automaticRole1.getId());
		this.recalculateSync(automaticRole2.getId());
		this.recalculateSync(automaticRole3.getId());
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(3, identityRoles.size());
		
		identityContractService.delete(contract3);
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());
		
		identityContractService.delete(contract2);
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		
		identityContractService.delete(primeContract);
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testDeletePrimaryContract() {
		UUID testValue1 = UUID.randomUUID();
		Long testValue2 = System.currentTimeMillis();
		Boolean testValue3 = Boolean.FALSE;
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity, null, null, LocalDate.now().plusDays(2));
		IdmIdentityContractDto contract3 = getHelper().createIdentityContact(identity, null, LocalDate.now().minusDays(2), LocalDate.now().plusDays(2));
		
		IdmIdentityContractDto primeContractCheck = getHelper().getPrimeContract(identity.getId());
		assertEquals(primeContract.getId(), primeContractCheck.getId());
		
		IdmFormAttributeDto eavAttributeContract1 = getHelper().createEavAttribute(getHelper().createName(), IdmIdentityContract.class, PersistentType.UUID);
		getHelper().setEavValue(primeContract, eavAttributeContract1, IdmIdentityContract.class, testValue1, PersistentType.UUID);
		
		IdmFormAttributeDto eavAttributeContract2 = getHelper().createEavAttribute(getHelper().createName(), IdmIdentityContract.class, PersistentType.LONG);
		getHelper().setEavValue(contract2, eavAttributeContract2, IdmIdentityContract.class, testValue2, PersistentType.LONG);
		
		IdmFormAttributeDto eavAttributeContract3 = getHelper().createEavAttribute(getHelper().createName(), IdmIdentityContract.class, PersistentType.BOOLEAN);
		getHelper().setEavValue(contract3, eavAttributeContract3, IdmIdentityContract.class, testValue3, PersistentType.BOOLEAN);
		
		IdmRoleDto role1 = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole1 = getHelper().createAutomaticRole(role1.getId());
		getHelper().createAutomaticRoleRule(automaticRole1.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract1.getId(), testValue1.toString());
		
		IdmRoleDto role2 = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole2 = getHelper().createAutomaticRole(role2.getId());
		getHelper().createAutomaticRoleRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract2.getId(), testValue2.toString());
		
		IdmRoleDto role3 = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole3 = getHelper().createAutomaticRole(role3.getId());
		getHelper().createAutomaticRoleRule(automaticRole3.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract3.getId(), testValue3.toString());
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		
		this.recalculateSync(automaticRole1.getId());
		this.recalculateSync(automaticRole2.getId());
		this.recalculateSync(automaticRole3.getId());
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(3, identityRoles.size());
		
		boolean contractCheck1 = false;
		boolean contractCheck2 = false;
		boolean contractCheck3 = false;
		//
		for (IdmIdentityRoleDto identityRole: identityRoles) {
			if (identityRole.getIdentityContract().equals(primeContract.getId())) {
				contractCheck1 = true;
			}
			if (identityRole.getIdentityContract().equals(contract2.getId())) {
				contractCheck2 = true;
			}
			if (identityRole.getIdentityContract().equals(contract3.getId())) {
				contractCheck3 = true;
			}
		}
		assertTrue(contractCheck1);
		assertTrue(contractCheck2);
		assertTrue(contractCheck3);
		
		identityContractService.delete(primeContract);
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());
		
		contractCheck1 = false;
		contractCheck2 = false;
		contractCheck3 = false;
		//
		for (IdmIdentityRoleDto identityRole: identityRoles) {
			if (identityRole.getIdentityContract().equals(primeContract.getId())) {
				contractCheck1 = true;
			}
			if (identityRole.getIdentityContract().equals(contract2.getId())) {
				contractCheck2 = true;
			}
			if (identityRole.getIdentityContract().equals(contract3.getId())) {
				contractCheck3 = true;
			}
		}
		assertFalse(contractCheck1);
		assertTrue(contractCheck2);
		assertTrue(contractCheck3);

		IdmIdentityContractDto newPrimeContract = getHelper().getPrimeContract(identity.getId());
		assertNotEquals(primeContract.getId(), newPrimeContract.getId());
		
		identityContractService.delete(newPrimeContract);
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		
		contractCheck1 = false;
		contractCheck2 = false;
		contractCheck3 = false;
		//
		for (IdmIdentityRoleDto identityRole: identityRoles) {
			if (identityRole.getIdentityContract().equals(primeContract.getId())) {
				contractCheck1 = true;
			}
			if (identityRole.getIdentityContract().equals(contract2.getId())) {
				contractCheck2 = true;
			}
			if (identityRole.getIdentityContract().equals(contract3.getId())) {
				contractCheck3 = true;
			}
		}
		assertFalse(contractCheck1);
		assertFalse(contractCheck2);
		assertTrue(contractCheck3);
		
		IdmIdentityContractDto newNewPrimeContract = getHelper().getPrimeContract(identity.getId());
		assertNotEquals(newPrimeContract.getId(), newNewPrimeContract.getId());
		
		assertEquals(contract3, newNewPrimeContract);
		
		identityContractService.delete(newNewPrimeContract);
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testTextTypeInEav() {
		String testValue = "123!@#" + System.currentTimeMillis();
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.TEXT);
		getHelper().setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.TEXT);
		//
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue);
	}
	
	@Test
	public void testEmptyIdentityEav() {
		String testDescription = "testDescription-" + System.currentTimeMillis();
		String eavCode = "eavCode-test-";
		//
		IdmIdentityDto identity1 = getHelper().createIdentity();
		IdmIdentityDto identity2 = getHelper().createIdentity();
		IdmIdentityDto identity3 = getHelper().createIdentity();
		
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(identity1.getId());
		IdmIdentityContractDto contract2 = getHelper().getPrimeContract(identity2.getId());
		IdmIdentityContractDto contract3 = getHelper().getPrimeContract(identity3.getId());
		
		contract1.setDescription(testDescription);
		contract2.setDescription(testDescription);
		contract3.setDescription(testDescription);
		
		contract1 = identityContractService.save(contract1);
		contract2 = identityContractService.save(contract2);
		contract3 = identityContractService.save(contract3);
		
		IdmFormAttributeDto eavAttribute = getHelper().createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentity.class, PersistentType.BOOLEAN);
		getHelper().setEavValue(identity1, eavAttribute, IdmIdentity.class, Boolean.TRUE, PersistentType.BOOLEAN);
		
		IdmRoleDto role1 = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role1.getId());
		
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.description.getName(), null, testDescription);
		
		List<IdmIdentityRoleDto> roles1 = identityRoleService.findAllByIdentity(identity1.getId());
		List<IdmIdentityRoleDto> roles2 = identityRoleService.findAllByIdentity(identity2.getId());
		List<IdmIdentityRoleDto> roles3 = identityRoleService.findAllByIdentity(identity3.getId());
		
		assertEquals(0, roles1.size());
		assertEquals(0, roles2.size());
		assertEquals(0, roles3.size());
		
		this.recalculateSync(automaticRole.getId());
		
		roles1 = identityRoleService.findAllByIdentity(identity1.getId());
		roles2 = identityRoleService.findAllByIdentity(identity2.getId());
		roles3 = identityRoleService.findAllByIdentity(identity3.getId());
		
		assertEquals(1, roles1.size());
		assertEquals(1, roles2.size());
		assertEquals(1, roles3.size());
		
		IdmAutomaticRoleAttributeRuleDto rule = getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, eavAttribute.getId(), Boolean.TRUE.toString());
		
		this.recalculateSync(automaticRole.getId());
		
		roles1 = identityRoleService.findAllByIdentity(identity1.getId());
		roles2 = identityRoleService.findAllByIdentity(identity2.getId());
		roles3 = identityRoleService.findAllByIdentity(identity3.getId());
		
		assertEquals(1, roles1.size());
		assertEquals(0, roles2.size());
		assertEquals(0, roles3.size());
		
		automaticRoleAttributeRuleService.delete(rule);
		
		this.recalculateSync(automaticRole.getId());
		
		roles1 = identityRoleService.findAllByIdentity(identity1.getId());
		roles2 = identityRoleService.findAllByIdentity(identity2.getId());
		roles3 = identityRoleService.findAllByIdentity(identity3.getId());
		
		assertEquals(1, roles1.size());
		assertEquals(1, roles2.size());
		assertEquals(1, roles3.size());
	}
	
	@Test
	public void testExpiredContract() {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, identity.getUsername());
		//
		this.recalculateSync(automaticRole.getId());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityContractDto expiredContract = getHelper().createIdentityContact(identity, null, LocalDate.now().minusDays(10), LocalDate.now().minusDays(5));
		// we must save identity, automatic role will be recalculate after identity save
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByContract(expiredContract.getId());
		assertEquals(0, identityRoles.size());
		//
		expiredContract.setValidTill(LocalDate.now().plusDays(100));
		expiredContract = identityContractService.save(expiredContract);
		// we must save identity, automatic role will be recalculate after identity save
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByContract(getHelper().getPrimeContract(identity.getId()).getId());
		assertEquals(1, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());
		//
		expiredContract.setValidTill(LocalDate.now().minusDays(2));
		expiredContract = identityContractService.save(expiredContract);
		// we must save identity, automatic role will be recalculate after identity save
		identity = identityService.save(identity);
		identityRoles = identityRoleService.findAllByContract(expiredContract.getId());
		assertEquals(0, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByContract(expiredContract.getId());
		assertEquals(0, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testFutureValidContract() {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, identity.getUsername());
		//
		this.recalculateSync(automaticRole.getId());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityContractDto futureValidContract = getHelper().createIdentityContact(identity, null, LocalDate.now().plusDays(10), LocalDate.now().plusDays(50));
		// we must save identity, automatic role will be recalculate after identity save
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByContract(futureValidContract.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testDisabledContract() {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity, null, LocalDate.now().minusMonths(5), LocalDate.now().plusMonths(5));
		contract2.setState(ContractState.DISABLED);
		contract2 = identityContractService.save(contract2);
		//
		IdmIdentityContractDto contract3 = getHelper().createIdentityContact(identity, null, null, LocalDate.now().plusMonths(5));
		contract3.setState(ContractState.DISABLED);
		contract3 = identityContractService.save(contract3);
		//
		IdmIdentityContractDto contract4 = getHelper().createIdentityContact(identity, null, null, null);
		contract4.setState(ContractState.DISABLED);
		contract4 = identityContractService.save(contract4);
		//
		IdmIdentityContractDto contract5 = getHelper().createIdentityContact(identity, null, LocalDate.now().minusMonths(5), null);
		contract5.setState(ContractState.DISABLED);
		contract5 = identityContractService.save(contract5);
		//
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, identity.getUsername());
		//
		this.recalculateSync(automaticRole.getId());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByContract(contract2.getId());
		assertEquals(0, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByContract(contract3.getId());
		assertEquals(0, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByContract(contract4.getId());
		assertEquals(0, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByContract(contract5.getId());
		assertEquals(0, identityRoles.size());
		//
		contract5.setState(null);
		contract5 = identityContractService.save(contract5);
		// we must save identity, automatic role will be recalculate after identity save
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByContract(contract5.getId());
		assertEquals(1, identityRoles.size());
		//
		contract4.setState(null);
		contract4 = identityContractService.save(contract4);
		// we must save identity, automatic role will be recalculate after identity save
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByContract(contract4.getId());
		assertEquals(1, identityRoles.size());
		//
		contract3.setState(null);
		contract3 = identityContractService.save(contract3);
		// we must save identity, automatic role will be recalculate after identity save
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByContract(contract3.getId());
		assertEquals(1, identityRoles.size());
		//
		contract2.setState(null);
		contract2 = identityContractService.save(contract2);
		// we must save identity, automatic role will be recalculate after identity save
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByContract(contract2.getId());
		assertEquals(1, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		for (IdmIdentityRoleDto identityRole : identityRoles) {
			assertEquals(automaticRole.getId(), identityRole.getAutomaticRole());
			AbstractIdmAutomaticRoleDto embedded = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.automaticRole, (AbstractIdmAutomaticRoleDto) null);
			assertEquals(automaticRole, embedded);
			assertEquals(role.getId(), embedded.getRole());
			assertEquals(role.getId(), identityRole.getRole());
		}
		//
		contract3.setState(ContractState.DISABLED);
		contract3 = identityContractService.save(contract3);
		// we must save identity, automatic role will be recalculate after identity save
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByContract(contract3.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testUpdateWithoutAutomaticRoles() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		
		IdmRoleDto basicRole = getHelper().createRole();
		getHelper().assignRoles(primeContract, basicRole);
		
		IdmTreeTypeDto type = getHelper().createTreeType();
		IdmTreeNodeDto node = getHelper().createTreeNode(type, null);
		
		primeContract.setWorkPosition(node.getId());
		identity.setDescription(String.valueOf(System.currentTimeMillis()));
		
		identityContractService.save(primeContract);
		
		identityService.save(identity);
	}

	@Test
	public void testChangeIndetityStateToInvalid() {
		String testValue = "test-value-" + System.currentTimeMillis();
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());

		IdmFormAttributeDto createEavAttributeIdentity = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.SHORTTEXT);
		getHelper().setEavValue(identity, createEavAttributeIdentity, IdmIdentity.class, testValue + "123", PersistentType.SHORTTEXT);

		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRoleIdentityEav = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRoleIdentityEav.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttributeIdentity.getId(), testValue);
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		
		getHelper().setEavValue(identity, createEavAttributeIdentity, IdmIdentity.class, testValue, PersistentType.SHORTTEXT);
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertEquals(role.getId(), identityRoleDto.getRole());

		primeContract.setValidTill(LocalDate.now().minusDays(5));
		primeContract = identityContractService.save(primeContract);

		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}

	@Test
	public void testChangeIndetityStateToValidSetValueBefore() {
		String testValue = "test-value-" + System.currentTimeMillis();
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		primeContract.setValidTill(LocalDate.now().minusDays(5));
		primeContract = identityContractService.save(primeContract);

		// set value before create automatic role
		IdmFormAttributeDto createEavAttributeIdentity = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.SHORTTEXT);
		getHelper().setEavValue(identity, createEavAttributeIdentity, IdmIdentity.class, testValue, PersistentType.SHORTTEXT);

		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRoleIdentityEav = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRoleIdentityEav.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttributeIdentity.getId(), testValue);
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		
		primeContract.setValidTill(null);
		primeContract = identityContractService.save(primeContract);

		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertEquals(role.getId(), identityRoleDto.getRole());
	}

	/**
	 * Difference between method testChangeIndetityStateToValidSetValueBefore and
	 * testChangeIndetityStateToValidSetValueAfter is with set eav value for identity before and after create automatic role
	 */
	@Test
	public void testChangeIndetityStateToValidSetValueAfter() {
		String testValue = "test-value-" + System.currentTimeMillis();
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		primeContract.setValidTill(LocalDate.now().minusDays(5));
		primeContract = identityContractService.save(primeContract);

		IdmFormAttributeDto createEavAttributeIdentity = getHelper().createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.SHORTTEXT);

		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRoleIdentityEav = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRoleIdentityEav.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttributeIdentity.getId(), testValue);
		
		// set value after create automatic role
		getHelper().setEavValue(identity, createEavAttributeIdentity, IdmIdentity.class, testValue, PersistentType.SHORTTEXT);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		
		primeContract.setValidTill(null);
		primeContract = identityContractService.save(primeContract);

		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertEquals(role.getId(), identityRoleDto.getRole());
	}

	@Test
	public void testChangeIndetityStateMoreRoles() {
		String testValueIdentityEav = "test-value-identityEav-" + System.currentTimeMillis();
		String testValueIdentityContractEav = "test-value-identityContractEav-" + System.currentTimeMillis();
		String testValueIdentityDescription = "test-value-identityDescription-" + System.currentTimeMillis();
		String testValueIdentityContractDescription = "test-value-identityContractDescription-" + System.currentTimeMillis();
		
		IdmIdentityDto identity = getHelper().createIdentity();
		identity.setDescription(testValueIdentityDescription);
		identity = identityService.save(identity);
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		primeContract.setValidTill(LocalDate.now().minusDays(5));
		primeContract.setDescription(testValueIdentityContractDescription);
		primeContract = identityContractService.save(primeContract);

		// set value for identity
		IdmFormAttributeDto createEavAttributeIdentity = getHelper().createEavAttribute("testingEavIdentity" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.SHORTTEXT);
		getHelper().setEavValue(identity, createEavAttributeIdentity, IdmIdentity.class, testValueIdentityEav, PersistentType.SHORTTEXT);

		// set value for contract
		IdmFormAttributeDto createEavAttributeIdentityContract = getHelper().createEavAttribute("testingEavContract" + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.SHORTTEXT);
		getHelper().setEavValue(primeContract, createEavAttributeIdentityContract, IdmIdentityContract.class, testValueIdentityContractEav, PersistentType.SHORTTEXT);

		IdmRoleDto roleIdentityEav = getHelper().createRole();
		IdmRoleDto roleIdentityContractEav = getHelper().createRole();
		IdmRoleDto roleIdentityContractDescription = getHelper().createRole();
		IdmRoleDto roleIdentityDescription = getHelper().createRole();

		IdmAutomaticRoleAttributeDto automaticRoleIdentityEav = getHelper().createAutomaticRole(roleIdentityEav.getId());
		getHelper().createAutomaticRoleRule(automaticRoleIdentityEav.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttributeIdentity.getId(), testValueIdentityEav);

		IdmAutomaticRoleAttributeDto automaticRoleIdentityContractEav = getHelper().createAutomaticRole(roleIdentityContractEav.getId());
		getHelper().createAutomaticRoleRule(automaticRoleIdentityContractEav.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, createEavAttributeIdentityContract.getId(), testValueIdentityContractEav);
		
		IdmAutomaticRoleAttributeDto automaticRoleIdentityContractDescription = getHelper().createAutomaticRole(roleIdentityContractDescription.getId());
		getHelper().createAutomaticRoleRule(automaticRoleIdentityContractDescription.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.description.getName(), null, testValueIdentityContractDescription);
		
		IdmAutomaticRoleAttributeDto automaticRoleIdentityDescription = getHelper().createAutomaticRole(roleIdentityDescription.getId());
		getHelper().createAutomaticRoleRule(automaticRoleIdentityDescription.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, testValueIdentityDescription);

		this.recalculateSync(automaticRoleIdentityEav.getId());
		this.recalculateSync(automaticRoleIdentityContractEav.getId());
		this.recalculateSync(automaticRoleIdentityContractDescription.getId());
		this.recalculateSync(automaticRoleIdentityDescription.getId());

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		
		primeContract.setValidTill(null);
		primeContract = identityContractService.save(primeContract);

		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(4, identityRoles.size());

		boolean isRoleFromIdentityEav = false;
		boolean isRoleFromIdentityContractEav = false;
		boolean isRoleFromIdentityDescription = false;
		boolean isRoleFromIdentityContractDescription = false;
		for (IdmIdentityRoleDto identityRole : identityRoles) {
			if (identityRole.getRole().equals(roleIdentityEav.getId())) {
				isRoleFromIdentityEav = true;
			} else if (identityRole.getRole().equals(roleIdentityContractEav.getId())) {
				isRoleFromIdentityContractEav = true;
			} else if (identityRole.getRole().equals(roleIdentityContractDescription.getId())) {
				isRoleFromIdentityDescription = true;
			} else if (identityRole.getRole().equals(roleIdentityDescription.getId())) {
				isRoleFromIdentityContractDescription = true;
			} else {
				fail();
			}
		}

		assertTrue(isRoleFromIdentityEav);
		assertTrue(isRoleFromIdentityContractEav);
		assertTrue(isRoleFromIdentityDescription);
		assertTrue(isRoleFromIdentityContractDescription);

		primeContract.setDescription("newValue");
		primeContract = identityContractService.save(primeContract);

		this.recalculateSync(automaticRoleIdentityEav.getId());
		this.recalculateSync(automaticRoleIdentityContractEav.getId());
		this.recalculateSync(automaticRoleIdentityContractDescription.getId());
		this.recalculateSync(automaticRoleIdentityDescription.getId());

		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(3, identityRoles.size());

		isRoleFromIdentityEav = false;
		isRoleFromIdentityContractEav = false;
		isRoleFromIdentityDescription = false;
		isRoleFromIdentityContractDescription = false;
		for (IdmIdentityRoleDto identityRole : identityRoles) {
			if (identityRole.getRole().equals(roleIdentityEav.getId())) {
				isRoleFromIdentityEav = true;
			} else if (identityRole.getRole().equals(roleIdentityContractEav.getId())) {
				isRoleFromIdentityContractEav = true;
			} else if (identityRole.getRole().equals(roleIdentityContractDescription.getId())) {
				isRoleFromIdentityContractDescription = true;
			} else if (identityRole.getRole().equals(roleIdentityDescription.getId())) {
				isRoleFromIdentityDescription = true;
			} else {
				fail();
			}
		}

		assertTrue(isRoleFromIdentityEav);
		assertTrue(isRoleFromIdentityContractEav);
		assertTrue(isRoleFromIdentityDescription);
		assertFalse(isRoleFromIdentityContractDescription);

		primeContract.setValidTill(LocalDate.now().minusDays(5));
		primeContract = identityContractService.save(primeContract);

		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());

		primeContract.setValidTill(LocalDate.now().plusDays(10));
		primeContract = identityContractService.save(primeContract);

		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(3, identityRoles.size());

		isRoleFromIdentityEav = false;
		isRoleFromIdentityContractEav = false;
		isRoleFromIdentityDescription = false;
		isRoleFromIdentityContractDescription = false;
		for (IdmIdentityRoleDto identityRole : identityRoles) {
			if (identityRole.getRole().equals(roleIdentityEav.getId())) {
				isRoleFromIdentityEav = true;
			} else if (identityRole.getRole().equals(roleIdentityContractEav.getId())) {
				isRoleFromIdentityContractEav = true;
			} else if (identityRole.getRole().equals(roleIdentityContractDescription.getId())) {
				isRoleFromIdentityContractDescription = true;
			} else if (identityRole.getRole().equals(roleIdentityDescription.getId())) {
				isRoleFromIdentityDescription = true;
			} else {
				fail();
			}
		}

		assertTrue(isRoleFromIdentityEav);
		assertTrue(isRoleFromIdentityContractEav);
		assertTrue(isRoleFromIdentityDescription);
		assertFalse(isRoleFromIdentityContractDescription);
	}

	@Test
	public void testRecalculationWithManyIdentities() {
		String description = getHelper().createName();
		List<IdmIdentityDto> identities = new ArrayList<IdmIdentityDto>();
		try {
		
			for (int index = 0; index < 187; index++) {
				IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
				identity.setDescription(description);
				identityService.save(identity);
				identities.add(identity);
			}
			assertEquals(187, identities.size());
	
			IdmRoleDto role = getHelper().createRole();
			IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
			getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
					AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, description);
	
			this.recalculateSync(automaticRole.getId());
	
			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
			filter.setAutomaticRoleId(automaticRole.getId());
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null).getContent();
			assertEquals(187, identityRoles.size());
	
			for (IdmIdentityDto identity : identities) {
				List<IdmIdentityRoleDto> allByIdentity = identityRoleService.findAllByIdentity(identity.getId());
				assertEquals(1, allByIdentity.size());
			}
		} finally {
			identityService.deleteAll(identities);
		}
	}

	@Test
	public void testRecalculationWithManyIdentitiesProcessAll() {
		String description = getHelper().createName();
		List<IdmIdentityDto> identities = new ArrayList<IdmIdentityDto>();
		try {
			for (int index = 0; index < 241; index++) {
				IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
				identity.setDescription(description);
				identityService.save(identity);
				identities.add(identity);
			}
			assertEquals(241, identities.size());
	
			IdmRoleDto role = getHelper().createRole();
			IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
			getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
					AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, description);
	
			ProcessAllAutomaticRoleByAttributeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAllAutomaticRoleByAttributeTaskExecutor.class);
			longRunningTaskManager.executeSync(automaticRoleTask);
	
			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
			filter.setAutomaticRoleId(automaticRole.getId());
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null).getContent();
			assertEquals(241, identityRoles.size());
	
			for (IdmIdentityDto identity : identities) {
				List<IdmIdentityRoleDto> allByIdentity = identityRoleService.findAllByIdentity(identity.getId());
				assertEquals(1, allByIdentity.size());
			}
		} finally {
			identityService.deleteAll(identities);
		}
	}

	@Test
	public void testCountForDisabledContracts() {
		String description = getHelper().createName();
		List<IdmIdentityDto> identities = new ArrayList<IdmIdentityDto>();

		for (int index = 0; index < 141; index++) {
			IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
			identity.setDescription(description);
			identityService.save(identity);
			identities.add(identity);
		}
		assertEquals(141, identities.size());

		int actualCount = 0;
		for (IdmIdentityDto identity : identities) {
			actualCount++;

			// Identity have contract
			IdmIdentityContractDto contractDto = identityContractService.findAllByIdentity(identity.getId()).get(0);
			contractDto.setState(ContractState.DISABLED);
			identityContractService.save(contractDto);

			// Disable only first 30 identities
			if (actualCount == 30) {
				break;
			}
		}

		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, description);

		ProcessAutomaticRoleByAttributeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByAttributeTaskExecutor.class);
		automaticRoleTask.setAutomaticRoleId(automaticRole.getId());
		longRunningTaskManager.executeSync(automaticRoleTask);
		
		IdmLongRunningTaskDto task = longRunningTaskService.get(automaticRoleTask.getLongRunningTaskId());
		assertEquals(Long.valueOf(111), task.getCount());
		assertEquals(Long.valueOf(111), task.getCounter());
	}

	@Test
	public void testNotEqualsIdentity() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_EQUALS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setDescription(value);
		identity = identityService.save(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setDescription("123" + value);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testStartWithIdentity() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.START_WITH, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.firstName.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setFirstName(getHelper().createName());
		identity = identityService.save(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setFirstName(value + "123");
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setFirstName("123" + value);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testNotStartWithIdentity() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_START_WITH, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.lastName.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setLastName(getHelper().createName());
		identity = identityService.save(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setLastName(getHelper().createName() + value + getHelper().createName());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setLastName(value + getHelper().createName());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setLastName("666" + value);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testEndWithIdentity() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.END_WITH, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setUsername(getHelper().createName());
		identity = identityService.save(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setUsername("123" + value);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setUsername(value + "123");
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testNotEndWithIdentity() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_END_WITH, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.externalCode.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setExternalCode(value);
		identity = identityService.save(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setExternalCode(value + getHelper().createName());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setExternalCode(getHelper().createName() + value);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testIsEmptyIdentity() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.IS_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setDescription(null);
		identity = identityService.save(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setDescription(value);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setDescription(null);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setDescription(getHelper().createName());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setDescription("");
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setDescription(System.lineSeparator());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testIsNotEmptyIdentity() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setDescription(null);
		identity = identityService.save(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setDescription(value);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setDescription(null);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setDescription(getHelper().createName());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setDescription("");
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setDescription(System.lineSeparator());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testContainsIdentity() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.CONTAINS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.titleAfter.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setTitleAfter(value);
		identity = identityService.save(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setTitleAfter(getHelper().createName());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		
		identity.setTitleAfter(value + getHelper().createName());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setTitleAfter("");
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setTitleAfter("9585" + value + "549"); // Problem with size
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testNotContainsIdentity() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_CONTAINS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.titleBefore.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setTitleBefore(value);
		identity = identityService.save(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setTitleBefore(getHelper().createName());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		
		identity.setTitleBefore(value + getHelper().createName());
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setTitleBefore("");
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		identity.setTitleBefore("5111" + value + "42"); // Problem with max size this attribute
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		identity.setTitleAfter(null);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testEqualsContractBoolean() {
		String value = Boolean.TRUE.toString();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.externe.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setExterne(true);
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setExterne(false);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setExterne(true);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testNotEqualsContractBoolean() {
		String value = Boolean.FALSE.toString();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_EQUALS, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.externe.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setExterne(true);
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setExterne(false);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setExterne(true);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test(expected = ResultCodeException.class)
	public void testStartWithContractBoolean() {
		String value = Boolean.FALSE.toString();
		createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.START_WITH, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.externe.getName(), value);
	}

	@Test(expected = ResultCodeException.class)
	public void testNotStartWithContractBoolean() {
		String value = Boolean.TRUE.toString();
		createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_START_WITH, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.externe.getName(), value);
	}

	@Test(expected = ResultCodeException.class)
	public void testEndWithContractBoolean() {
		String value = Boolean.FALSE.toString();
		createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.END_WITH, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.externe.getName(), value);
	}

	@Test(expected = ResultCodeException.class)
	public void testNotEndWithContractBoolean() {
		String value = Boolean.TRUE.toString();
		createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_END_WITH, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.externe.getName(), value);
	}

	@Test(expected = ResultCodeException.class)
	public void testContainsContractBoolean() {
		String value = Boolean.TRUE.toString();
		createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.CONTAINS, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.main.getName(), value);
	}

	@Test(expected = ResultCodeException.class)
	public void testNotContainsContractBoolean() {
		String value = Boolean.FALSE.toString();
		createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_CONTAINS, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.main.getName(), value);
	}

	@Test
	public void testEqualsContract() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setPosition(getHelper().createName());
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition(getHelper().createName() + value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testNotEqualsContract() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_EQUALS, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setPosition(getHelper().createName());
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition(value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition(value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(null);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition("");
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testStartWithContract() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.START_WITH, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.description.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setDescription(value + getHelper().createName());
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setDescription(getHelper().createName() + value + getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setDescription(getHelper().createName() + value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setDescription(value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testNotStartWithContract() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_START_WITH, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.description.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setDescription(value + getHelper().createName());
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setDescription(getHelper().createName() + value + getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setDescription(getHelper().createName() + value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setDescription(value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setDescription("");
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setDescription(null);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testEndWithContract() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.END_WITH, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setPosition(value + getHelper().createName());
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(getHelper().createName() + value + getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(getHelper().createName() + value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition("");
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		
		contract.setPosition(value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition(null);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testNotEndWithContract() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_END_WITH, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setPosition(value + getHelper().createName());
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		
		contract.setPosition(getHelper().createName() + value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(getHelper().createName() + value + getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition("");
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		
		contract.setPosition(value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(null);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testIsEmptyContract() {
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.IS_EMPTY, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.description.getName(), null);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setDescription(getHelper().createName());
		contract = identityContractService.save(contract);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		
		contract.setDescription(null);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setDescription(getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setDescription("");
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testIsNotEmptyContract() {
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), null);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setPosition(getHelper().createName());
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		
		contract.setPosition("");
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition(null);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testContainsContract() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.CONTAINS, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setPosition(value + getHelper().createName());
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition(getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(getHelper().createName() + value + getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition("");
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(getHelper().createName() + value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition(null);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		
		contract.setPosition(value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testNotContainsContract() {
		String value = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison.NOT_CONTAINS, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setPosition(value + getHelper().createName());
		contract = identityContractService.save(contract);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition(getHelper().createName() + value + getHelper().createName());
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition("");
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		contract.setPosition(getHelper().createName() + value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition(null);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		
		contract.setPosition(value);
		contract = identityContractService.save(contract);
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testEqualsContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value));

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName()));
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testEqualsBooleanContractEav() {
		String value = Boolean.TRUE.toString();
		PersistentType persistentType = PersistentType.BOOLEAN;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value));

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(Boolean.FALSE.toString()));
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testEqualsMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, true);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value));

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName()));
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName(), getHelper().createName(), value, getHelper().createName()));
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testEqualsMultivaluedIntContractEav() {
		String value = "10";
		PersistentType persistentType = PersistentType.INT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, true);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(10));

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(20, 30, 40, 50, 60));
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(20, 30, 40, 50, 60, 10));
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testNotEqualsContractEav() {
		String value = "10";
		PersistentType persistentType = PersistentType.LONG;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_EQUALS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(10l));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(20l));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		
		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(10l));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test(expected = ResultCodeException.class)
	public void testNotEqualsMultivaluedContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_EQUALS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);
	}

	@Test
	public void testStartWithContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.START_WITH, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName()));
		recalculateSync(automaticRole.getId());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value + getHelper().createName()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		
		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName() + value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testNotStartWithContractEav() {
		String value = "4";
		PersistentType persistentType = PersistentType.DOUBLE;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_START_WITH, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(40.5));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(640.5));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(4.0));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		
		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(444.4));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(0));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testEndWithContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.END_WITH, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value + "a"));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName() + value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		
		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value.substring(0, 5)));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testNotEndWithContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_END_WITH, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value + "a"));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName() + value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		
		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(""));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value.substring(0, 5)));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testIsEmptyContractEav() {
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.IS_EMPTY, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, null);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList("00000"));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList("     "));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testIsNotEmptyContractEav() {
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, null);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(""));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList("00000"));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList("-"));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testContainsContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.CONTAINS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName() + value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(""));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName() + value + getHelper().createName()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testNotContainsContractEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_CONTAINS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName() + value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(""));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(getHelper().createName() + value + getHelper().createName()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testLessContainsContractEav() {
		String value = "10";
		PersistentType persistentType = PersistentType.LONG;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.LESS_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(10));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(9));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(11));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testGreaterContainsContractEav() {
		String value = "0";
		PersistentType persistentType = PersistentType.INT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.GREATER_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(10));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(9));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(-10));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(11));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(-1));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testGreaterAndLessContractEav() {
		String valueOne = "5";
		String valueTwo = "10";
		PersistentType persistentType = PersistentType.INT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.GREATER_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, valueOne);

		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.LESS_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, valueTwo);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		checkIdentityRoles(identity, 0);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(6));
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 1);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(2));
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(5));
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 1);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(-10));
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(10));
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 1);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testGreaterAndLessSameContractEav() {
		String valueOne = "5";
		String valueTwo = "5";
		PersistentType persistentType = PersistentType.LONG;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.GREATER_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, valueOne);

		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.LESS_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attribute, valueTwo);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		checkIdentityRoles(identity, 0);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(5));
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 1);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 1);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(2));
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList(6));
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attribute, persistentType, Lists.newArrayList());
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);
		recalculateAllSync(automaticRole);
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testEqualsIdentityMultivaluedEav() {
		String value = "10";
		PersistentType persistentType = PersistentType.LONG;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, true);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(10, 20, 30));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(9, 2, 3));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(11, 10, 12));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test(expected = ResultCodeException.class)
	public void testNotEqualsIdentityMultivaluedEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, true);
		createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_EQUALS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);
	}

	@Test
	public void testIsEmptyIdentityMultivaluedEav() {
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, true);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.IS_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, null);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(getHelper().createName()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		
		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(getHelper().createName(), getHelper().createName(), getHelper().createName()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testEqualsTrueIdentityMultivaluedEav() {
		String value = Boolean.TRUE.toString();
		PersistentType persistentType = PersistentType.BOOLEAN;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, true);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testEqualsIdentityEav() {
		String value = Boolean.FALSE.toString();
		PersistentType persistentType = PersistentType.BOOLEAN;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(Boolean.FALSE));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(Boolean.TRUE));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testNotEqualsIdentityEav() {
		String value = "150";
		PersistentType persistentType = PersistentType.LONG;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_EQUALS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(155));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testStartWithIdentityEav() {
		String value = "150";
		PersistentType persistentType = PersistentType.LONG;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.START_WITH, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(1505));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testNotStartWithIdentityEav() {
		String value = "150";
		PersistentType persistentType = PersistentType.LONG;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_START_WITH, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(1505));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(1150));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testEndWithIdentityEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.END_WITH, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(""));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(getHelper() + value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(value + getHelper()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testNotEndWithIdentityEav() {
		String value = getHelper().createName();
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_END_WITH, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(""));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(getHelper() + value));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(value + getHelper()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testIsEmptyIdentityEav() {
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.IS_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, null);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(getHelper().createName()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testIsNotEmptyIdentityEav() {
		PersistentType persistentType = PersistentType.SHORTTEXT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, null);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(getHelper().createName()));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(""));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testContainsIdentityEav() {
		String value = "666";
		PersistentType persistentType = PersistentType.LONG;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.CONTAINS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(666));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(123666123));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(1666));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(123));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(6662));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	@Test
	public void testNotContainsIdentityEav() {
		String value = "666";
		PersistentType persistentType = PersistentType.LONG;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.NOT_CONTAINS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(666));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(123666123));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(1666));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(123));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(6662));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testLessIdentityEav() {
		String value = "666";
		PersistentType persistentType = PersistentType.LONG;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.LESS_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(666));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(667));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(1666));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(123));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testGreaterIdentityEav() {
		String value = "666";
		PersistentType persistentType = PersistentType.INT;
		IdmFormAttributeDto attribute = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, persistentType, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.GREATER_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attribute, value);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(666));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(667));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(123));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attribute, persistentType, Lists.newArrayList(1666));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);


		saveEavValue(identity, attribute, persistentType, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testRuleCombinationInvalidOne() {
		IdmFormAttributeDto attributeOne = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, PersistentType.INT, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attributeOne, "666");
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.IS_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attributeOne, null);
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attributeOne, null);

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attributeOne, PersistentType.INT, Lists.newArrayList(666));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attributeOne, PersistentType.INT, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attributeOne, PersistentType.INT, Lists.newArrayList(667));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testRuleCombinationInvalidTwo() {
		IdmFormAttributeDto attributeOne = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, PersistentType.INT, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attributeOne, "666");
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attributeOne, null);

		IdmFormAttributeDto attributeTwo = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, PersistentType.SHORTTEXT, true);
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attributeTwo, "test");

		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.IS_EMPTY, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attributeTwo, null);
		
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attributeTwo, PersistentType.SHORTTEXT, Lists.newArrayList("test"));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attributeOne, PersistentType.INT, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attributeTwo, PersistentType.SHORTTEXT, Lists.newArrayList("test2"));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
	}

	@Test
	public void testRuleCombinationOne() {
		IdmFormAttributeDto attributeOne = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, PersistentType.INT, false);
		IdmAutomaticRoleAttributeDto automaticRole =
				createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attributeOne, "666");
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attributeOne, null);

		IdmFormAttributeDto attributeTwo = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, PersistentType.SHORTTEXT, true);
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attributeTwo, "teest");
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attributeTwo, null);

		IdmFormAttributeDto attributeThree = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, PersistentType.LONG, false);
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.LESS_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attributeThree, "10");
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.GREATER_THAN_OR_EQUAL, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attributeThree, "0");

		IdmFormAttributeDto attributeFour = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, PersistentType.SHORTTEXT, true);
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attributeFour, null);

		IdmFormAttributeDto attributeFive = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, PersistentType.LONG, true);
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attributeFive, null);

		IdmFormAttributeDto attributeSix = createFormAttribute(AutomaticRoleAttributeRuleType.IDENTITY_EAV, PersistentType.SHORTTEXT, true);
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.IS_EMPTY, AutomaticRoleAttributeRuleType.IDENTITY_EAV, attributeSix, null);

		IdmFormAttributeDto attributeSeven = createFormAttribute(AutomaticRoleAttributeRuleType.CONTRACT_EAV, PersistentType.INT, true);
		createAutomaticRuleForEav(automaticRole, AutomaticRoleAttributeRuleComparison.IS_EMPTY, AutomaticRoleAttributeRuleType.CONTRACT_EAV, attributeSeven, null);

		createAutomaticRuleForEntity(automaticRole, AutomaticRoleAttributeRuleComparison.START_WITH, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.firstName.getName(), "John");
		
		createAutomaticRuleForEntity(automaticRole, AutomaticRoleAttributeRuleComparison.CONTAINS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), "doe");
		
		createAutomaticRuleForEntity(automaticRole, AutomaticRoleAttributeRuleComparison.NOT_CONTAINS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.externalCode.getName(), "doe");
		
		createAutomaticRuleForEntity(automaticRole, AutomaticRoleAttributeRuleComparison.NOT_START_WITH, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), "test");
		
		createAutomaticRuleForEntity(automaticRole, AutomaticRoleAttributeRuleComparison.NOT_EQUALS, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), "test");

		createAutomaticRuleForEntity(automaticRole, AutomaticRoleAttributeRuleComparison.NOT_END_WITH, AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), "test");

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attributeOne, PersistentType.INT, Lists.newArrayList(666));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attributeTwo, PersistentType.SHORTTEXT, Lists.newArrayList("test", "teest", "te"));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attributeThree, PersistentType.LONG, Lists.newArrayList(5));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attributeFour, PersistentType.SHORTTEXT, Lists.newArrayList("test2", "test4"));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attributeFive, PersistentType.LONG, Lists.newArrayList(1, 2, 3));
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(identity, attributeSix, PersistentType.SHORTTEXT, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		saveEavValue(contract, attributeSeven, PersistentType.INT, Lists.newArrayList());
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);

		contract.setPosition("123test123");
		contract = identityContractService.save(contract);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 0);
		
		identity.setFirstName("John123");
		identity.setDescription("111doe111");
		identity.setExternalCode(null);
		identity = identityService.save(identity);
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
		recalculateSync(automaticRole.getId());
		checkIdentityRoles(identity, 1);
	}

	/**
	 * Recalculate all given roles sync
	 *
	 * @param roles
	 */
	private void recalculateAllSync(IdmAutomaticRoleAttributeDto ...roles) {
		for (IdmAutomaticRoleAttributeDto role : roles) {
			recalculateSync(role.getId());
		}
	}

	/**
	 * Create automatic role attribute with one rule for entity only (contract, identity)
	 *
	 * @param comparison
	 * @param type
	 * @param attributeName
	 * @param value
	 * @return
	 */
	private IdmAutomaticRoleAttributeDto createAutomaticRuleForEntity(AutomaticRoleAttributeRuleComparison comparison, AutomaticRoleAttributeRuleType type, String attributeName, String value) {
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(getHelper().createRole().getId());
		return this.createAutomaticRuleForEntity(automaticRole, comparison, type, attributeName, value);
	}

	/**
	 * Create automatic role attribute with one rule for entity only (contract, identity)
	 *
	 * @param comparison
	 * @param type
	 * @param attributeName
	 * @param value
	 * @return
	 */
	private IdmAutomaticRoleAttributeDto createAutomaticRuleForEntity(IdmAutomaticRoleAttributeDto automaticRole, AutomaticRoleAttributeRuleComparison comparison, AutomaticRoleAttributeRuleType type, String attributeName, String value) {
		getHelper().createAutomaticRoleRule(automaticRole.getId(), comparison,
				type, attributeName, null, value);
		
		recalculateSync(automaticRole.getId());

		return automaticRole;
	}

	/**
	 * Create automatic role rule for given automatic role (contract eav, identity eav)
	 *
	 * @param automaticRole
	 * @param comparison
	 * @param type
	 * @param attribute
	 * @param value
	 */
	private void createAutomaticRuleForEav(IdmAutomaticRoleAttributeDto automaticRole, AutomaticRoleAttributeRuleComparison comparison, AutomaticRoleAttributeRuleType type, IdmFormAttributeDto attribute, String value) {
		getHelper().createAutomaticRoleRule(automaticRole.getId(), comparison,
				type, null, attribute.getId(), value);
		recalculateSync(automaticRole.getId());
	}

	/**
	 * Create automatic role attribute with one rule for entity form attribute (contract eav, identity eav)
	 * @param comparison
	 * @param type
	 * @param persistentType
	 * @param value
	 * @return
	 */
	private IdmAutomaticRoleAttributeDto createAutomaticRuleForEav(AutomaticRoleAttributeRuleComparison comparison, AutomaticRoleAttributeRuleType type, IdmFormAttributeDto attribute, String value) {
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(getHelper().createRole().getId());
		this.createAutomaticRuleForEav(automaticRole, comparison, type, attribute, value);
		return automaticRole;
	}

	/**
	 * Create form attribute for contract eav or identity eav
	 * @param type
	 * @param persistentType
	 * @param multiple
	 * @return
	 */
	private IdmFormAttributeDto createFormAttribute(AutomaticRoleAttributeRuleType type, PersistentType persistentType, boolean multiple) {
		IdmFormAttributeDto attribute = null;
		if (type == AutomaticRoleAttributeRuleType.CONTRACT_EAV) {
			attribute = getHelper().createEavAttribute(getHelper().createName(), IdmIdentityContract.class, persistentType);
		} else {
			attribute = getHelper().createEavAttribute(getHelper().createName(), IdmIdentity.class, persistentType);
		}
		attribute.setMultiple(multiple);
		return formService.saveAttribute(attribute);
	}

	/**
	 * Method correspond method {@link IdmAutomaticRoleAttributeRuleService#recalculate()} but in synchronized mode
	 */
	private void recalculateSync(UUID automaticRoleId) {
		automaticRoleAttributeService.recalculate(automaticRoleId);
	}

	/**
	 * Create (save) rule for contract eav multiple.
	 *
	 * @param comparsion
	 */
	private void createRuleForContractEav(AutomaticRoleAttributeRuleComparison comparsion, String value) {
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getHelper().createName());
		automaticRole = automaticRoleAttributeService.save(automaticRole);

		IdmFormAttributeDto attribute = getHelper().createEavAttribute(getHelper().createName(), IdmIdentityContract.class, PersistentType.SHORTTEXT);
		attribute.setMultiple(true);
		attribute = formService.saveAttribute(attribute);

		IdmAutomaticRoleAttributeRuleDto rule = new IdmAutomaticRoleAttributeRuleDto();
		rule.setComparison(comparsion);
		rule.setType(AutomaticRoleAttributeRuleType.CONTRACT_EAV);
		rule.setValue(value);
		rule.setFormAttribute(attribute.getId());
		rule.setAutomaticRoleAttribute(automaticRole.getId());
		automaticRoleAttributeRuleService.save(rule);
	}

	/**
	 * Check count of identity roles
	 *
	 * @param identity
	 * @param count
	 */
	private void checkIdentityRoles(IdmIdentityDto identity, int count) {
		assertEquals(count, identityRoleService.findAllByIdentity(identity.getId()).size());
	}

	/**
	 * Save form value.
	 *
	 * @param owner
	 * @param attribute
	 * @param type
	 * @param values
	 */
	private void saveEavValue(Identifiable owner, IdmFormAttributeDto attribute, PersistentType type, List<Serializable> values) {
		Class<? extends Identifiable> ownerClass;
		if (owner instanceof IdmIdentityDto) {
			ownerClass = IdmIdentity.class;
		} else {
			ownerClass = IdmIdentityContract.class;
		}

		if (values == null || values.isEmpty()) {
			formService.deleteValues(owner, attribute);
		} else if (values.size() == 1) {
				getHelper().setEavValue(owner, attribute, ownerClass, values.get(0), type);
		} else {
			IdmFormDefinitionDto main = formService.getDefinition(ownerClass);
			List<IdmFormValueDto> valuesNew = new ArrayList<IdmFormValueDto>();
			for (Serializable value : values) {
				IdmFormValueDto newValue = new IdmFormValueDto();
				newValue.setPersistentType(type);
				newValue.setValue(value);
				newValue.setFormAttribute(attribute.getId());
				newValue.setOwnerId(owner.getId());
				valuesNew.add(newValue);
			}
			formService.saveFormInstance(owner, main, valuesNew);
		}
	}
}
