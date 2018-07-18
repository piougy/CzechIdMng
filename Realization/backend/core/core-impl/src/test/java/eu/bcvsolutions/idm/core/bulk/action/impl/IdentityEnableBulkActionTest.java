package eu.bcvsolutions.idm.core.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link IdentityEnableBulkAction}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdentityEnableBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private IdmIdentityService identityService;
	
	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.UPDATE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		for (IdmIdentityDto identity : identities) {
			identity.setState(IdentityState.DISABLED_MANUALLY);
			identity = identityService.save(identity);
			assertTrue(identity.getState() == IdentityState.DISABLED_MANUALLY);
		}
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityEnableBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
			assertTrue(identityDto.getState() == IdentityState.VALID);
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		String testFirstName = "bulkActionFirstName" + System.currentTimeMillis();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		for (IdmIdentityDto identity : identities) {
			identity.setFirstName(testFirstName);
			identity = identityService.save(identity);
			identity = identityService.disable(identity.getId());
			assertTrue(identity.getState() == IdentityState.DISABLED_MANUALLY);
		}
		
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setFirstName(testFirstName);

		List<IdmIdentityDto> checkIdentities = identityService.find(filter, null).getContent();
		assertEquals(5, checkIdentities.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityEnableBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);
		
		for (IdmIdentityDto identity : identities) {
			IdmIdentityDto dto = identityService.get(identity.getId());
			assertNotNull(dto);
			assertTrue(dto.getState() == IdentityState.VALID);
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
			identity = identityService.disable(identity.getId());
			assertTrue(identity.getState() == IdentityState.DISABLED_MANUALLY);
		}
		
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setLastName(testLastName);
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityEnableBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		bulkAction.setRemoveIdentifiers(Sets.newHashSet(removedIdentity.getId(), removedIdentity2.getId()));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 3l, null, null);
		
		for (IdmIdentityDto identity : identities) {
			IdmIdentityDto dto = identityService.get(identity.getId());
			assertNotNull(dto);
			if (dto.getId().equals(removedIdentity.getId()) || dto.getId().equals(removedIdentity2.getId())) {
				assertTrue(dto.getState() == IdentityState.DISABLED_MANUALLY);
				continue;
			}
			assertTrue(dto.getState() == IdentityState.VALID);
		}
	}
	
	@Test
	public void processBulkActionWithoutPermission() {
		// user hasn't permission for update identity
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
		
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		for (IdmIdentityDto identity : identities) {
			identity = identityService.disable(identity.getId());
			assertTrue(identity.getState() == IdentityState.DISABLED_MANUALLY);
		}
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityEnableBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 0l, 5l);
		
		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
			assertTrue(identityDto.getState() == IdentityState.DISABLED_MANUALLY);
		}
	}
}
