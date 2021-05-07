package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Integration tests for {@link RoleDeleteBulkAction}.
 *
 * @author svandav
 * @author Radek Tomiška
 */
public class RoleDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmRoleCompositionService roleCompositionService;
	@Autowired
	private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired
	private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	
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
		
		checkResultLrt(processAction, 0l, 0l, 5l);
		
		for (UUID id : ids) {
			IdmRoleDto roleDto = roleService.get(id);
			assertNotNull(roleDto);
		}
	}
	
	@Test
	public void testPrevalidationManyConceptsToModify() {
		IdmRoleDto role = getHelper().createRole();
		assertNotNull(role);

		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createRoleRequest(identity, role);

		Set<UUID> roleIds = new HashSet<UUID>();
		roleIds.add(role.getId());
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(roleIds);
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		List<ResultModel> infos = resultModels //
				.getInfos() //
				.stream() //
				.filter(info -> {
					return CoreResultCode.ROLE_DELETE_BULK_ACTION_CONCEPTS_TO_MODIFY.getCode()
							.equals(info.getStatusEnum());
				}).collect(Collectors.toList());
		assertEquals(1, infos.size());
	}
	
	@Test
	public void testPrevalidateWithAssignedRoles() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);
		//
		// ~ force is not avaliable
		logout();
		loginWithout(TestHelper.ADMIN_USERNAME, IdmGroupPermission.APP_ADMIN, CoreGroupPermission.ROLE_ADMIN); // 
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getFormAttributes().stream().allMatch(a -> !a.getCode().equals(RoleProcessor.PROPERTY_FORCE_DELETE));
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		List<ResultModel> infos = resultModels //
				.getInfos() //
				.stream() //
				.filter(info -> {
					return CoreResultCode.ROLE_DELETE_BULK_ACTION_NUMBER_OF_IDENTITIES.getCode()
							.equals(info.getStatusEnum());
				}).collect(Collectors.toList());
		assertEquals(1, infos.size());
		//
		// force is available
		logout();
		loginAsAdmin();
		bulkAction = this.findBulkAction(IdmRole.class, RoleDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getFormAttributes().stream().anyMatch(a -> a.getCode().equals(RoleProcessor.PROPERTY_FORCE_DELETE));
		resultModels = bulkActionManager.prevalidate(bulkAction);
		infos = resultModels //
				.getInfos() //
				.stream() //
				.filter(info -> {
					return CoreResultCode.ROLE_FORCE_DELETE_BULK_ACTION_NUMBER_OF_IDENTITIES.getCode()
							.equals(info.getStatusEnum());
				}).collect(Collectors.toList());
		assertEquals(1, infos.size());
	}
	
	@Test
	public void testForceDeleteAsync() {
		logout();
		loginAsAdmin();
		// create identities
		String description = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setDescription(description);
		IdmIdentityDto identityOne = identityService.save(identity);
		identity = getHelper().createIdentity((GuardedString) null);
		identity.setDescription(description);
		IdmIdentityDto identityTwo = identityService.save(identity);
		// create roles
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto roleSubOne = getHelper().createRole();
		IdmRoleDto roleSubTwo = getHelper().createRole();
		IdmRoleDto roleSubSubOne = getHelper().createRole();
		// create business roles
		IdmRoleCompositionDto compositionOne = getHelper().createRoleComposition(role, roleSubOne);
		IdmRoleCompositionDto compositionTwo = getHelper().createRoleComposition(role, roleSubTwo);
		IdmRoleCompositionDto compositionThree = getHelper().createRoleComposition(roleSubOne, roleSubSubOne);
		// create automatic roles - by tree and by attribute too
		IdmAutomaticRoleAttributeDto automaticRoleOne = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRoleOne.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, description);
		IdmAutomaticRoleAttributeDto automaticRoleTwo = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRoleTwo.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, description);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identityOne);
		contract.setWorkPosition(treeNode.getId());
		contractService.save(contract);
		contract = getHelper().getPrimeContract(identityTwo);
		contract.setWorkPosition(treeNode.getId());
		contractService.save(contract);
		IdmRoleTreeNodeDto automaticRoleThree = getHelper().createRoleTreeNode(role, treeNode, false);
		IdmRoleTreeNodeDto automaticRoleFour = getHelper().createRoleTreeNode(role, treeNode, false);
		// create manuallyAssigned roles
		getHelper().createIdentityRole(identityOne, role);
		getHelper().createIdentityRole(identityOne, role);
		getHelper().createIdentityRole(identityTwo, role);
		getHelper().createIdentityRole(identityTwo, role);
		
		Assert.assertEquals(24, identityRoleService.findAllByIdentity(identityOne.getId()).size());
		Assert.assertEquals(24, identityRoleService.findAllByIdentity(identityTwo.getId()).size());
		// remove role async
		try {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			
			Map<String, Object> properties = new HashMap<>();
			properties.put(RoleProcessor.PROPERTY_FORCE_DELETE, Boolean.TRUE);
			// delete by bulk action
			IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleDeleteBulkAction.NAME);
			bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
			bulkAction.setProperties(properties);
			IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
			checkResultLrt(processAction, 1l, 0l, 0l);
			//
			getHelper().waitForResult(res -> {
				return roleService.get(role) != null;
			});
			//
			Assert.assertTrue(identityRoleService.findAllByIdentity(identityOne.getId()).isEmpty());
			Assert.assertTrue(identityRoleService.findAllByIdentity(identityTwo.getId()).isEmpty());
			Assert.assertNull(roleCompositionService.get(compositionOne));
			Assert.assertNull(roleCompositionService.get(compositionTwo));
			Assert.assertNotNull(roleCompositionService.get(compositionThree));
			Assert.assertNull(automaticRoleAttributeService.get(automaticRoleOne));
			Assert.assertNull(automaticRoleAttributeService.get(automaticRoleTwo));
			Assert.assertNull(roleTreeNodeService.get(automaticRoleThree));
			Assert.assertNull(roleTreeNodeService.get(automaticRoleFour));
			Assert.assertNull(roleService.get(role));
			Assert.assertNotNull(roleService.get(roleSubOne));
			Assert.assertNotNull(roleService.get(roleSubTwo));
			Assert.assertNotNull(roleService.get(roleSubSubOne));
		} finally {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
		}
	}
}
