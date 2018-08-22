package eu.bcvsolutions.idm.core.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.bulk.action.impl.IdentityRemoveRoleBulkAction;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Bulk action for {@link IdentityRemoveRoleBulkAction}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdentityRemoveRoleBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	
	@Before
	public void login() {
		IdmIdentityDto identity = getHelper().createIdentity();
		
		IdmRoleDto createRole = getHelper().createRole();
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.READ, IdentityBasePermission.CHANGEPERMISSION);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITYCONTRACT, IdmIdentityContract.class, IdmBasePermission.AUTOCOMPLETE);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.ROLEREQUEST, IdmRoleRequest.class, IdmBasePermission.CREATE, IdmBasePermission.EXECUTE);

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
		IdmRoleDto createRole = getHelper().createRole();
		IdmRoleDto createRole2 = getHelper().createRole();
		
		for (IdmIdentityDto identity : identities) {
			getHelper().createIdentityRole(identity, createRole);
			getHelper().createIdentityRole(identity, createRole2);
		}
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRemoveRoleBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityRemoveRoleBulkAction.ROLE_CODE, Lists.newArrayList(createRole.getId().toString()) );
		properties.put(IdentityRemoveRoleBulkAction.PRIMARY_CONTRACT_CODE, Boolean.TRUE);
		properties.put(IdentityRemoveRoleBulkAction.APPROVE_CODE, Boolean.FALSE);
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
			List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(id);
			assertEquals(1, contracts.size());
			IdmIdentityContractDto contract = contracts.get(0);
			
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
			assertEquals(1, identityRoles.size());
			
			IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
			assertEquals(createRole2.getId(), identityRoleDto.getRole());
		}
	}

	@Test
	public void processBulkActionByFilter() {
		String testFirstName = "bulkActionFirstName" + System.currentTimeMillis();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		IdmRoleDto createRole = getHelper().createRole();
		IdmRoleDto createRole2 = getHelper().createRole();
		
		for (IdmIdentityDto identity : identities) {
			identity.setFirstName(testFirstName);
			identity = identityService.save(identity);

			// create second contract
			IdmIdentityContractDto contact = getHelper().createIdentityContact(identity);
			contact.setValidTill(LocalDate.now().plusDays(5));
			contact.setExterne(true);
			contact = identityContractService.save(contact);

			getHelper().createIdentityRole(contact, createRole);
			getHelper().createIdentityRole(contact, createRole2);
			getHelper().createIdentityRole(identity, createRole);
			getHelper().createIdentityRole(identity, createRole2);
		}
		
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setFirstName(testFirstName);

		List<IdmIdentityDto> checkIdentities = identityService.find(filter, null).getContent();
		assertEquals(5, checkIdentities.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRemoveRoleBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityRemoveRoleBulkAction.ROLE_CODE, Lists.newArrayList(createRole.getId().toString(), createRole2.getId().toString()) );
		properties.put(IdentityRemoveRoleBulkAction.PRIMARY_CONTRACT_CODE, Boolean.FALSE);
		properties.put(IdentityRemoveRoleBulkAction.APPROVE_CODE, Boolean.FALSE);
		bulkAction.setProperties(properties);
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);
		
		for (IdmIdentityDto identity : identities) {
			List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
			assertEquals(2, contracts.size());
			for (IdmIdentityContractDto contract : contracts) {
				List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
				assertEquals(0, identityRoles.size());
			}
		}
	}

	@Test
	public void processBulkActionByFilterWithRemove() {
		String testLastName = "bulkActionLastName" + System.currentTimeMillis();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		IdmIdentityDto removedIdentity = identities.get(0);
		IdmIdentityDto removedIdentity2 = identities.get(1);
		
		IdmRoleDto createRole = getHelper().createRole();
		
		for (IdmIdentityDto identity : identities) {
			identity.setLastName(testLastName);
			identity = identityService.save(identity);
			getHelper().createIdentityRole(identity, createRole);
		}
		
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setLastName(testLastName);
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRemoveRoleBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		bulkAction.setRemoveIdentifiers(Sets.newHashSet(removedIdentity.getId(), removedIdentity2.getId()));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityRemoveRoleBulkAction.ROLE_CODE, Lists.newArrayList(createRole.getId().toString()) );
		properties.put(IdentityRemoveRoleBulkAction.PRIMARY_CONTRACT_CODE, Boolean.TRUE);
		properties.put(IdentityRemoveRoleBulkAction.APPROVE_CODE, Boolean.FALSE);
		bulkAction.setProperties(properties);
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 3l, null, null);
		
		Set<UUID> ids = getIdFromList(identities);
		for (UUID id : ids) {
			IdmIdentityDto identity = identityService.get(id);
			
			List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
			assertEquals(1, contracts.size());
			IdmIdentityContractDto contract = contracts.get(0);
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
			
			if (id.equals(removedIdentity.getId()) || id.equals(removedIdentity2.getId())) {
				assertFalse(identityRoles.isEmpty());
				IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
				assertEquals(createRole.getId(), identityRoleDto.getRole());
			} else {
				assertTrue(identityRoles.isEmpty());
			}
		}
	}

	@Test
	public void processBulkActionWithoutPermission() {
		// user hasn't permission for change permission identity
		IdmIdentityDto identityForLogin = getHelper().createIdentity();
		
		IdmRoleDto permissionRole = getHelper().createRole();
		getHelper().createBasePolicy(permissionRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.READ);
		getHelper().createBasePolicy(permissionRole.getId(), CoreGroupPermission.IDENTITYCONTRACT, IdmIdentityContract.class, IdmBasePermission.AUTOCOMPLETE);
		
		getHelper().createIdentityRole(identityForLogin, permissionRole);
		loginAsNoAdmin(identityForLogin.getUsername());
		
		IdmRoleDto createRole = getHelper().createRole();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		for (IdmIdentityDto identity : identities) {
			getHelper().createIdentityRole(identity, createRole);
		}
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRemoveRoleBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityRemoveRoleBulkAction.ROLE_CODE, Lists.newArrayList(createRole.getId().toString()));
		properties.put(IdentityRemoveRoleBulkAction.PRIMARY_CONTRACT_CODE, Boolean.TRUE);
		properties.put(IdentityRemoveRoleBulkAction.APPROVE_CODE, Boolean.FALSE);
		bulkAction.setProperties(properties);
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 0l, 5l);
		
		for (UUID id : ids) {
			IdmIdentityDto identity = identityService.get(id);
			
			List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
			assertEquals(1, contracts.size());
			IdmIdentityContractDto contract = contracts.get(0);
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
			assertEquals(1, identityRoles.size());
			IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
			
			assertEquals(createRole.getId(), identityRoleDto.getRole());
		}
	}

	@Test
	public void processBulkActionWithoutContracts() {
		IdmRoleDto createRole = getHelper().createRole();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		for (IdmIdentityDto identity : identities) {
			for (IdmIdentityContractDto contract : identityContractService.findAllByIdentity(identity.getId())) {
				identityContractService.delete(contract);
			}
		}
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRemoveRoleBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityRemoveRoleBulkAction.ROLE_CODE, Lists.newArrayList(createRole.getId().toString()) );
		properties.put(IdentityRemoveRoleBulkAction.PRIMARY_CONTRACT_CODE, Boolean.FALSE);
		properties.put(IdentityRemoveRoleBulkAction.APPROVE_CODE, Boolean.FALSE);
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 0l, 5l);
		
		for (UUID id : ids) {
			IdmIdentityDto identity = identityService.get(id);
			
			List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
			assertEquals(0, contracts.size());
		}
	}
}
