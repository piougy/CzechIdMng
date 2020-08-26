package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.exception.SystemEntityNotFoundException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for provisioning disable of system.
 * 
 * @author Radek Tomiška
 *
 */
public class DisabledProvisioningSystemProcessorIntegrationTest extends AbstractIntegrationTest{

	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private SysProvisioningOperationService provisioningOperationService;
	@Autowired private SysProvisioningArchiveService provisioningArchiveService;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemService systemService;
	@Autowired private IdmIdentityService identityService;
	
	@Test
	public void testDontCreateProvisioningOperationWithDisabledSystem() {
		// prepare role
		IdmRoleDto role = getHelper().createRole();
		//
		// create test system with mapping and link her to the sub roles
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		system.setDisabledProvisioning(true);
		system = systemService.save(system);
		getHelper().createRoleSystem(role, system);
		//
		// assign role
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		final IdmRoleRequestDto roleRequestOne = getHelper().createRoleRequest(identity, role);
		//
		getHelper().executeRequest(roleRequestOne, false);
		//
		// check after create
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		IdmIdentityRoleDto directRole = assignedRoles.stream().filter(ir -> ir.getDirectRole() == null).findFirst().get();
		Assert.assertEquals(role.getId(), directRole.getRole());
		//
		// check created account
		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		// empty provisioning
		Assert.assertNull(getHelper().findResource(account.getRealUid()));
		//
		// check provisioning archive
		SysProvisioningOperationFilter archiveFilter = new SysProvisioningOperationFilter();
		archiveFilter.setEntityIdentifier(identity.getId());
		//
		List<SysProvisioningOperationDto> activeOperations = provisioningOperationService.find(archiveFilter, null).getContent();
		Assert.assertTrue(activeOperations.isEmpty());
		List<SysProvisioningArchiveDto> executedOperations = provisioningArchiveService.find(archiveFilter, null).getContent();
		Assert.assertTrue(executedOperations.isEmpty());
	}
	
	@Test
	public void testDontCreateProvisioningOperationWithReadOnlySystem() {
		// prepare role
		IdmRoleDto role = getHelper().createRole();
		//
		// create test system with mapping and link her to the sub roles
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		system.setReadonly(true);
		system.setDisabledProvisioning(true);
		system = systemService.save(system);
		getHelper().createRoleSystem(role, system);
		//
		// assign role
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		final IdmRoleRequestDto roleRequestOne = getHelper().createRoleRequest(identity, role);
		//
		getHelper().executeRequest(roleRequestOne, false);
		//
		// check after create
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		IdmIdentityRoleDto directRole = assignedRoles.stream().filter(ir -> ir.getDirectRole() == null).findFirst().get();
		Assert.assertEquals(role.getId(), directRole.getRole());
		//
		// check created account
		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		// empty provisioning
		Assert.assertNull(getHelper().findResource(account.getRealUid()));
		//
		// check provisioning archive
		SysProvisioningOperationFilter archiveFilter = new SysProvisioningOperationFilter();
		archiveFilter.setEntityIdentifier(identity.getId());
		//
		List<SysProvisioningOperationDto> activeOperations = provisioningOperationService.find(archiveFilter, null).getContent();
		Assert.assertTrue(activeOperations.isEmpty());
		List<SysProvisioningArchiveDto> executedOperations = provisioningArchiveService.find(archiveFilter, null).getContent();
		Assert.assertTrue(executedOperations.isEmpty());
	}
	
	/**
	 * Password cannot be changed for identity, without provisioning was created before.
	 */
	@Test(expected = SystemEntityNotFoundException.class)
	public void testDisabledProvisioningPasswordChange() {
		// prepare role
		IdmRoleDto role = getHelper().createRole();
		//
		// create test system with mapping and link her to the sub roles
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		system.setReadonly(true);
		system.setDisabledProvisioning(true);
		system = systemService.save(system);
		getHelper().createRoleSystem(role, system);
		//
		// create identity and assign role 
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);
		//
		// check after create
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		IdmIdentityRoleDto directRole = assignedRoles.stream().filter(ir -> ir.getDirectRole() == null).findFirst().get();
		Assert.assertEquals(role.getId(), directRole.getRole());
		//
		// check created account
		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertNull(account.getSystemEntity());
		//
		// empty provisioning
		Assert.assertNull(getHelper().findResource(account.getRealUid()));
		//
		// check provisioning archive
		SysProvisioningOperationFilter archiveFilter = new SysProvisioningOperationFilter();
		archiveFilter.setEntityIdentifier(identity.getId());
		//
		List<SysProvisioningOperationDto> activeOperations = provisioningOperationService.find(archiveFilter, null).getContent();
		Assert.assertTrue(activeOperations.isEmpty());
		List<SysProvisioningArchiveDto> executedOperations = provisioningArchiveService.find(archiveFilter, null).getContent();
		Assert.assertTrue(executedOperations.isEmpty());
		//
		// switch system to normal state
		system.setReadonly(false);
		system.setDisabledProvisioning(false);
		system = systemService.save(system);
		//
		// try to change password
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setOldPassword(identity.getPassword());
		passwordChangeDto.setNewPassword(identity.getPassword());
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		//
		identityService.passwordChange(identity, passwordChangeDto);
	}
	
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
