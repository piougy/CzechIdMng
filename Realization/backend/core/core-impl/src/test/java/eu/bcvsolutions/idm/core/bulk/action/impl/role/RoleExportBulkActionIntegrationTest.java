package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.collections.Lists;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.ExportImportType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIncompatibleRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ImportManager;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.task.SelfLongRunningTaskEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractExportBulkActionTest;

/**
 * Export role integration test
 * 
 * @author Vít Švanda
 *
 */
public class RoleExportBulkActionIntegrationTest extends AbstractExportBulkActionTest {

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmRoleFormAttributeService roleFormAttributeService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmRoleCompositionService roleCompositionService;
	@Autowired
	private ImportManager importManager;
	@Autowired
	private IdmIncompatibleRoleService incompatibleRoleService;
	@Autowired
	private IdmRoleGuaranteeService roleGuaranteeService;
	@Autowired
	private IdmRoleGuaranteeRoleService roleGuaranteeRoleService;
	@Autowired
	private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired
	private IdmRoleCatalogueRoleService roleCatalogueRoleService;
	@Autowired
	private IdmRoleCatalogueService roleCatalogueService;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	/**
	 * Green line test. Testing form attributes and business roles.
	 */
	@Test
	public void testExportAndImportRole() {
		IdmRoleDto role = createRole();
		// create attributes, automatic roles etc.
		createRoleFormAttribute(role, getHelper().createName(), getHelper().createName());

		Assert.assertFalse(findAllSubRoles(role).isEmpty());
		Assert.assertFalse(findRoleFormAttributes(role).isEmpty());

		// Make export, upload and import
		executeExportAndImport(role, RoleExportBulkAction.NAME,
				ImmutableMap.of(EXECUTE_BEFORE_DTO_DELETE, this::deleteAllSubroles));

		role = roleService.get(role.getId());
		Assert.assertNotNull(role);
		Assert.assertFalse(findAllSubRoles(role).isEmpty());
		Assert.assertFalse(findRoleFormAttributes(role).isEmpty());
	}

	@Test
	public void testExportAndImportRoleEavs() {
		IdmRoleDto role = createRole();
		IdmFormInstanceDto formInstanceDto = formService.getFormInstance(role);

		// Load form definition for role.
		Assert.assertNotNull(formInstanceDto);
		IdmFormDefinitionDto formDefinition = formInstanceDto.getFormDefinition();

		// Create new EAV attribute and set unique value to the role.
		String attributeName = this.getHelper().createName();
		String attributeValue = this.getHelper().createName();
		IdmFormAttributeDto formAttribute = new IdmFormAttributeDto(attributeName, attributeName,
				PersistentType.SHORTTEXT);
		formAttribute.setFormDefinition(formDefinition.getId());
		IdmFormAttributeDto finalFormAttribute = formService.saveAttribute(formAttribute);
		formService.saveValues(role, finalFormAttribute, Lists.newArrayList(attributeValue));

		// Make export, upload and import
		executeExportAndImport(role, RoleExportBulkAction.NAME,
				ImmutableMap.of(EXECUTE_BEFORE_DTO_DELETE, this::deleteAllSubroles));

		role = roleService.get(role.getId());
		Assert.assertNotNull(role);

		formInstanceDto = formService.getFormInstance(role);
		List<IdmFormValueDto> values = formInstanceDto.getValues();

		IdmFormValueDto formValueDto = values.stream()//
				.filter(value -> finalFormAttribute.getId().equals(value.getFormAttribute()))//
				.findFirst()//
				.get();
		Assert.assertEquals(formValueDto.getValue(), attributeValue);

	}

	@Test
	public void testExportAndImportRoleIncompatibilities() {
		IdmRoleDto role = createRole();
		IdmRoleDto incompatibileRoleOne = this.getHelper().createRole();
		IdmRoleDto incompatibileRoleTwo = this.getHelper().createRole();

		IdmIncompatibleRoleDto incompatibleRoleOne = this.getHelper().createIncompatibleRole(role,
				incompatibileRoleOne);

		// Make export, upload and import
		IdmExportImportDto importBatch = executeExportAndImport(role, RoleExportBulkAction.NAME,
				ImmutableMap.of(EXECUTE_BEFORE_DTO_DELETE, this::deleteAllSubroles));

		role = roleService.get(role.getId());
		Assert.assertNotNull(role);

		List<IdmIncompatibleRoleDto> incompatibilites = this.findIncompatibilites(role);
		Assert.assertEquals(1, incompatibilites.size());
		Assert.assertEquals(incompatibleRoleOne.getId(), incompatibilites.get(0).getId());

		this.getHelper().createIncompatibleRole(role, incompatibileRoleTwo);
		incompatibilites = this.findIncompatibilites(role);
		Assert.assertEquals(2, incompatibilites.size());

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Second incompatibility had to be deleted!
		incompatibilites = this.findIncompatibilites(role);
		Assert.assertEquals(1, incompatibilites.size());
		Assert.assertEquals(incompatibleRoleOne.getId(), incompatibilites.get(0).getId());
	}

