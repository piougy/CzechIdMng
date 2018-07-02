package eu.bcvsolutions.idm.core.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link RoleDeleteBulkAction}
 *
 * @author svandav
 *
 */

public class RoleDeleteBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private IdmRoleService roleService;
	
	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.DELETE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		List<IdmRoleDto> roles = this.createRoles(5);
		
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(roles);
		bulkAction.setIdentifiers(this.getIdFromList(roles));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			IdmRoleDto roleDto = roleService.get(id);
			assertNull(roleDto);
		}
	}
	
	@Test
	public void prevalidationBulkActionByIds() {
		IdmRoleDto role = getHelper().createRole();
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleDeleteBulkAction.NAME);
		bulkAction.getIdentifiers().add(role.getId());
		
		// None info results
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		assertEquals(0, resultModels.getInfos().size());
		
		// Assign identity to role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);
		
		// Warning message, role has identity
		resultModels = bulkActionManager.prevalidate(bulkAction);
		assertEquals(1, resultModels.getInfos().size());
	}
	
	@Test
	public void processBulkActionByFilter() {
		String testDescription = "bulkActionName" + System.currentTimeMillis();
		List<IdmRoleDto> roles = this.createRoles(5);
		
		for (IdmRoleDto role : roles) {
			role.setDescription(testDescription);
			role = roleService.save(role);
		}
		
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setValue(testDescription);
		filter.setProperty(IdmRole_.description.getName());

		List<IdmRoleDto> checkIdentities = roleService.find(filter, null).getContent();
		assertEquals(5, checkIdentities.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);
		
		for (IdmRoleDto role : roles) {
			IdmRoleDto dto = roleService.get(role.getId());
			assertNull(dto);
		}
	}
	
	@Test
	public void processBulkActionByFilterWithRemove() {
		String testDescription = "bulkActionName" + System.currentTimeMillis();
		List<IdmRoleDto> roles = this.createRoles(5);
		
		IdmRoleDto removedRole = roles.get(0);
		IdmRoleDto removedRole2 = roles.get(1);
		
		for (IdmRoleDto role : roles) {
			role.setDescription(testDescription);
			role = roleService.save(role);
		}
		
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setValue(testDescription);
		filter.setProperty(IdmRole_.description.getName());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		bulkAction.setRemoveIdentifiers(Sets.newHashSet(removedRole.getId(), removedRole2.getId()));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 3l, null, null);
		
		Set<UUID> ids = getIdFromList(roles);
		for (UUID id : ids) {
			IdmRoleDto dto = roleService.get(id);
			if (id.equals(removedRole.getId()) || id.equals(removedRole2.getId())) {
				assertNotNull(dto);
				continue;
			}
			assertNull(dto);
		}
	}

	@Test
	public void processBulkActionWithoutPermission() {
		// user hasn't permission for update role
		IdmIdentityDto readerIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(readerIdentity.getUsername());
		
		List<IdmRoleDto> roles = this.createRoles(5);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(roles);
		bulkAction.setIdentifiers(this.getIdFromList(roles));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 5l, 0l);
		
		for (UUID id : ids) {
			IdmRoleDto roleDto = roleService.get(id);
			assertNotNull(roleDto);
		}
	}
}
