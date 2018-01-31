package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAutomaticRoleByAttributeTaskExecutor;
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
	private IdmFormAttributeService formAttributeService;
	@Autowired
	private IdmFormDefinitionService formDefinitionService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	
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
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmFormAttributeDto createEavAttribute = createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.TEXT);
		setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.TEXT);
		//
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENITITY_EAV, null, createEavAttribute.getId(), testValue);
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
		setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue + "-test", PersistentType.TEXT);
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
		IdmFormAttributeDto createEavAttribute = createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.TEXT);
		setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testValue, PersistentType.TEXT);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, createEavAttribute.getId(), testValue);
		//
		primeContract.setPosition(testValue);
		primeContract = identityContractService.save(primeContract);
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
		setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testValue + "-test", PersistentType.TEXT);
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
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		//
		String email = "test@example.tld";
		String username = getTestName();
		String firstName = "firstName";
		//
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.email.getName(), null, email);
		//
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmAutomaticRoleAttributeRuleDto firstNameRule = createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		IdmAutomaticRoleAttributeDto automaticRole2 = createAutomaticRole(role.getId());
		//
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.email.getName(), null, testEmail);
		//
		createRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmFormAttributeDto createEavAttribute = createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.INT);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENITITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.INT);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleLongEav() {
		Long testValue = 123456l;
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.LONG);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENITITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.LONG);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDoubleEav() {
		Double testValue = 123456d;
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.DOUBLE);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENITITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.DOUBLE);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDateEav() {
		DateTime testValue = new DateTime(1514764800);
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.DATE);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENITITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.DATE);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDateTimeEav() {
		DateTime testValue = new DateTime(System.currentTimeMillis());
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.DATETIME);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENITITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.DATETIME);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleBooleanEav() {
		Boolean testValue = true;
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmFormAttributeDto createEavAttribute = createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.BOOLEAN);
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENITITY_EAV, null, createEavAttribute.getId(), testValue.toString());
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(0, identityRoles.size());
		//
		// change eav value
		setEavValue(identity, createEavAttribute, IdmIdentity.class, testValue, PersistentType.BOOLEAN);
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
	}
	
	@Test
	public void testAutomaticRoleDisableAttribute() {
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmRoleDto role = testHelper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
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
		IdmFormAttributeDto createEavAttribute = createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentityContract.class, PersistentType.TEXT);
		setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testEavContractValue + "-not-passed", PersistentType.TEXT);
		IdmFormAttributeDto createEavAttribute2 = createEavAttribute("testingEav" + System.currentTimeMillis(), IdmIdentity.class, PersistentType.TEXT);
		setEavValue(identity, createEavAttribute2, IdmIdentity.class, testEavIdentityValue + "-not-passed", PersistentType.TEXT);
		//
		IdmAutomaticRoleAttributeDto automaticRole = createAutomaticRole(role.getId());
		IdmAutomaticRoleAttributeDto automaticRole2 = createAutomaticRole(role2.getId());
		//
		// rules for first automatic role
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.email.getName(), null, testEmail);
		createRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT_EAV, null, createEavAttribute.getId(), testEavContractValue);
		//
		// rules for second automatic role
		createRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), null, testPositionName);
		createRule(automaticRole2.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENITITY_EAV, null, createEavAttribute2.getId(), testEavIdentityValue);
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
		setEavValue(primeContract, createEavAttribute, IdmIdentityContract.class, testEavContractValue, PersistentType.TEXT);
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
		setEavValue(identity, createEavAttribute2, IdmIdentity.class, testEavIdentityValue, PersistentType.TEXT);
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
	
	/**
	 * Create eav attribute
	 * 
	 * @param code
	 * @param clazz
	 * @param type
	 * @return
	 */
	private IdmFormAttributeDto createEavAttribute(String code, Class<? extends Identifiable> clazz, PersistentType type) {
		IdmFormAttributeDto eavAttribute = new IdmFormAttributeDto();
		eavAttribute.setCode(code);
		IdmFormDefinitionDto main = formDefinitionService.findOneByMain(clazz.getName());
		eavAttribute.setFormDefinition(main.getId());
		eavAttribute.setName(code);
		eavAttribute.setConfidential(false);
		eavAttribute.setRequired(false);
		eavAttribute.setReadonly(false);
		eavAttribute.setPersistentType(type);
		return formAttributeService.save(eavAttribute);
	}
	
	/**
	 * Save value to eav with code
	 * 
	 * @param ownerId
	 * @param code
	 * @param clazz
	 * @param value
	 */
	private void setEavValue(Identifiable owner, IdmFormAttributeDto attribute, Class<? extends Identifiable> clazz, Serializable value, PersistentType type) {
		UUID ownerId = UUID.fromString(owner.getId().toString());
		IdmFormDefinitionDto main = formDefinitionService.findOneByMain(clazz.getName());
		List<IdmFormValueDto> values = formService.getValues(ownerId, clazz, attribute);
		
		if (values.isEmpty()) {
			IdmFormValueDto newValue = new IdmFormValueDto();
			newValue.setPersistentType(type);
			newValue.setValue(value);
			newValue.setFormAttribute(attribute.getId());
			newValue.setOwnerId(owner.getId());
			values.add(newValue);
		} else {
			values.get(0).setValue(value);
		}
		
		formService.saveFormInstance(owner, main, values);
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
	 * Method create new automatic role by attribute
	 * 
	 * @param roleId
	 * @return
	 */
	private IdmAutomaticRoleAttributeDto createAutomaticRole(UUID roleId) {
		if (roleId == null) {
			IdmRoleDto role = testHelper.createRole();
			roleId = role.getId();
		}
		IdmAutomaticRoleAttributeDto automaticRole = new IdmAutomaticRoleAttributeDto();
		automaticRole.setRole(roleId);
		automaticRole.setName(getTestName());
		return automaticRoleAttributeService.save(automaticRole);
	}

	/**
	 * Create new rule with given informations. See params.
	 * And remove concept state from automatic role by attribute
	 * 
	 * @param automaticRoleId
	 * @param comparsion
	 * @param type
	 * @param attrName
	 * @param formAttrId
	 * @param value
	 * @return
	 */
	private IdmAutomaticRoleAttributeRuleDto createRule(UUID automaticRoleId,
			AutomaticRoleAttributeRuleComparison comparsion, AutomaticRoleAttributeRuleType type, String attrName,
			UUID formAttrId, String value) {
		IdmAutomaticRoleAttributeRuleDto rule = new IdmAutomaticRoleAttributeRuleDto();
		rule.setComparison(comparsion);
		rule.setType(type);
		rule.setAttributeName(attrName);
		rule.setFormAttribute(formAttrId);
		rule.setValue(value);
		rule.setAutomaticRoleAttribute(automaticRoleId);
		rule = automaticRoleAttributeRuleService.save(rule);
		// disable concept must be after rule save
		disableConcept(automaticRoleId);
		return rule;
	}
	
	/**
	 * Method correspond method {@link IdmAutomaticRoleAttributeRuleService#recalculate()} but in synchronized mode
	 */
	private Boolean recalculateSync(UUID automaticRoleId) {
		ProcessAutomaticRoleByAttributeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByAttributeTaskExecutor.class);
		automaticRoleTask.setAutomaticRoleId(automaticRoleId);
		return longRunningTaskManager.executeSync(automaticRoleTask);
	}
	
	/**
	 * Disable concept state for given automatic role
	 * 
	 * @param automaticRoleId
	 * @return
	 */
	private IdmAutomaticRoleAttributeDto disableConcept(UUID automaticRoleId) {
		return this.disableConcept(automaticRoleAttributeService.get(automaticRoleId));
	}
	
	/**
	 * Disable concept state for given automatic role
	 * 
	 * @param automaticRole
	 * @return
	 */
	private IdmAutomaticRoleAttributeDto disableConcept(IdmAutomaticRoleAttributeDto automaticRole) {
		// remove concept state
		automaticRole.setConcept(false);
		return automaticRoleAttributeService.save(automaticRole);
	}
}