	@Test
	public void testExportAndImportRoleGuaranteeByIdentity() {
		IdmRoleDto role = createRole();
		IdmIdentityDto guaranteeIdentity = this.getHelper().createIdentity();

		IdmRoleGuaranteeDto guarantee = this.getHelper().createRoleGuarantee(role, guaranteeIdentity);

		// Make export, upload and import
		IdmExportImportDto importBatch = executeExportAndImport(role, RoleExportBulkAction.NAME,
				ImmutableMap.of(EXECUTE_BEFORE_DTO_DELETE, this::deleteAllSubroles));
		
		role = roleService.get(role.getId());
		Assert.assertNotNull(role);

		List<IdmRoleGuaranteeDto> guarantees = this.findGuarantees(role);
		Assert.assertEquals(1, guarantees.size());
		Assert.assertEquals(guarantee.getId(), guarantees.get(0).getId());

		IdmIdentityDto guaranteeIdentityTwo = this.getHelper().createIdentity();

		this.getHelper().createRoleGuarantee(role, guaranteeIdentityTwo);
		guarantees = this.findGuarantees(role);
		Assert.assertEquals(2, guarantees.size());

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Second guarantor had to be deleted!
		guarantees = this.findGuarantees(role);
		Assert.assertEquals(1, guarantees.size());
		Assert.assertEquals(guarantee.getId(), guarantees.get(0).getId());
	}

	@Test
	public void testExportAndImportRoleGuaranteeByRole() {
		IdmRoleDto role = createRole();
		IdmRoleDto guaranteeRole = this.getHelper().createRole();

		IdmRoleGuaranteeRoleDto guarantee = this.getHelper().createRoleGuaranteeRole(role, guaranteeRole);

		// Make export, upload and import
		IdmExportImportDto importBatch = executeExportAndImport(role, RoleExportBulkAction.NAME,
				ImmutableMap.of(EXECUTE_BEFORE_DTO_DELETE, this::deleteAllSubroles));

		role = roleService.get(role.getId());
		Assert.assertNotNull(role);

		List<IdmRoleGuaranteeRoleDto> guarantees = this.findRoleGuarantees(role);
		Assert.assertEquals(1, guarantees.size());
		Assert.assertEquals(guarantee.getId(), guarantees.get(0).getId());

		IdmRoleDto guaranteeRoleTwo = this.getHelper().createRole();

		this.getHelper().createRoleGuaranteeRole(role, guaranteeRoleTwo);
		guarantees = this.findRoleGuarantees(role);
		Assert.assertEquals(2, guarantees.size());

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Second guarantor had to be deleted!
		guarantees = this.findRoleGuarantees(role);
		Assert.assertEquals(1, guarantees.size());
		Assert.assertEquals(guarantee.getId(), guarantees.get(0).getId());
	}

	@Test
	public void testExportAndImportRolePolicy() {
		IdmRoleDto role = createRole();
		IdmAuthorizationPolicyDto policy = this.getHelper().createAuthorizationPolicy(role.getId(),
				CoreGroupPermission.SCHEDULER, IdmLongRunningTask.class, SelfLongRunningTaskEvaluator.class,
				IdmBasePermission.READ);

		// Make export, upload and import
		IdmExportImportDto importBatch = executeExportAndImport(role, RoleExportBulkAction.NAME,
				ImmutableMap.of(EXECUTE_BEFORE_DTO_DELETE, this::deleteAllSubroles));

		role = roleService.get(role.getId());
		Assert.assertNotNull(role);

		List<IdmAuthorizationPolicyDto> policies = this.findPolicy(role);
		Assert.assertEquals(1, policies.size());
		Assert.assertEquals(policy.getId(), policies.get(0).getId());

		this.getHelper().createAuthorizationPolicy(role.getId(), CoreGroupPermission.SCHEDULER,
				IdmLongRunningTask.class, SelfLongRunningTaskEvaluator.class, IdmBasePermission.ADMIN);

		policies = this.findPolicy(role);
		Assert.assertEquals(2, policies.size());

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Second policy had to be deleted!
		policies = this.findPolicy(role);
		Assert.assertEquals(1, policies.size());
		Assert.assertEquals(policy.getId(), policies.get(0).getId());
	}

