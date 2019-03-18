package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.bulk.action.impl.role.RoleDuplicateBulkAction;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.processor.role.DuplicateRoleCompositionProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Test prevent to remove account on target system, when role is duplicated and changed.
 * 
 * @author Radek Tomiška
 *
 */
public class AccRoleDuplicateBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private AccAccountService accountService;
	@Autowired private IdmRoleCompositionService roleCompositionService;
	@Autowired private AccIdentityAccountService identityAccountService;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testDontRemoveAccount() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		String environment = getHelper().createName();
		IdmRoleDto role = getHelper().createRole(null, null, environment);
		IdmRoleDto roleSubOne = getHelper().createRole(null, null, environment);
		IdmRoleDto roleSubTwo = getHelper().createRole(null, null, environment);
		IdmRoleCompositionDto compositionSubOne = getHelper().createRoleComposition(role, roleSubOne);
		//
		// create system mapping on the target
		String targetEnvironment = getHelper().createName();
		IdmRoleDto roleTarget = getHelper().createRole(null, role.getBaseCode(), targetEnvironment);
		IdmRoleDto roleSubOneTarget = getHelper().createRole(null, roleSubOne.getBaseCode(), targetEnvironment);
		IdmRoleDto roleSubTwoTarget = getHelper().createRole(null, roleSubTwo.getBaseCode(), targetEnvironment);
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		getHelper().createRoleSystem(roleSubOneTarget, system);
		getHelper().createRoleSystem(roleSubTwoTarget, system);
		getHelper().createIdentityRole(identity, roleTarget);
		//
		// check account not exist now - composition on target doesn't exist
		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNull(account);
		//
		// bulk action updates composition only
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(roleSubOneTarget.getId())));
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> !ir.getRole().equals(roleSubTwoTarget.getId())));
		//
		// change a source composition
		roleCompositionService.delete(compositionSubOne);
		getHelper().createRoleComposition(role, roleSubTwo);
		//
		processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		//
		List<IdmRoleCompositionDto> targetSubRoles = roleCompositionService.findAllSubRoles(roleTarget.getId());
		Assert.assertEquals(1, targetSubRoles.size());
		Assert.assertEquals(roleSubTwoTarget.getId(), targetSubRoles.get(0).getSub());
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(roleSubTwoTarget.getId())));
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> !ir.getRole().equals(roleSubOneTarget.getId())));
		//
		// search identity accounts
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();
		Assert.assertEquals(1, identityAccounts.size());
		//
		AccAccountDto switchedAccount = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(switchedAccount);
		Assert.assertEquals(account.getId(), switchedAccount.getId());
	}
	
	@Override
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
