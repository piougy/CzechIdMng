package eu.bcvsolutions.idm.core.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Test for bulk action extends {@link IdentityRoleByIdentityDeduplicationBulkAction}.
 * 
 * @author Ondrej Kopr
 *
 */
public class IdentityRoleByIdentityDeduplicationBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmRoleRequestService roleRequestService;

	@Before
	public void login() {
		IdmIdentityDto identity = getHelper().createIdentity();
		
		IdmRoleDto createRole = getHelper().createRole();
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.READ, IdentityBasePermission.CHANGEPERMISSION);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITYCONTRACT, IdmIdentityContract.class, IdmBasePermission.AUTOCOMPLETE);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITYROLE, IdmIdentityRole.class, IdmBasePermission.READ);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.ROLEREQUEST, IdmRoleRequest.class, IdmBasePermission.ADMIN);
		
		getHelper().createIdentityRole(identity, createRole);
		loginAsNoAdmin(identity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	@Transactional
	public void testTwoManuallyOneContract() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role);
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role);

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, roles.size());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRoleByIdentityDeduplicationBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(identity.getId()));
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, roles.size());

		IdmIdentityRoleDto actual = roles.get(0);
		assertEquals(one.getId(), actual.getId());
		assertNotEquals(two.getId(), actual.getId());
	}

	@Test
	@Transactional
	public void testTwoManuallyTwoContract() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto secondContract = getHelper().createIdentityContact(identity);
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityRoleDto one = getHelper().createIdentityRole(primeContract, role);
		IdmIdentityRoleDto two = getHelper().createIdentityRole(secondContract, role);

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, roles.size());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRoleByIdentityDeduplicationBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(identity.getId()));
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, roles.size());

		roles = identityRoleService.findAllByContract(primeContract.getId());
		assertEquals(1, roles.size());
		IdmIdentityRoleDto actual = roles.get(0);
		assertEquals(one.getId(), actual.getId());
		assertNotEquals(two.getId(), actual.getId());
		
		roles = identityRoleService.findAllByContract(secondContract.getId());
		assertEquals(1, roles.size());
		actual = roles.get(0);
		assertEquals(two.getId(), actual.getId());
		assertNotEquals(one.getId(), actual.getId());
	}

	@Test
	@Transactional
	public void testFourManuallyTwoContract() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto secondContract = getHelper().createIdentityContact(identity);
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityRoleDto primaryContractOne = getHelper().createIdentityRole(primeContract, role);
		IdmIdentityRoleDto primaryContractTwo = getHelper().createIdentityRole(primeContract, role);
		IdmIdentityRoleDto secondContractOne = getHelper().createIdentityRole(secondContract, role);
		IdmIdentityRoleDto secondContractTwo = getHelper().createIdentityRole(secondContract, role);

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(4, roles.size());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRoleByIdentityDeduplicationBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(identity.getId()));
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, roles.size());

		roles = identityRoleService.findAllByContract(primeContract.getId());
		assertEquals(1, roles.size());
		IdmIdentityRoleDto actual = roles.get(0);
		assertEquals(primaryContractOne.getId(), actual.getId());
		assertNotEquals(primaryContractTwo.getId(), actual.getId());
		
		roles = identityRoleService.findAllByContract(secondContract.getId());
		assertEquals(1, roles.size());
		actual = roles.get(0);
		assertEquals(secondContractOne.getId(), actual.getId());
		assertNotEquals(secondContractTwo.getId(), actual.getId());
	}

	@Test
	@Transactional
	public void testSevenManuallyOneContract() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role);
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role);
		IdmIdentityRoleDto three = getHelper().createIdentityRole(identity, role);
		IdmIdentityRoleDto four = getHelper().createIdentityRole(identity, role);
		IdmIdentityRoleDto five = getHelper().createIdentityRole(identity, role);
		IdmIdentityRoleDto six = getHelper().createIdentityRole(identity, role);
		IdmIdentityRoleDto seven = getHelper().createIdentityRole(identity, role);

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(7, roles.size());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRoleByIdentityDeduplicationBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(identity.getId()));
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		IdmLongRunningTaskDto longRunningTask = checkResultLrt(processAction, 1l, null, null);
		Assert.assertEquals(OperationState.EXECUTED, longRunningTask.getResultState());
		// item 
		List<IdmProcessedTaskItemDto> processedItems = processedTaskItemService.findLogItems(longRunningTask, null).getContent();
		Assert.assertEquals(1, processedItems.size());
		Assert.assertEquals(OperationState.EXECUTED, processedItems.get(0).getOperationResult().getState());
		// background request
		IdmRoleRequestFilter requestFilter = new IdmRoleRequestFilter();
		requestFilter.setApplicantId(identity.getId());
		List<IdmRoleRequestDto> requests = roleRequestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		Assert.assertEquals(RoleRequestState.EXECUTED, requests.get(0).getState());
		
		roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, roles.size());

		IdmIdentityRoleDto actual = roles.get(0);
		assertEquals(one.getId(), actual.getId());
		assertNotEquals(two.getId(), actual.getId());
		assertNotEquals(three.getId(), actual.getId());
		assertNotEquals(four.getId(), actual.getId());
		assertNotEquals(five.getId(), actual.getId());
		assertNotEquals(six.getId(), actual.getId());
		assertNotEquals(seven.getId(), actual.getId());
	}

	@Test
	public void testTwoAutomatic() {
		String automaticRoleValue = "test" + System.currentTimeMillis();

		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		identity.setDescription(automaticRoleValue);
		identity.setTitleAfter(automaticRoleValue);
		identity = identityService.save(identity);

		IdmRoleDto role = getHelper().createRole();

		IdmAutomaticRoleAttributeDto automaticRoleOne = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRoleOne.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.description.getName(), null, automaticRoleValue);
		
		IdmAutomaticRoleAttributeDto automaticRoleTwo = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRoleTwo.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.titleAfter.getName(), null, automaticRoleValue);

		getHelper().recalculateAutomaticRoleByAttribute(automaticRoleOne.getId());
		getHelper().recalculateAutomaticRoleByAttribute(automaticRoleTwo.getId());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, roles.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRoleByIdentityDeduplicationBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(identity.getId()));
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, roles.size());
	}

	@Test
	public void testThreeAutomaticAndThreeManually() {
		String automaticRoleValue = "test" + System.currentTimeMillis();

		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		identity.setDescription(automaticRoleValue);
		identity.setTitleAfter(automaticRoleValue);
		identity.setTitleBefore(automaticRoleValue);
		identity = identityService.save(identity);

		IdmRoleDto role = getHelper().createRole();
		
		IdmIdentityRoleDto identityRoleOne = getHelper().createIdentityRole(identity, role);
		IdmIdentityRoleDto identityRoleTwo = getHelper().createIdentityRole(identity, role);
		IdmIdentityRoleDto identityRoleThree = getHelper().createIdentityRole(identity, role);

		IdmAutomaticRoleAttributeDto automaticRoleOne = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRoleOne.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.description.getName(), null, automaticRoleValue);
		
		IdmAutomaticRoleAttributeDto automaticRoleTwo = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRoleTwo.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.titleAfter.getName(), null, automaticRoleValue);
		
		IdmAutomaticRoleAttributeDto automaticRoleThree = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRoleThree.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.titleBefore.getName(), null, automaticRoleValue);

		getHelper().recalculateAutomaticRoleByAttribute(automaticRoleOne.getId());
		getHelper().recalculateAutomaticRoleByAttribute(automaticRoleTwo.getId());
		getHelper().recalculateAutomaticRoleByAttribute(automaticRoleThree.getId());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(6, roles.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRoleByIdentityDeduplicationBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(identity.getId()));
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(3, roles.size());
		
		for (IdmIdentityRoleDto identityRole : roles) {
			assertNotEquals(identityRoleOne.getId(), identityRole.getId());
			assertNotEquals(identityRoleTwo.getId(), identityRole.getId());
			assertNotEquals(identityRoleThree.getId(), identityRole.getId());
		}
	}

	@Test
	public void testAutomaticAndManuallyAndContractCombination() {
		String automaticRoleValue = "test" + System.currentTimeMillis();

		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto primary = getHelper().getPrimeContract(identity);
		IdmIdentityContractDto identityContactOne = getHelper().createIdentityContact(identity);
		IdmIdentityContractDto identityContactTwo = getHelper().createIdentityContact(identity);

		identity.setDescription(automaticRoleValue);
		identity.setTitleAfter(automaticRoleValue);
		identity.setTitleBefore(automaticRoleValue);
		identity = identityService.save(identity);

		IdmRoleDto role = getHelper().createRole();
		
		IdmIdentityRoleDto primaryOne = getHelper().createIdentityRole(primary, role);
		IdmIdentityRoleDto contractOneOne = getHelper().createIdentityRole(identityContactOne, role);
		IdmIdentityRoleDto contractOneTwo = getHelper().createIdentityRole(identityContactOne, role);
		IdmIdentityRoleDto contractOneThree = getHelper().createIdentityRole(identityContactOne, role);
		IdmIdentityRoleDto contractOneFour = getHelper().createIdentityRole(identityContactOne, role);
		IdmIdentityRoleDto contractTwoOne = getHelper().createIdentityRole(identityContactTwo, role);
		IdmIdentityRoleDto contractTwoTwo = getHelper().createIdentityRole(identityContactTwo, role);

		IdmAutomaticRoleAttributeDto automaticRoleOne = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRoleOne.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.description.getName(), null, automaticRoleValue);
		
		IdmAutomaticRoleAttributeDto automaticRoleTwo = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRoleTwo.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.titleAfter.getName(), null, automaticRoleValue);
		
		IdmAutomaticRoleAttributeDto automaticRoleThree = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRoleThree.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.titleBefore.getName(), null, automaticRoleValue);

		getHelper().recalculateAutomaticRoleByAttribute(automaticRoleOne.getId());
		getHelper().recalculateAutomaticRoleByAttribute(automaticRoleTwo.getId());
		getHelper().recalculateAutomaticRoleByAttribute(automaticRoleThree.getId());

		// 7 manually added and 9 automatically
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(16, roles.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRoleByIdentityDeduplicationBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(identity.getId()));
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		roles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(9, roles.size());

		roles = identityRoleService.findAllByContract(primary.getId());
		assertEquals(3, roles.size());
		for (IdmIdentityRoleDto identityRole : roles) {
			assertNotEquals(primaryOne.getId(), identityRole.getId());
		}

		roles = identityRoleService.findAllByContract(primary.getId());
		assertEquals(3, roles.size());
		for (IdmIdentityRoleDto identityRole : roles) {
			assertNotEquals(primaryOne.getId(), identityRole.getId());
		}

		roles = identityRoleService.findAllByContract(identityContactOne.getId());
		assertEquals(3, roles.size());
		for (IdmIdentityRoleDto identityRole : roles) {
			assertNotEquals(contractOneOne.getId(), identityRole.getId());
			assertNotEquals(contractOneTwo.getId(), identityRole.getId());
			assertNotEquals(contractOneThree.getId(), identityRole.getId());
			assertNotEquals(contractOneFour.getId(), identityRole.getId());
		}

		roles = identityRoleService.findAllByContract(identityContactTwo.getId());
		assertEquals(3, roles.size());
		for (IdmIdentityRoleDto identityRole : roles) {
			assertNotEquals(contractTwoOne.getId(), identityRole.getId());
			assertNotEquals(contractTwoTwo.getId(), identityRole.getId());
		}
	}
}