	@Test
	public void testExportAndImportRoleCatalogue() {
		IdmRoleDto role = createRole();
		IdmRoleCatalogueDto parentCatalogue = this.getHelper().createRoleCatalogue();
		IdmRoleCatalogueDto catalogue = this.getHelper().createRoleCatalogue(getHelper().createName(), parentCatalogue.getId());
		IdmRoleCatalogueDto notUseCatalogue = this.getHelper().createRoleCatalogue();
		IdmRoleCatalogueRoleDto roleCatalogueRole = this.getHelper().createRoleCatalogueRole(role, catalogue);

		// Make export, upload and import
		IdmExportImportDto importBatch = executeExportAndImport(role, RoleExportBulkAction.NAME,
				ImmutableMap.of(EXECUTE_BEFORE_DTO_DELETE, this::deleteAllSubroles));

		role = roleService.get(role.getId());
		Assert.assertNotNull(role);

		List<IdmRoleCatalogueRoleDto> roleCatalogues = this.findCatalogue(role);
		Assert.assertEquals(1, roleCatalogues.size());
		Assert.assertEquals(roleCatalogueRole.getId(), roleCatalogues.get(0).getId());
		
		// Delete all catalogues (catalogue and parentCatalogue should be created again)
		roleCatalogueService.delete(catalogue);
		roleCatalogueService.delete(parentCatalogue);
		roleCatalogueService.delete(notUseCatalogue);

		IdmRoleCatalogueDto catalogueTwo = this.getHelper().createRoleCatalogue();
		this.getHelper().createRoleCatalogueRole(role, catalogueTwo);
		roleCatalogues = this.findCatalogue(role);
		Assert.assertEquals(1, roleCatalogues.size());

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Second policy had to be deleted!
		roleCatalogues = this.findCatalogue(role);
		Assert.assertEquals(1, roleCatalogues.size());
		Assert.assertEquals(roleCatalogueRole.getId(), roleCatalogues.get(0).getId());
		
		// Catalogue with parent had to be created.
		Assert.assertNotNull(roleCatalogueService.get(catalogue.getId()));
		Assert.assertNotNull(roleCatalogueService.get(parentCatalogue.getId()));
		// Not using catalogue was not created.
		Assert.assertNull(roleCatalogueService.get(notUseCatalogue.getId()));
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

	private IdmRoleFormAttributeDto createRoleFormAttribute(IdmRoleDto role, String definitionCode,
			String attributeCode) {
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
		if (role.getIdentityRoleAttributeDefinition() == null
				|| !role.getIdentityRoleAttributeDefinition().equals(definition.getId())) {
			role.setIdentityRoleAttributeDefinition(definition.getId());
			role = roleService.save(role);
		}
		//
		return roleFormAttributeService.addAttributeToSubdefintion(role, attribute);
	}

	private List<IdmRoleCatalogueRoleDto> findCatalogue(IdmRoleDto role) {
		IdmRoleCatalogueRoleFilter filter = new IdmRoleCatalogueRoleFilter();
		filter.setRoleId(role.getId());

		return roleCatalogueRoleService.find(filter, null).getContent();
	}

	private List<IdmAuthorizationPolicyDto> findPolicy(IdmRoleDto role) {
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
		filter.setRoleId(role.getId());

		return authorizationPolicyService.find(filter, null).getContent();
	}

	private List<IdmRoleGuaranteeDto> findGuarantees(IdmRoleDto role) {
		IdmRoleGuaranteeFilter filter = new IdmRoleGuaranteeFilter();
		filter.setRole(role.getId());

		return roleGuaranteeService.find(filter, null).getContent();
	}

	private List<IdmRoleGuaranteeRoleDto> findRoleGuarantees(IdmRoleDto role) {
		IdmRoleGuaranteeRoleFilter filter = new IdmRoleGuaranteeRoleFilter();
		filter.setRole(role.getId());

		return roleGuaranteeRoleService.find(filter, null).getContent();
	}

	private List<IdmIncompatibleRoleDto> findIncompatibilites(IdmRoleDto role) {
		IdmIncompatibleRoleFilter filter = new IdmIncompatibleRoleFilter();
		filter.setRoleId(role.getId());

		return incompatibleRoleService.find(filter, null).getContent();
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
	
	private void deleteAllSubroles(IdmRoleDto role) {
		findAllSubRoles(role).forEach(subRole -> {
			roleCompositionService.delete(subRole);
		});
	}

}
