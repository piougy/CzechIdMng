package eu.bcvsolutions.idm.core.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link IdentityAddRoleBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class IdentityChangeUserTypeBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private IdmIdentityService identityService;
	@Autowired 
	private IdmFormProjectionService projectionService;
	@Autowired
	private LookupService lookupService;

	@Before
	public void login() {
		IdmIdentityDto identity = getHelper().createIdentity();

		IdmRoleDto createRole = getHelper().createRole();
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class,
				IdmBasePermission.READ,	IdentityBasePermission.CHANGEPROJECTION);
		getHelper().createIdentityRole(identity, createRole);
		loginAsNoAdmin(identity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void processBulkActionByIds() {
		List<IdmIdentityDto> identities = this.createIdentities(5);
		IdmFormProjectionDto projection = new IdmFormProjectionDto();
		projection.setCode(getHelper().createName());
		projection.setOwnerType(lookupService.getOwnerType(IdmIdentityDto.class));
		projection = projectionService.save(projection);
		
		IdmFormProjectionDto projection2 = new IdmFormProjectionDto();
		projection2.setCode(getHelper().createName());
		projection2.setOwnerType(lookupService.getOwnerType(IdmIdentityDto.class));
		projection2 = projectionService.save(projection2);

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityChangeUserTypeBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));

		// set created form projection to all identities
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityChangeUserTypeBulkAction.PROPERTY_USER_TYPE, projection.getId().toString());
		bulkAction.setProperties(properties);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);
		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
			assertEquals(identityDto.getFormProjection(), projection.getId());
		}
		
		// change form projection to another type
		properties.put(IdentityChangeUserTypeBulkAction.PROPERTY_USER_TYPE, projection2.getId().toString());
		bulkAction.setProperties(properties);
		processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);
		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
			assertEquals(identityDto.getFormProjection(), projection2.getId());
		}

		// remove projection from all identities
		properties.put(IdentityChangeUserTypeBulkAction.PROPERTY_USER_TYPE, null);
		bulkAction.setProperties(properties);
		processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);
		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
			assertNull(identityDto.getFormProjection());
		}
	}
}
