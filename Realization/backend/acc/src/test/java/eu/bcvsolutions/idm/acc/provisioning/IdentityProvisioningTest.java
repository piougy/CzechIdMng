package eu.bcvsolutions.idm.acc.provisioning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AssignedRoleDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.impl.IdentityProvisioningExecutor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Identity provisioning tests
 * 
 * @author Vít Švanda
 *
 */
@Service
public class IdentityProvisioningTest extends AbstractIntegrationTest {
	@Autowired
	private TestHelper helper;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testAssignedRoles() {
		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		SysSystemMappingDto defaultMapping = helper.getDefaultMapping(systemDto);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(systemDto.getId());

		List<SysSchemaAttributeDto> schemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null)
				.getContent();
		SysSchemaAttributeDto descriptionSchemaAttribute = schemaAttributes.stream()
				.filter(attribute -> TestHelper.ATTRIBUTE_MAPPING_DESCRIPTION.equals(attribute.getName())).findFirst()
				.get();

		SysSystemAttributeMappingDto attributeAssignedRoles = new SysSystemAttributeMappingDto();
		attributeAssignedRoles.setUid(false);
		attributeAssignedRoles.setEntityAttribute(true);
		attributeAssignedRoles.setIdmPropertyName(IdentityProvisioningExecutor.ASSIGNED_ROLES_FIELD);
		attributeAssignedRoles.setTransformToResourceScript("if(attributeValue == null) " + System.lineSeparator()
				+ "{return null;}" + System.lineSeparator() + " String result = '';" + System.lineSeparator()
				+ " for(Object assignedRole : attributeValue)" + System.lineSeparator()
				+ " {result = result + (assignedRole.toString())};" + System.lineSeparator() + " return result;");
		attributeAssignedRoles.setName(descriptionSchemaAttribute.getName());
		attributeAssignedRoles.setSchemaAttribute(descriptionSchemaAttribute.getId());
		attributeAssignedRoles.setSystemMapping(defaultMapping.getId());
		schemaAttributeMappingService.save(attributeAssignedRoles);
		IdmRoleDto roleWithSystem = helper.createRole();
		IdmRoleDto roleWithOutSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, systemDto);
		IdmIdentityDto identity = helper.createIdentity();

		helper.createIdentityRole(identity, roleWithOutSystem, null, null);
		helper.createIdentityRole(identity, roleWithSystem, null, null);
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		List<IdmIdentityRoleDto> identityRoles = identityRoleService
				.find(identityRoleFilter,
						new PageRequest(0, Integer.MAX_VALUE, new Sort(IdmIdentityRole_.created.getName())))
				.getContent();

		TestResource resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		String valueOnResource = resource.getDescrip();
		String result = "";
		for (IdmIdentityRoleDto identityRole : identityRoles) {
			IdmFormInstanceDto formInstanceDto = identityRoleService.getRoleAttributeValues(identityRole);
			identityRole.getEavs().clear();
			identityRole.getEavs().add(formInstanceDto);
			result = result + IdentityProvisioningExecutor.convertToAssignedRoleDto(identityRole).toString();
		}
		assertEquals(result, valueOnResource);
	}

	@Test
	public void testAssignedRolesForSystem() {
		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		SysSystemMappingDto defaultMapping = helper.getDefaultMapping(systemDto);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(systemDto.getId());

		List<SysSchemaAttributeDto> schemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null)
				.getContent();
		SysSchemaAttributeDto descriptionSchemaAttribute = schemaAttributes.stream()
				.filter(attribute -> TestHelper.ATTRIBUTE_MAPPING_DESCRIPTION.equals(attribute.getName())).findFirst()
				.get();

		SysSystemAttributeMappingDto attributeAssignedRoles = new SysSystemAttributeMappingDto();
		attributeAssignedRoles.setUid(false);
		attributeAssignedRoles.setEntityAttribute(true);
		attributeAssignedRoles.setIdmPropertyName(IdentityProvisioningExecutor.ASSIGNED_ROLES_FOR_SYSTEM_FIELD);
		attributeAssignedRoles.setTransformToResourceScript("if(attributeValue == null) " + System.lineSeparator()
				+ "{return null;}" + System.lineSeparator() + " String result = '';" + System.lineSeparator()
				+ " for(Object assignedRole : attributeValue)" + System.lineSeparator()
				+ " {result = result + (assignedRole.toString())};" + System.lineSeparator() + " return result;");
		attributeAssignedRoles.setName(descriptionSchemaAttribute.getName());
		attributeAssignedRoles.setSchemaAttribute(descriptionSchemaAttribute.getId());
		attributeAssignedRoles.setSystemMapping(defaultMapping.getId());
		schemaAttributeMappingService.save(attributeAssignedRoles);
		IdmRoleDto roleWithSystem = helper.createRole();
		IdmRoleDto roleWithOutSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, systemDto);
		IdmIdentityDto identity = helper.createIdentity();

		helper.createIdentityRole(identity, roleWithOutSystem, null, null);
		IdmIdentityRoleDto identityRoleWithSystem = helper.createIdentityRole(identity, roleWithSystem, null, null);
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());

		TestResource resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		String valueOnResource = resource.getDescrip();
		String result = "";
		IdmFormInstanceDto formInstanceDto = identityRoleService.getRoleAttributeValues(identityRoleWithSystem);
		identityRoleWithSystem.getEavs().clear();
		identityRoleWithSystem.getEavs().add(formInstanceDto);
		result = IdentityProvisioningExecutor.convertToAssignedRoleDto(identityRoleWithSystem).toString();
		assertEquals(result, valueOnResource);
	}

	@Test
	public void testConvertToAssignedRoleDto() {

		IdmRoleDto role = helper.createRole();
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role, LocalDate.now(),
				LocalDate.now().plusDays(1));

		AssignedRoleDto result = IdentityProvisioningExecutor.convertToAssignedRoleDto(identityRole);

		assertNotNull(result);
		assertEquals(identityRole.getId(), result.getId());
		assertEquals(identityRole.getValidFrom(), result.getValidFrom());
		assertEquals(identityRole.getValidTill(), result.getValidTill());
		assertEquals(identityRole.getIdentityContract(), result.getIdentityContract().getId());
		assertEquals(identityRole.getRole(), result.getRole().getId());
		assertEquals(identityRole.getDirectRole(),
				identityRole.getDirectRole() != null ? result.getDirectRole().getId() : null);
		assertEquals(identityRole.getContractPosition(),
				identityRole.getContractPosition() != null ? result.getContractPosition().getId() : null);
		assertEquals(identityRole.getRoleComposition(),
				identityRole.getRoleComposition() != null ? result.getRoleComposition().getId() : null);
		assertEquals(identityRole.getAutomaticRole(),
				identityRole.getAutomaticRole() != null ? result.getRoleTreeNode().getId() : null);

	}

}
