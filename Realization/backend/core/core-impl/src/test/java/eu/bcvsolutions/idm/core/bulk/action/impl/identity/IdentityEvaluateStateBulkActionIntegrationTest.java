package eu.bcvsolutions.idm.core.bulk.action.impl.identity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Bulk aciton test.
 * 
 * @author Radek Tomiška
 */
public class IdentityEvaluateStateBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmIdentityService identityService;

	@Before
	public void login() {
		IdmIdentityDto identity = getHelper().createIdentity();

		IdmRoleDto createRole = getHelper().createRole();
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class,
				IdmBasePermission.READ,	IdmBasePermission.UPDATE);
		getHelper().createIdentityRole(identity, createRole);
		loginAsNoAdmin(identity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void processBulkActionByIds() {
		List<IdmIdentityDto> identities = this.createIdentities(3);
		IdmIdentityDto identityOne = identities.get(0);
		identityOne.setState(IdentityState.DISABLED);
		identityOne = identityService.save(identityOne);
		Assert.assertTrue(identityOne.isDisabled());
		Assert.assertEquals(IdentityState.DISABLED, identityOne.getState());
		IdmIdentityDto identityTwo = identities.get(1);
		identityTwo.setState(IdentityState.DISABLED_MANUALLY);
		identityTwo = identityService.save(identityTwo);
		Assert.assertTrue(identityTwo.isDisabled());
		Assert.assertEquals(IdentityState.DISABLED_MANUALLY, identityTwo.getState());
		IdmIdentityDto identityThree = identities.get(2);
		Assert.assertFalse(identityThree.isDisabled());
		Assert.assertEquals(IdentityState.CREATED, identityThree.getState());
		//
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityEvaluateStateBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(ids);
		//
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 3l, null, null);
		identityOne = identityService.get(identityOne);
		identityTwo = identityService.get(identityTwo);
		identityThree = identityService.get(identityThree);
		Assert.assertEquals(IdentityState.VALID, identityOne.getState());
		Assert.assertEquals(IdentityState.DISABLED_MANUALLY, identityTwo.getState());
		Assert.assertEquals(IdentityState.VALID, identityThree.getState());
		//
		processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 3l, null, null);
		identityOne = identityService.get(identityOne);
		identityTwo = identityService.get(identityTwo);
		identityThree = identityService.get(identityThree);
		Assert.assertEquals(IdentityState.VALID, identityOne.getState());
		Assert.assertEquals(IdentityState.DISABLED_MANUALLY, identityTwo.getState());
		Assert.assertEquals(IdentityState.VALID, identityThree.getState());
	}
}
