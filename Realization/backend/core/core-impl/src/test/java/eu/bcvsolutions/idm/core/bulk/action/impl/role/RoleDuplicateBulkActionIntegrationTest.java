package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.bulk.action.impl.role.RoleDuplicateBulkAction;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.event.processor.role.DuplicateRoleAutomaticByTreeProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.role.DuplicateRoleCompositionProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.role.DuplicateRoleFormAttributeProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete entity events from queue integration test
 * 
 * @author Radek Tomiška
 *
 */
public class RoleDuplicateBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleFormAttributeService roleFormAttributeService;
	@Autowired private FormService formService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService;
	@Autowired private IdmRoleCompositionService roleCompositionService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityContractService contractService;
	@Autowired private IdmEntityStateService entityStateService;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testDuplicateRoleOnlyOnTheSameEnvironment() {
		IdmRoleDto role = createRole();
		// create attributes, automatic roles etc.
		createRoleFormAttribute(role, getHelper().createName(), getHelper().createName());
		createAutomaticRole(role, getHelper().createTreeNode());
		createAutomaticRole(role, getHelper().createName());
		//
		Assert.assertFalse(findAllSubRoles(role).isEmpty());
		Assert.assertFalse(findAutomaticRolesByTree(role).isEmpty());
		Assert.assertFalse(findRoleFormAttributes(role).isEmpty());
		Assert.assertFalse(findAutomaticRolesByAttribute(role).isEmpty());
		//
		String roleBaseCode = role.getBaseCode();
		//
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(DuplicateRoleAutomaticByTreeProcessor.PARAMETER_INCLUDE_AUTOMATIC_ROLE, false);
		bulkAction.getProperties().put(DuplicateRoleFormAttributeProcessor.PARAMETER_INCLUDE_ROLE_FORM_ATTRIBUTE, false);
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, false);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);	
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setEnvironment(role.getEnvironment());
		List<IdmRoleDto> roles = roleService.find(filter, null).getContent();
		IdmRoleDto duplicate = roles.stream().filter(r -> r.getBaseCode().startsWith(roleBaseCode) && !r.getBaseCode().equals(roleBaseCode)).findFirst().get();
		Assert.assertNotNull(duplicate);
		Assert.assertEquals(duplicate.getName(), role.getName());
		Assert.assertEquals(duplicate.getDescription(), role.getDescription());
		//
		Assert.assertTrue(findAllSubRoles(duplicate).isEmpty());
		Assert.assertTrue(findAutomaticRolesByTree(duplicate).isEmpty());
		Assert.assertTrue(findRoleFormAttributes(duplicate).isEmpty());
		Assert.assertTrue(findAutomaticRolesByAttribute(duplicate).isEmpty());
		//
		role.setDescription(getHelper().createName());
		//
		processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		filter = new IdmRoleFilter();
		filter.setProperty(IdmRole_.name.getName());
		filter.setValue(role.getName());
		roles = roleService.find(filter, null).getContent();
		Assert.assertEquals(3, roles.size()); // on the same environment is created the second duplicate
	}
	
	@Test
	public void testDuplicateRoleOnlyOnTheSameEnvironmentGivenAsProperty() {
		IdmRoleDto role = createRole();
		// create attributes, automatic roles etc.
		createRoleFormAttribute(role, getHelper().createName(), getHelper().createName());
		createAutomaticRole(role, getHelper().createTreeNode());
		createAutomaticRole(role, getHelper().createName());
		//
		Assert.assertFalse(findAllSubRoles(role).isEmpty());
		Assert.assertFalse(findAutomaticRolesByTree(role).isEmpty());
		Assert.assertFalse(findRoleFormAttributes(role).isEmpty());
		Assert.assertFalse(findAutomaticRolesByAttribute(role).isEmpty());
		//
		String roleBaseCode = role.getBaseCode();
		//
		String targetEnvironment = role.getEnvironment();
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.getProperties().put(DuplicateRoleAutomaticByTreeProcessor.PARAMETER_INCLUDE_AUTOMATIC_ROLE, false);
		bulkAction.getProperties().put(DuplicateRoleFormAttributeProcessor.PARAMETER_INCLUDE_ROLE_FORM_ATTRIBUTE, false);
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, false);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);	
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setEnvironment(targetEnvironment);
		List<IdmRoleDto> roles = roleService.find(filter, null).getContent();
		IdmRoleDto duplicate = roles.stream().filter(r -> r.getBaseCode().startsWith(roleBaseCode) && !r.getBaseCode().equals(roleBaseCode)).findFirst().get();
		Assert.assertEquals(role.getName(), duplicate.getName());
		Assert.assertEquals(role.getDescription(), duplicate.getDescription());
		//
		Assert.assertTrue(findAllSubRoles(duplicate).isEmpty());
		Assert.assertTrue(findAutomaticRolesByTree(duplicate).isEmpty());
		Assert.assertTrue(findRoleFormAttributes(duplicate).isEmpty());
		Assert.assertTrue(findAutomaticRolesByAttribute(duplicate).isEmpty());
		//
		role.setDescription(getHelper().createName());
		//
		processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		filter = new IdmRoleFilter();
		filter.setProperty(IdmRole_.name.getName());
		filter.setValue(role.getName());
		roles = roleService.find(filter, null).getContent();
		Assert.assertEquals(3, roles.size()); // on the same environment is created the second duplicate
	}
	
	@Test
	public void testDuplicateRoleOnlyOnTheDifferentEnvironment() {
		IdmRoleDto role = createRole();
		// create attributes, automatic roles etc.
		createRoleFormAttribute(role, getHelper().createName(), getHelper().createName());
		createAutomaticRole(role, getHelper().createTreeNode());
		createAutomaticRole(role, getHelper().createName());
		//
		Assert.assertFalse(findAllSubRoles(role).isEmpty());
		Assert.assertFalse(findAutomaticRolesByTree(role).isEmpty());
		Assert.assertFalse(findRoleFormAttributes(role).isEmpty());
		Assert.assertFalse(findAutomaticRolesByAttribute(role).isEmpty());
		//
		String roleBaseCode = role.getBaseCode();
		//
		String targetEnvironment = getHelper().createName();
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.getProperties().put(DuplicateRoleAutomaticByTreeProcessor.PARAMETER_INCLUDE_AUTOMATIC_ROLE, false);
		bulkAction.getProperties().put(DuplicateRoleFormAttributeProcessor.PARAMETER_INCLUDE_ROLE_FORM_ATTRIBUTE, false);
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, false);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);	
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setEnvironment(targetEnvironment);
		List<IdmRoleDto> roles = roleService.find(filter, null).getContent();
		//
		Assert.assertEquals(1, roles.size());
		IdmRoleDto duplicate = roles.get(0);
		Assert.assertEquals(roleBaseCode, duplicate.getBaseCode());
		Assert.assertEquals(role.getName(), duplicate.getName());
		Assert.assertEquals(role.getDescription(), duplicate.getDescription());
		//
		Assert.assertTrue(findAllSubRoles(duplicate).isEmpty());
		Assert.assertTrue(findAutomaticRolesByTree(duplicate).isEmpty());
		Assert.assertTrue(findRoleFormAttributes(duplicate).isEmpty());
		Assert.assertTrue(findAutomaticRolesByAttribute(duplicate).isEmpty());
		//
		role.setDescription(getHelper().createName());
		role = roleService.save(role);
		//
		processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		roles = roleService.find(filter, null).getContent();
		Assert.assertEquals(1, roles.size());
		duplicate = roles.get(0);
		Assert.assertEquals(1, roles.size()); // on the different environment is updated the first duplicate
		Assert.assertEquals(role.getDescription(), duplicate.getDescription());
	}
	
	/**
	 * Sub roles are used the same as on parent
	 */
	@Test
	public void testDuplicateRoleOnTheSameEnvironmentWithComposition() {
		IdmRoleDto role = createRole();
		List<IdmRoleCompositionDto> subRoles = findAllSubRoles(role); 
		Assert.assertFalse(subRoles.isEmpty());
		//
		String roleBaseCode = role.getBaseCode();
		//
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);	
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setEnvironment(role.getEnvironment());
		List<IdmRoleDto> roles = roleService.find(filter, null).getContent();
		IdmRoleDto duplicate = roles.stream().filter(r -> r.getBaseCode().startsWith(roleBaseCode) && !r.getBaseCode().equals(roleBaseCode)).findFirst().get();
		//
		List<IdmRoleCompositionDto> duplicateSubRoles = findAllSubRoles(duplicate); 
		Assert.assertFalse(duplicateSubRoles.isEmpty());
		Assert.assertEquals(subRoles.size(), duplicateSubRoles.size());
		Assert.assertTrue(duplicateSubRoles.stream().allMatch(s -> subRoles.stream().anyMatch(r -> r.getSub().equals(s.getSub()))));
	}
	
	@Test
	public void testUpdateRoleRecursivelly() {
		// update some sub role description
		IdmRoleDto role = createRole();
		IdmRoleDto subRole = getHelper().createRole();
		getHelper().createRoleComposition(role, subRole);
		//
		String targetEnvironment = getHelper().createName();
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		IdmRoleDto duplicate = roleService.getByBaseCodeAndEnvironment(subRole.getCode(), targetEnvironment);
		Assert.assertNotNull(duplicate);
		Assert.assertEquals(subRole.getName(), duplicate.getName());
		//
		// change a original name
		subRole.setName(getHelper().createName());
		subRole = roleService.save(subRole);
		//
		processAction = bulkActionManager.processAction(bulkAction);
		//
		IdmRoleDto duplicateUpdate = roleService.getByBaseCodeAndEnvironment(subRole.getCode(), targetEnvironment);
		Assert.assertNotNull(duplicateUpdate);
		Assert.assertEquals(subRole.getName(), duplicateUpdate.getName());
		Assert.assertEquals(duplicate.getId(), duplicateUpdate.getId()); // just for sure - update
	}
	
	@Test
	public void testDuplicateRoleWithTheCyclicComposition() {
		// create cyclic composition
		IdmRoleDto role = createRole();
		IdmRoleDto subRole = getHelper().createRole();
		getHelper().createRoleComposition(role, subRole);
		getHelper().createRoleComposition(subRole, role);
		//
		//
		String targetEnvironment = getHelper().createName();
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		IdmRoleDto duplicate = roleService.getByBaseCodeAndEnvironment(subRole.getCode(), targetEnvironment);
		//
		Assert.assertEquals(findAllSubRoles(role).size(), findAllSubRoles(duplicate).size());
	}
	
	@Test
	public void testDuplicateRoleWithRoleAttributes() {
		IdmRoleDto role = createRole();
		// create attributes, automatic roles etc.
		String definitionCode = getHelper().createName();
		createRoleFormAttribute(role, definitionCode, getHelper().createName());
		createRoleFormAttribute(role, definitionCode, getHelper().createName());
		//
		List<IdmRoleFormAttributeDto> roleFormAttributes = findRoleFormAttributes(role);
		Assert.assertFalse(roleFormAttributes.isEmpty());
		//
		String roleBaseCode = role.getBaseCode();
		String targetEnvironment = getHelper().createName();
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.getProperties().put(DuplicateRoleFormAttributeProcessor.PARAMETER_INCLUDE_ROLE_FORM_ATTRIBUTE, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		IdmRoleDto duplicate = roleService.getByBaseCodeAndEnvironment(roleBaseCode, targetEnvironment);
		//
		List<IdmRoleFormAttributeDto> duplicateFormAttributes = findRoleFormAttributes(duplicate);
		Assert.assertFalse(duplicateFormAttributes.isEmpty());
		Assert.assertEquals(roleFormAttributes.size(), duplicateFormAttributes.size());
		Assert.assertTrue(duplicateFormAttributes.stream().allMatch(d -> roleFormAttributes.stream().anyMatch(r -> d.getFormAttribute().equals(r.getFormAttribute()))));
	}
	
	@Test
	public void testDuplicateRoleWithAutomaticRoles() {
		IdmRoleDto role = createRole();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setWorkPosition(treeNode.getId());
		contractService.save(contract);
		// create attributes, automatic roles etc.
		IdmAutomaticRoleAttributeDto automaticRoleAttribute = createAutomaticRole(role, identity.getUsername());
		IdmRoleTreeNodeDto automaticRoleTree = createAutomaticRole(role, treeNode);
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleTree.getId().equals(ir.getAutomaticRole())));
		//
		String roleBaseCode = role.getBaseCode();
		String targetEnvironment = getHelper().createName();
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.getProperties().put(DuplicateRoleAutomaticByTreeProcessor.PARAMETER_INCLUDE_AUTOMATIC_ROLE, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		IdmRoleDto duplicate = roleService.getByBaseCodeAndEnvironment(roleBaseCode, targetEnvironment);
		//
		IdmAutomaticRoleAttributeDto duplicateAutomaticRoleAttribute = findAutomaticRolesByAttribute(duplicate).get(0);
		IdmRoleTreeNodeDto duplicateAtomaticRoleTree = findAutomaticRolesByTree(duplicate).get(0);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleTree.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> duplicateAutomaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> duplicateAtomaticRoleTree.getId().equals(ir.getAutomaticRole())));
	}
	
	@Test
	public void testDuplicateRoleWithCompositionAndAutomaticRoles() {
		// automatic role on sub role
		IdmRoleDto parentRole = createRole();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setWorkPosition(treeNode.getId());
		contractService.save(contract);
		IdmRoleDto subRole = createRole();
		getHelper().createRoleComposition(parentRole, subRole);
		
		// create attributes, automatic roles etc.
		IdmAutomaticRoleAttributeDto automaticRoleAttribute = createAutomaticRole(subRole, identity.getUsername());
		IdmRoleTreeNodeDto automaticRoleTree = createAutomaticRole(subRole, treeNode);
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleTree.getId().equals(ir.getAutomaticRole())));
		//
		String targetEnvironment = getHelper().createName();
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(parentRole.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.getProperties().put(DuplicateRoleAutomaticByTreeProcessor.PARAMETER_INCLUDE_AUTOMATIC_ROLE, true);
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		IdmRoleDto duplicate = roleService.getByBaseCodeAndEnvironment(subRole.getBaseCode(), targetEnvironment);
		//
		IdmAutomaticRoleAttributeDto duplicateAutomaticRoleAttribute = findAutomaticRolesByAttribute(duplicate).get(0);
		IdmRoleTreeNodeDto duplicateAtomaticRoleTree = findAutomaticRolesByTree(duplicate).get(0);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleTree.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> duplicateAutomaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> duplicateAtomaticRoleTree.getId().equals(ir.getAutomaticRole())));
	}
	
	@Test
	public void testRemoveAutomaticRole() {
		//
		// create new entity state with a different transactionId - has to be preserved
		TransactionContextHolder.clearContext();
		IdmEntityStateDto otherState = new IdmEntityStateDto();
		otherState.setOwnerId(UUID.randomUUID());
		otherState.setOwnerType("mock");
		otherState.setResult(new OperationResultDto.Builder(OperationState.CREATED).build());
		otherState.setInstanceId("mock");
		otherState = entityStateService.save(otherState);
		//
		TransactionContextHolder.clearContext();
		// automatic role on sub role
		IdmRoleDto parentRole = createRole();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setWorkPosition(treeNode.getId());
		contractService.save(contract);
		IdmRoleDto subRole = createRole();
		getHelper().createRoleComposition(parentRole, subRole);
		
		// create attributes, automatic roles etc.
		IdmAutomaticRoleAttributeDto automaticRoleAttribute = createAutomaticRole(subRole, identity.getUsername());
		IdmRoleTreeNodeDto automaticRoleTree = createAutomaticRole(subRole, treeNode);
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleTree.getId().equals(ir.getAutomaticRole())));
		//
		String targetEnvironment = getHelper().createName();
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(parentRole.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.getProperties().put(DuplicateRoleAutomaticByTreeProcessor.PARAMETER_INCLUDE_AUTOMATIC_ROLE, true);
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		IdmRoleDto duplicate = roleService.getByBaseCodeAndEnvironment(subRole.getBaseCode(), targetEnvironment);
		//
		IdmAutomaticRoleAttributeDto duplicateAutomaticRoleAttribute = findAutomaticRolesByAttribute(duplicate).get(0);
		IdmRoleTreeNodeDto duplicateAtomaticRoleTree = findAutomaticRolesByTree(duplicate).get(0);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleTree.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> duplicateAutomaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> duplicateAtomaticRoleTree.getId().equals(ir.getAutomaticRole())));
		//
		automaticRoleAttributeService.delete(automaticRoleAttribute);
		//
		processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		duplicate = roleService.getByBaseCodeAndEnvironment(subRole.getBaseCode(), targetEnvironment);
		//
		Assert.assertTrue(findAutomaticRolesByAttribute(duplicate).isEmpty());
		Assert.assertNotNull(entityStateService.get(otherState));
	}
	
	@Test
	public void testRemoveRoleFromComposition() {
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto subRole = getHelper().createRole();
		IdmRoleCompositionDto roleComposition = getHelper().createRoleComposition(role, subRole);
		//
		String targetEnvironment = getHelper().createName();
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		IdmRoleDto duplicate = roleService.getByBaseCodeAndEnvironment(role.getBaseCode(), targetEnvironment);
		//
		Assert.assertEquals(1, findAllSubRoles(duplicate).size());
		//
		roleCompositionService.delete(roleComposition);
		//
		processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		Assert.assertTrue(findAllSubRoles(duplicate).isEmpty());
	}
	
	@Test
	public void testUpdateAutomaticRole() {
		// automatic role on sub role
		IdmRoleDto parentRole = createRole();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto subRole = createRole();
		getHelper().createRoleComposition(parentRole, subRole);
		
		// create attributes, automatic roles etc.
		IdmAutomaticRoleAttributeDto automaticRoleAttribute = createAutomaticRole(subRole, identity.getUsername());
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		//
		String targetEnvironment = getHelper().createName();
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(parentRole.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.getProperties().put(DuplicateRoleAutomaticByTreeProcessor.PARAMETER_INCLUDE_AUTOMATIC_ROLE, true);
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		IdmRoleDto duplicate = roleService.getByBaseCodeAndEnvironment(subRole.getBaseCode(), targetEnvironment);
		//
		IdmAutomaticRoleAttributeDto duplicateAutomaticRoleAttribute = findAutomaticRolesByAttribute(duplicate).get(0);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> duplicateAutomaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
		//
		IdmIdentityDto otherIdentity = getHelper().createIdentity((GuardedString) null);
		IdmAutomaticRoleAttributeRuleDto rule = automaticRoleAttributeRuleService.findAllRulesForAutomaticRole(automaticRoleAttribute.getId()).get(0);
		rule.setValue(otherIdentity.getUsername());
		automaticRoleAttributeRuleService.save(rule);
		automaticRoleAttributeService.recalculate(automaticRoleAttribute.getId());
		//
		processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		duplicate = roleService.getByBaseCodeAndEnvironment(subRole.getBaseCode(), targetEnvironment);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		assignedRoles = identityRoleService.findAllByIdentity(otherIdentity.getId());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRoleAttribute.getId().equals(ir.getAutomaticRole())));
	}
	
	private IdmRoleDto createRole() {
		String environment = getHelper().createName();
		IdmRoleDto role = getHelper().createRole(null, null, environment);
		role.setDescription(getHelper().createName());
		role = roleService.save(role);
		// create role composition - all roles with the same environment
		IdmRoleDto roleSubOne = getHelper().createRole(null, null, environment);
		IdmRoleDto roleSubTwo = getHelper().createRole(null, null, environment);
		IdmRoleDto roleSubOneSubOne = getHelper().createRole(null, null, environment);
		IdmRoleDto roleSubOneSubTwo = getHelper().createRole(null, null, environment);
		IdmRoleDto roleSubTwoSubOne = getHelper().createRole(null, null, environment);
		IdmRoleDto roleSubOneSubOneSubOne = getHelper().createRole(null, null, environment);
		getHelper().createRoleComposition(role, roleSubOne);
		getHelper().createRoleComposition(role, roleSubTwo);
		getHelper().createRoleComposition(roleSubOne, roleSubOneSubOne);
		getHelper().createRoleComposition(roleSubOne, roleSubOneSubTwo);
		getHelper().createRoleComposition(roleSubTwo, roleSubTwoSubOne);
		getHelper().createRoleComposition(roleSubOneSubOne, roleSubOneSubOneSubOne);
		//
		return role;
	}
	
	private IdmRoleFormAttributeDto createRoleFormAttribute(IdmRoleDto role, String definitionCode, String attributeCode) {
		IdmFormDefinitionDto definition = formService.getDefinition(IdmIdentityRoleDto.class, definitionCode);
		if (definition == null) {
			definition = formService.createDefinition(IdmIdentityRoleDto.class, definitionCode, null);
		}
		IdmFormAttributeDto attribute = definition.getMappedAttributeByCode(attributeCode);
		if (attribute == null) {
			attribute = new IdmFormAttributeDto(attributeCode);
			attribute.setFormDefinition(definition.getId());
			attribute.setPersistentType(PersistentType.TEXT);
			attribute.setRequired(false);
			attribute.setDefaultValue(getHelper().createName());
			//
			attribute = formService.saveAttribute(attribute);
		}
		//
		if (role.getIdentityRoleAttributeDefinition() == null || !role.getIdentityRoleAttributeDefinition().equals(definition.getId())) {
			role.setIdentityRoleAttributeDefinition(definition.getId());
			role = roleService.save(role);
		}
		//
		return roleFormAttributeService.addAttributeToSubdefintion(role, attribute);
	}
	
	private IdmAutomaticRoleAttributeDto createAutomaticRole(IdmRoleDto role, String username) {
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		//
		getHelper().createAutomaticRoleRule(
				automaticRole.getId(), 
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.username.getName(),
				null,
				username);
		automaticRoleAttributeService.recalculate(automaticRole.getId());
		//
		return automaticRole;
	}
	
	private IdmRoleTreeNodeDto createAutomaticRole(IdmRoleDto role, IdmTreeNodeDto treeNode) {
		return getHelper().createAutomaticRole(role, treeNode);
	}
	
	private List<IdmRoleTreeNodeDto> findAutomaticRolesByTree(IdmRoleDto role) {
		IdmRoleTreeNodeFilter filter = new IdmRoleTreeNodeFilter();
		filter.setRoleId(role.getId());
		//
		return roleTreeNodeService.find(filter, null).getContent();
	}
	
	private List<IdmAutomaticRoleAttributeDto> findAutomaticRolesByAttribute(IdmRoleDto role) {
		IdmAutomaticRoleFilter filter = new IdmAutomaticRoleFilter();
		filter.setRoleId(role.getId());
		//
		return automaticRoleAttributeService.find(filter, null).getContent();
	}
	
	private List<IdmRoleFormAttributeDto> findRoleFormAttributes(IdmRoleDto role) {
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		//
		return roleFormAttributeService.find(filter, null).getContent();
	}
	
	private List<IdmRoleCompositionDto> findAllSubRoles(IdmRoleDto role) {
		return roleCompositionService.findAllSubRoles(role.getId());
	}
	
}
