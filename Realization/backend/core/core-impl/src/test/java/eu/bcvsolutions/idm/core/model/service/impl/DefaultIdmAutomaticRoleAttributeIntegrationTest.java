package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAutomaticRoleByAttributeTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveAutomaticRoleTaskExecutor;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test for automatic roles by attribute and their rules
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class DefaultIdmAutomaticRoleAttributeIntegrationTest extends AbstractIntegrationTest {

	private final String TEST_NAME = "test-name-";

	@Autowired
	private TestHelper testHelper;
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
	
	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		automaticRoleAttributeService.find(null).forEach(autoRole -> {
			automaticRoleAttributeService.delete(autoRole);
		});
		super.logout();
	}

	@Test
	public void testAutomaticRoleCrud() {
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getTestName());
		IdmAutomaticRoleAttributeDto savedAutomaticRole = automaticRoleAttributeService.save(automaticRole);
		//
		assertNotNull(savedAutomaticRole);
		assertNotNull(savedAutomaticRole.getId());
		assertEquals(automaticRole.getRole(), savedAutomaticRole.getRole());
		assertEquals(automaticRole.getName(), savedAutomaticRole.getName());
		//
		try {
			// update isn't allowed
			savedAutomaticRole.setName(getTestName());
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
	
	@Test
	public void testFilterHasRules() {
		long totalElements = automaticRoleAttributeService.find(null).getTotalElements();
		assertEquals(0, totalElements);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getTestName());
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
		automaticRole.setName(getTestName());
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
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getTestName());
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
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, testValue);
		//
		IdmIdentityDto identity = testHelper.createIdentity();
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
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(role.getId());
		automaticRole.setName(getTestName());
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
		IdmIdentityDto identity = testHelper.createIdentity();
		identity.setDescription(testValue);
		identity = identityService.save(identity);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		assertNotNull(identityRoleDto.getRoleTreeNode());
		assertEquals(automaticRole.getId(), identityRoleDto.getRoleTreeNode());
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
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, testValue);
		//
		IdmIdentityDto identity = testHelper.createIdentity();
		identity.setDescription(testValue);
		identity = identityService.save(identity);
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertNotNull(identityRoleDto.getRoleTreeNode());
		assertEquals(automaticRole.getId(), identityRoleDto.getRoleTreeNode());
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
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		primeContract.setPosition(testValue);
		primeContract = identityContractService.save(primeContract);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), null, testValue);
		//
		// add new one
		this.recalculateSync(automaticRole.getId());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertNotNull(identityRoleDto.getRoleTreeNode());
		assertEquals(automaticRole.getId(), identityRoleDto.getRoleTreeNode());
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
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), null, testValue);
		//
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		primeContract.setPosition(testValue);
		primeContract = identityContractService.save(primeContract);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertNotNull(identityRoleDto.getRoleTreeNode());
		assertEquals(automaticRole.getId(), identityRoleDto.getRoleTreeNode());
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
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.SHORTTEXT);
		testHelper.setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.SHORTTEXT);
		//
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		assertNotNull(identityRoleDto.getRoleTreeNode());
		assertEquals(automaticRole.getId(), identityRoleDto.getRoleTreeNode());
		assertEquals(automaticRole.getRole(), identityRoleDto.getRole());
		//
		// change value and recalculate
		testHelper.setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue + "-test", PersistentType.SHORTTEXT);
		//
		// recalculate isn't needed, is done when save identity contract
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAssingByContractEavAttrWithoutRecalcualte() {
		String testValue = "123!@#" + System.currentTimeMillis();
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		//
		IdmFormAttributeDto createEavAttribute = testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.SHORTTEXT);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, createEavAttribute.getId(), testValue);
		//
		primeContract.setPosition(testValue);
		primeContract = identityContractService.save(primeContract);
		//
		// we need to save eav value, resave identity doesn't enough - we need event eav_save
		testHelper.setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testValue, PersistentType.SHORTTEXT);
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		assertNotNull(identityRoleDto.getRoleTreeNode());
		assertEquals(automaticRole.getId(), identityRoleDto.getRoleTreeNode());
		assertEquals(automaticRole.getRole(), identityRoleDto.getRole());
		//
		// change value and recalculate
		testHelper.setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testValue + "-test", PersistentType.SHORTTEXT);
		//
		// recalculate isn't needed, is done when save identity
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testMoreRulesIdentity() {
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmRoleDto role = testHelper.createRole();
		//
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		//
		String email = "test@example.tld";
		String username = getTestName();
		String firstName = "firstName";
		//
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.email.getName(), null, email);
		//
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmAutomaticRoleAttributeRuleDto firstNameRule = testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmRoleDto role = testHelper.createRole();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		//
		testHelper.createIdentityRole(primeContract, role);
		//
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		IdmAutomaticRoleAttributeDto automaticRole2 = testHelper.createAutomaticRole(role.getId());
		//
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.email.getName(), null, testEmail);
		//
		testHelper.createAutomaticRoleRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, testDescription);
		//
		// only one role, assigned by direct add
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRole = identityRoles.get(0);
		assertNull(identityRole.getRoleTreeNode());
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
			if (idenityRole.getRoleTreeNode() == null) {
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
			if (idenityRole.getRoleTreeNode() == null) {
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
		identity.setDescription(testDescription + " ");
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleIntegerEav() {
		Integer testValue = 123456;
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.INT);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		testHelper.setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.INT);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleLongEav() {
		Long testValue = 123456l;
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.LONG);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		testHelper.setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.LONG);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDoubleEav() {
		Double testValue = 123456d;
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.DOUBLE);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		testHelper.setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.DOUBLE);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDateEav() {
		DateTime testValue = new DateTime(1514764800);
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.DATE);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		testHelper.setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.DATE);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDateTimeEav() {
		DateTime testValue = new DateTime(System.currentTimeMillis());
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.DATETIME);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		testHelper.setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.DATETIME);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleBooleanEav() {
		Boolean testValue = true;
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.BOOLEAN);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		testHelper.setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.BOOLEAN);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDisableAttribute() {
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
	public void testAutomaticRoleContractMainAttribute() {
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmRoleDto role = testHelper.createRole();
		IdmRoleDto role2 = testHelper.createRole();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		//
		// create two eav attributes (for identity and contract)
		IdmFormAttributeDto createEavAttribute = 	testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.SHORTTEXT);
		testHelper.setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testEavContractValue + "-not-passed", PersistentType.SHORTTEXT);
		IdmFormAttributeDto createEavAttribute2 = 	testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.SHORTTEXT);
		testHelper.setEavValue(identity, createEavAttribute2, IdmIdentity.class, testEavIdentityValue + "-not-passed", PersistentType.SHORTTEXT);
		//
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		IdmAutomaticRoleAttributeDto automaticRole2 = testHelper.createAutomaticRole(role2.getId());
		//
		// rules for first automatic role
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.email.getName(), null, testEmail);
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, createEavAttribute.getId(), testEavContractValue);
		//
		// rules for second automatic role
		testHelper.createAutomaticRoleRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), null, testPositionName);
		testHelper.createAutomaticRoleRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		testHelper.setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testEavContractValue, PersistentType.SHORTTEXT);
		//
		// one automatic roles has passed all rules
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRole = identityRoles.get(0);
		assertEquals(automaticRole.getRole(), identityRole.getRole());
		assertEquals(automaticRole.getId(), identityRole.getRoleTreeNode());
		//
		identity.setEmail(testEmail + "-not-passed");
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		testHelper.setEavValue(identity, createEavAttribute2, IdmIdentity.class, testEavIdentityValue, PersistentType.SHORTTEXT);
		// passed second automatic role
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		identityRole = identityRoles.get(0);
		assertEquals(automaticRole2.getRole(), identityRole.getRole());
		assertEquals(automaticRole2.getId(), identityRole.getRoleTreeNode());
		//
		identity.setEmail(testEmail);
		identity = identityService.save(identity);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());
		for (IdmIdentityRoleDto identityRol : identityRoles) {
			if (identityRol.getRole().equals(role.getId())) {
				assertEquals(automaticRole.getRole(), identityRol.getRole());
				assertEquals(automaticRole.getId(), identityRol.getRoleTreeNode());
			} else {
				assertEquals(automaticRole2.getRole(), identityRol.getRole());
				assertEquals(automaticRole2.getId(), identityRol.getRoleTreeNode());
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
		assertEquals(automaticRole.getId(), identityRole.getRoleTreeNode());
		//
		automaticRoleAttributeService.delete(automaticRole);
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testRemoveLastRuleWithCheck() throws InterruptedException {
		String eavCode = "testingEav";
		Long testEavContractValue = System.currentTimeMillis();
		UUID testEavIdentityValue = UUID.randomUUID();
		
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmRoleDto role = testHelper.createRole();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		
		// create two eav attributes (for identity and contract)
		IdmFormAttributeDto eavAttributeIdentity = testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentity.class, PersistentType.UUID);
		testHelper.setEavValue(identity, eavAttributeIdentity, IdmIdentity.class, testEavIdentityValue, PersistentType.UUID);
		IdmFormAttributeDto eavAttributeContract = 	testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.LONG);
		testHelper.setEavValue(primeContract, eavAttributeContract, IdmIdentityContract.class, testEavContractValue, PersistentType.LONG);

		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		IdmAutomaticRoleAttributeRuleDto rule1 = testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, eavAttributeIdentity.getId(), testEavIdentityValue.toString());
		IdmAutomaticRoleAttributeRuleDto rule2 = testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		
		waitForTaskWithRecalculation(automaticRole);
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testRemoveLastRuleWithoutCheck() {
		String eavCode = "testingEav";
		Long testEavIdentityValue = System.currentTimeMillis();
		UUID testEavContractValue = UUID.randomUUID();
		
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmRoleDto role = testHelper.createRole();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		
		// create two eav attributes (for identity and contract)
		IdmFormAttributeDto eavAttributeIdentity = testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentity.class, PersistentType.LONG);
		testHelper.setEavValue(identity, eavAttributeIdentity, IdmIdentity.class, testEavIdentityValue, PersistentType.LONG);
		IdmFormAttributeDto eavAttributeContract = 	testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.UUID);
		testHelper.setEavValue(primeContract, eavAttributeContract, IdmIdentityContract.class, testEavContractValue, PersistentType.UUID);

		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		IdmAutomaticRoleAttributeRuleDto rule1 = testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, eavAttributeIdentity.getId(), testEavIdentityValue.toString());
		IdmAutomaticRoleAttributeRuleDto rule2 = testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		String eavCode = "testingEav";
		UUID testValue1 = UUID.randomUUID();
		Long testValue2 = System.currentTimeMillis();
		Boolean testValue3 = Boolean.FALSE;
		
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		IdmIdentityContractDto contract2 = testHelper.createIdentityContact(identity, null, null, new LocalDate().plusDays(2));
		IdmIdentityContractDto contract3 = testHelper.createIdentityContact(identity, null, new LocalDate().minusDays(5), new LocalDate().plusDays(5));
		
		IdmIdentityContractDto primeContractCheck = testHelper.getPrimeContract(identity.getId());
		assertEquals(primeContract.getId(), primeContractCheck.getId());
		
		IdmFormAttributeDto eavAttributeContract1 = testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.UUID);
		testHelper.setEavValue(primeContract, eavAttributeContract1, IdmIdentityContract.class, testValue1, PersistentType.UUID);
		
		IdmFormAttributeDto eavAttributeContract2 = testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.LONG);
		testHelper.setEavValue(contract2, eavAttributeContract2, IdmIdentityContract.class, testValue2, PersistentType.LONG);
		
		IdmFormAttributeDto eavAttributeContract3 = testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.BOOLEAN);
		testHelper.setEavValue(contract3, eavAttributeContract3, IdmIdentityContract.class, testValue3, PersistentType.BOOLEAN);
		
		IdmRoleDto role1 = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole1 = testHelper.createAutomaticRole(role1.getId());
		testHelper.createAutomaticRoleRule(automaticRole1.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract1.getId(), testValue1.toString());
		
		IdmRoleDto role2 = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole2 = testHelper.createAutomaticRole(role2.getId());
		testHelper.createAutomaticRoleRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract2.getId(), testValue2.toString());
		
		IdmRoleDto role3 = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole3 = testHelper.createAutomaticRole(role3.getId());
		testHelper.createAutomaticRoleRule(automaticRole3.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
	public void testDeletePrimariContract() {
		String eavCode = "testingEav";
		UUID testValue1 = UUID.randomUUID();
		Long testValue2 = System.currentTimeMillis();
		Boolean testValue3 = Boolean.FALSE;
		
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		IdmIdentityContractDto contract2 = testHelper.createIdentityContact(identity, null, null, new LocalDate().plusDays(2));
		IdmIdentityContractDto contract3 = testHelper.createIdentityContact(identity, null, new LocalDate().minusDays(2), new LocalDate().plusDays(2));
		
		IdmIdentityContractDto primeContractCheck = testHelper.getPrimeContract(identity.getId());
		assertEquals(primeContract.getId(), primeContractCheck.getId());
		
		IdmFormAttributeDto eavAttributeContract1 = testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.UUID);
		testHelper.setEavValue(primeContract, eavAttributeContract1, IdmIdentityContract.class, testValue1, PersistentType.UUID);
		
		IdmFormAttributeDto eavAttributeContract2 = testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.LONG);
		testHelper.setEavValue(contract2, eavAttributeContract2, IdmIdentityContract.class, testValue2, PersistentType.LONG);
		
		IdmFormAttributeDto eavAttributeContract3 = testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.BOOLEAN);
		testHelper.setEavValue(contract3, eavAttributeContract3, IdmIdentityContract.class, testValue3, PersistentType.BOOLEAN);
		
		IdmRoleDto role1 = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole1 = testHelper.createAutomaticRole(role1.getId());
		testHelper.createAutomaticRoleRule(automaticRole1.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract1.getId(), testValue1.toString());
		
		IdmRoleDto role2 = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole2 = testHelper.createAutomaticRole(role2.getId());
		testHelper.createAutomaticRoleRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, eavAttributeContract2.getId(), testValue2.toString());
		
		IdmRoleDto role3 = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole3 = testHelper.createAutomaticRole(role3.getId());
		testHelper.createAutomaticRoleRule(automaticRole3.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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

		IdmIdentityContractDto newPrimeContract = testHelper.getPrimeContract(identity.getId());
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
		assertTrue(contractCheck2);
		assertFalse(contractCheck3);
		
		IdmIdentityContractDto newNewPrimeContract = testHelper.getPrimeContract(identity.getId());
		assertNotEquals(newPrimeContract.getId(), newNewPrimeContract.getId());
		
		assertEquals(contract2, newNewPrimeContract);
		
		identityContractService.delete(newNewPrimeContract);
		
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testTextTypeInEav() {
		String testValue = "123!@#" + System.currentTimeMillis();
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = testHelper.createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.TEXT);
		testHelper.setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.TEXT);
		//
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, null, createEavAttribute.getId(), testValue);
	}
	
	@Test
	public void testEmptyIdentityEav() {
		String testDescription = "testDescription-" + System.currentTimeMillis();
		String eavCode = "eavCode-test-";
		//
		IdmIdentityDto identity1 = testHelper.createIdentity();
		IdmIdentityDto identity2 = testHelper.createIdentity();
		IdmIdentityDto identity3 = testHelper.createIdentity();
		
		IdmIdentityContractDto contract1 = testHelper.getPrimeContract(identity1.getId());
		IdmIdentityContractDto contract2 = testHelper.getPrimeContract(identity2.getId());
		IdmIdentityContractDto contract3 = testHelper.getPrimeContract(identity3.getId());
		
		contract1.setDescription(testDescription);
		contract2.setDescription(testDescription);
		contract3.setDescription(testDescription);
		
		contract1 = identityContractService.save(contract1);
		contract2 = identityContractService.save(contract2);
		contract3 = identityContractService.save(contract3);
		
		IdmFormAttributeDto eavAttribute = testHelper.createEavAttribute(eavCode + System.currentTimeMillis(), IdmIdentity.class, PersistentType.BOOLEAN);
		testHelper.setEavValue(identity1, eavAttribute, IdmIdentity.class, Boolean.TRUE, PersistentType.BOOLEAN);
		
		IdmRoleDto role1 = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role1.getId());
		
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		
		IdmAutomaticRoleAttributeRuleDto rule = testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, identity.getUsername());
		//
		this.recalculateSync(automaticRole.getId());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityContractDto expiredContract = testHelper.createIdentityContact(identity, null, new LocalDate().minusDays(10), new LocalDate().minusDays(5));
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByContract(expiredContract.getId());
		assertEquals(0, identityRoles.size());
		//
		expiredContract.setValidTill(new LocalDate().plusDays(100));
		expiredContract = identityContractService.save(expiredContract);
		//
		identityRoles = identityRoleService.findAllByContract(testHelper.getPrimeContract(identity.getId()).getId());
		assertEquals(1, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());
		//
		expiredContract.setValidTill(new LocalDate().minusDays(2));
		expiredContract = identityContractService.save(expiredContract);
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
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, identity.getUsername());
		//
		this.recalculateSync(automaticRole.getId());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		//
		IdmIdentityContractDto expiredContract = testHelper.createIdentityContact(identity, null, new LocalDate().plusDays(10), new LocalDate().plusDays(50));
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByContract(expiredContract.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testDisabledContract() {
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		IdmIdentityContractDto contract2 = testHelper.createIdentityContact(identity, null, new LocalDate().minusMonths(5), new LocalDate().plusMonths(5));
		contract2.setState(ContractState.DISABLED);
		contract2 = identityContractService.save(contract2);
		//
		IdmIdentityContractDto contract3 = testHelper.createIdentityContact(identity, null, null, new LocalDate().plusMonths(5));
		contract3.setState(ContractState.DISABLED);
		contract3 = identityContractService.save(contract3);
		//
		IdmIdentityContractDto contract4 = testHelper.createIdentityContact(identity, null, null, null);
		contract4.setState(ContractState.DISABLED);
		contract4 = identityContractService.save(contract4);
		//
		IdmIdentityContractDto contract5 = testHelper.createIdentityContact(identity, null, new LocalDate().minusMonths(5), null);
		contract5.setState(ContractState.DISABLED);
		contract5 = identityContractService.save(contract5);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = testHelper.createAutomaticRole(role.getId());
		testHelper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		//
		identityRoles = identityRoleService.findAllByContract(contract5.getId());
		assertEquals(1, identityRoles.size());
		//
		contract4.setState(null);
		contract4 = identityContractService.save(contract4);
		//
		identityRoles = identityRoleService.findAllByContract(contract4.getId());
		assertEquals(1, identityRoles.size());
		//
		contract3.setState(null);
		contract3 = identityContractService.save(contract3);
		//
		identityRoles = identityRoleService.findAllByContract(contract3.getId());
		assertEquals(1, identityRoles.size());
		//
		contract2.setState(null);
		contract2 = identityContractService.save(contract2);
		//
		identityRoles = identityRoleService.findAllByContract(contract2.getId());
		assertEquals(1, identityRoles.size());
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		for (IdmIdentityRoleDto identityRole : identityRoles) {
			assertEquals(automaticRole.getId(), identityRole.getRoleTreeNode());
			AbstractIdmAutomaticRoleDto embedded = DtoUtils.getEmbedded(identityRole, IdmAutomaticRoleAttributeService.ROLE_TREE_NODE_ATTRIBUTE_NAME, AbstractIdmAutomaticRoleDto.class);
			assertEquals(automaticRole, embedded);
			assertEquals(role.getId(), embedded.getRole());
			assertEquals(role.getId(), identityRole.getRole());
		}
		//
		contract3.setState(ContractState.DISABLED);
		contract3 = identityContractService.save(contract3);
		//
		identityRoles = identityRoleService.findAllByContract(contract3.getId());
		assertEquals(0, identityRoles.size());
	}
	
	private void waitForTaskWithRecalculation(IdmAutomaticRoleAttributeDto automaticRole) throws InterruptedException {
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setTaskType(RemoveAutomaticRoleTaskExecutor.class.getCanonicalName());
		
		IdmLongRunningTaskDto taskWithRecalculation = longRunningTaskService.find(filter, null).getContent()
			.stream() //
			.filter(lrt -> {
					Object parameter = lrt.getTaskProperties().get(RemoveAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE);
					if (parameter.equals(automaticRole.getId())) {
						return true;
					}
					return false;
				}
				)
			.findFirst()
			.orElse(null);
		
		assertNotNull(taskWithRecalculation);
		
		while(taskWithRecalculation.isRunning()) {
			Thread.sleep(500);
			taskWithRecalculation = longRunningTaskService.get(taskWithRecalculation.getId());
		}
	}

	/**
	 * Method return test name
	 * 
	 * @return
	 */
	private String getTestName() {
		return TEST_NAME + System.currentTimeMillis();
	}
	
	/**
	 * Method correspond method {@link IdmAutomaticRoleAttributeRuleService#recalculate()} but in synchronized mode
	 */
	private Boolean recalculateSync(UUID automaticRoleId) {
		ProcessAutomaticRoleByAttributeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByAttributeTaskExecutor.class);
		automaticRoleTask.setAutomaticRoleId(automaticRoleId);
		return longRunningTaskManager.executeSync(automaticRoleTask);
	}
}
