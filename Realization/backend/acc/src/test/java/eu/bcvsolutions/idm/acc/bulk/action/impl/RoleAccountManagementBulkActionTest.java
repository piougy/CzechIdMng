package eu.bcvsolutions.idm.acc.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link RoleAcmBulkAction}
 *
 * @author svandav
 *
 */

public class RoleAccountManagementBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private TestHelper helper;
	
	@Before
	public void init() {
		loginAsAdmin(InitApplicationData.ADMIN_USERNAME);
	}

	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testProcessBulkAction() {
		
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.UPDATE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
		// Delete all data
		helper.deleteAllResourceData();
		
		List<IdmRoleDto> roles = this.createRoles(1);
		IdmRoleDto role = roles.get(0);
		// Assign identity to role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);
		// Create system and assign him to the role
		SysSystemDto system = helper.createTestResourceSystem(true);
		helper.createRoleSystem(role, system);
		// ACM was not run, so account cannot be exists
		assertNull(helper.findResource(identity.getUsername()));
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);

		bulkAction.setIdentifiers(this.getIdFromList(roles));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1l, null, null);
		
		// ACM was run, so account must exists
		assertNotNull(helper.findResource(identity.getUsername()));
	}
	
	@Test
	public void testProcessBulkActionWithoutPermissions() {
		
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
		// Delete all data
		helper.deleteAllResourceData();
		
		List<IdmRoleDto> roles = this.createRoles(1);
		IdmRoleDto role = roles.get(0);
		// Assign identity to role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);
		// Create system and assign him to the role
		SysSystemDto system = helper.createTestResourceSystem(true);
		helper.createRoleSystem(role, system);
		// ACM was not run, so account cannot be exists
		assertNull(helper.findResource(identity.getUsername()));
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);

		bulkAction.setIdentifiers(this.getIdFromList(roles));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, null, 0l, 1l);
		
		// ACM was run, but without premissions for update the role
		assertNull(helper.findResource(identity.getUsername()));
	}
	
	@Test
	public void testProcessBulkActionWithPermissions() {
		
		// Read permission for all
		IdmIdentityDto user = getHelper().createIdentity();
		IdmRoleDto permissionRole = getHelper().createRole();
		getHelper().createBasePolicy(permissionRole.getId(), IdmBasePermission.READ);
		getHelper().createBasePolicy(permissionRole.getId(), CoreGroupPermission.ROLE, IdmRole.class, IdmBasePermission.UPDATE);
		getHelper().createIdentityRole(user, permissionRole);
		loginAsNoAdmin(user.getUsername());
		// Delete all data
		helper.deleteAllResourceData();
		
		List<IdmRoleDto> roles = this.createRoles(1);
		IdmRoleDto role = roles.get(0);
		// Assign identity to role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);
		// Create system and assign him to the role
		SysSystemDto system = helper.createTestResourceSystem(true);
		helper.createRoleSystem(role, system);
		// ACM was not run, so account cannot be exists
		assertNull(helper.findResource(identity.getUsername()));
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);

		bulkAction.setIdentifiers(this.getIdFromList(roles));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1l, null, null);
		
		// ACM was run, so account must exists
		assertNotNull(helper.findResource(identity.getUsername()));
	}
	
	@Test
	public void testPrevalidationBulkAction() {
		IdmRoleDto role = getHelper().createRole();
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);
		bulkAction.getIdentifiers().add(role.getId());
		
		// Warning message, role hasn't identity
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		assertEquals(1, resultModels.getInfos().size());
		assertEquals(AccResultCode.ROLE_ACM_BULK_ACTION_NONE_IDENTITIES.getCode(), resultModels.getInfos().get(0).getStatusEnum());
		// Assign identity to role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);
		
		// Info message, role has identity
		resultModels = bulkActionManager.prevalidate(bulkAction);
		assertEquals(1, resultModels.getInfos().size());
		assertEquals(AccResultCode.ROLE_ACM_BULK_ACTION_NUMBER_OF_IDENTITIES.getCode(), resultModels.getInfos().get(0).getStatusEnum());
	}
	
	
}
