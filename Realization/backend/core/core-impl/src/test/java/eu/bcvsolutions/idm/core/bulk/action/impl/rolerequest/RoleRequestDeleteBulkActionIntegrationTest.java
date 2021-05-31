package eu.bcvsolutions.idm.core.bulk.action.impl.rolerequest;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link RoleDeleteBulkAction}.
 *
 * @author Ondrej Husnik
 */
public class RoleRequestDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired
	private IdmRoleRequestService roleRequestService;
	
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
		IdmRoleDto role = this.createRoles(1).get(0);
		IdmIdentityDto identity = createIdentities(1).get(0);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		
		IdmRoleRequestDto roleRequest = roleRequestService.createRequest(contract, role);
		Assert.assertNotNull(roleRequest);
		Assert.assertNotNull(roleRequest.getId());
				
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRoleRequest.class, RoleRequestDeleteBulkAction.NAME);
		
		Set<UUID> ids = new HashSet<UUID>();
		ids.add(roleRequest.getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		IdmRoleRequestDto deletedRoleRequest = roleRequestService.get(roleRequest.getId());
		Assert.assertNull(deletedRoleRequest);
	}
}
