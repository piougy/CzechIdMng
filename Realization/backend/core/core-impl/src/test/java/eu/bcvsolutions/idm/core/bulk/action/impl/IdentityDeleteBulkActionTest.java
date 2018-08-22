package eu.bcvsolutions.idm.core.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.bulk.action.impl.IdentityDeleteBulkAction;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link IdentityDeleteBulkAction}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdentityDeleteBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private IdmIdentityService identityService;
	
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
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNull(identityDto);
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		String testFirstName = "bulkActionFirstName" + System.currentTimeMillis();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		for (IdmIdentityDto identity : identities) {
			identity.setFirstName(testFirstName);
			identity = identityService.save(identity);
		}
		
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setFirstName(testFirstName);

		List<IdmIdentityDto> checkIdentities = identityService.find(filter, null).getContent();
		assertEquals(5, checkIdentities.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);
		
		for (IdmIdentityDto identity : identities) {
			IdmIdentityDto dto = identityService.get(identity.getId());
			assertNull(dto);
		}
	}
	
	@Test
	public void processBulkActionByFilterWithRemove() {
		String testLastName = "bulkActionLastName" + System.currentTimeMillis();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		IdmIdentityDto removedIdentity = identities.get(0);
		IdmIdentityDto removedIdentity2 = identities.get(1);
		
		for (IdmIdentityDto identity : identities) {
			identity.setLastName(testLastName);
			identity = identityService.save(identity);
			assertTrue(identity.getState() != IdentityState.DISABLED_MANUALLY);
		}
		
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setLastName(testLastName);
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		bulkAction.setRemoveIdentifiers(Sets.newHashSet(removedIdentity.getId(), removedIdentity2.getId()));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 3l, null, null);
		
		Set<UUID> ids = getIdFromList(identities);
		for (UUID id : ids) {
			IdmIdentityDto dto = identityService.get(id);
			if (id.equals(removedIdentity.getId()) || id.equals(removedIdentity2.getId())) {
				assertNotNull(dto);
				continue;
			}
			assertNull(dto);
		}
	}

	@Test
	public void processBulkActionWithoutPermission() {
		// user hasn't permission for update identity
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
		
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 0l, 5l);
		
		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
		}
	}
}
