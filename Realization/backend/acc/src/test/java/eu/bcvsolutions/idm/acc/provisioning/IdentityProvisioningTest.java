package eu.bcvsolutions.idm.acc.provisioning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

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
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
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
	@Autowired
	private FormService formService;
	@Autowired
	private AttachmentManager attachmentManager;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmContractPositionService contractPositionService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;

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
				.filter(attribute -> TestHelper.ATTRIBUTE_MAPPING_DESCRIPTION.equalsIgnoreCase(attribute.getName())).findFirst()
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
				.filter(attribute -> TestHelper.ATTRIBUTE_MAPPING_DESCRIPTION.equalsIgnoreCase(attribute.getName())).findFirst()
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

	@Test
	public void testAttachment() {
		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		SysSystemMappingDto defaultMapping = helper.getDefaultMapping(systemDto);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(systemDto.getId());

		List<SysSchemaAttributeDto> schemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null)
				.getContent();
		SysSchemaAttributeDto descriptionSchemaAttribute = schemaAttributes.stream()
				.filter(attribute -> TestHelper.ATTRIBUTE_MAPPING_DESCRIPTION.equalsIgnoreCase(attribute.getName())).findFirst()
				.get();

		SysSystemAttributeMappingDto attributeByte = new SysSystemAttributeMappingDto();
		attributeByte.setUid(false);
		attributeByte.setEntityAttribute(false);
		attributeByte.setExtendedAttribute(true);
		attributeByte.setIdmPropertyName(getHelper().createName());
		attributeByte.setName(descriptionSchemaAttribute.getName());
		attributeByte.setSchemaAttribute(descriptionSchemaAttribute.getId());
		attributeByte.setSystemMapping(defaultMapping.getId());
		// Transformation data to string
		attributeByte.setTransformToResourceScript("if(attributeValue == null) " + System.lineSeparator()
				+ "{return null;}" + System.lineSeparator() + " return new String(attributeValue.getData());");
		schemaAttributeMappingService.save(attributeByte);
		IdmRoleDto roleWithSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, systemDto);

		// Set type of attribute to attachment
		IdmFormAttributeDto eavAttributeByte = formService.getAttribute(IdmIdentityDto.class,
				attributeByte.getIdmPropertyName());
		eavAttributeByte.setPersistentType(PersistentType.ATTACHMENT);
		eavAttributeByte = formService.saveAttribute(eavAttributeByte);

		// Create attachment with content
		String originalContent = getHelper().createName();
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setName("test.txt");
		attachment.setMimetype("text/plain");
		attachment.setInputData(IOUtils.toInputStream(originalContent));
		attachment.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);

		attachment = attachmentManager.saveAttachment(null, attachment);
		InputStream inputStream = attachmentManager.getAttachmentData(attachment.getId());
		try {
			String content = IOUtils.toString(inputStream);
			assertEquals(originalContent, content);

			// Create form value with attachment
			IdmIdentityDto identity = helper.createIdentity();
			formService.saveValues(identity, eavAttributeByte, Lists.newArrayList(attachment.getId()));

			// Assign the system
			helper.createIdentityRole(identity, roleWithSystem, null, null);
			IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
			identityRoleFilter.setIdentityId(identity.getId());

			TestResource resource = helper.findResource(identity.getUsername());
			assertNotNull(resource);
			String valueOnResource = resource.getDescrip();

			InputStream is = attachmentManager.getAttachmentData(attachment.getId());
			try {
				String data = new String(IOUtils.toByteArray(is));
				assertEquals(data, valueOnResource);
			} finally {
				IOUtils.closeQuietly(is);
			}
		} catch (IOException e) {
			throw new CoreException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	@Test
	public void testProvisioningOnChangeContractPosition() {
		SysSystemDto systemDto = helper.createTestResourceSystem(true);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(systemDto.getId());

		IdmRoleDto roleWithSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, systemDto);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		assertNotNull(primeContract);
		IdmContractPositionDto contractPosition = helper.createContractPosition(primeContract);

		helper.createIdentityRole(identity, roleWithSystem, null, null);

		identity.setFirstName(helper.createName());
		identityService.save(identity);

		TestResource resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		String valueOnResource = resource.getFirstname();
		assertEquals(identity.getFirstName(), valueOnResource);

		// Change first name without call provisioning
		identity.setFirstName(helper.createName());
		identityService.saveInternal(identity);

		resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		assertNotEquals(identity.getFirstName(), resource.getFirstname());

		// Save of position -> must execute provisioning
		contractPositionService.save(contractPosition);

		resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		assertEquals(identity.getFirstName(), resource.getFirstname());
	}

	@Test
	public void testProvisioningOnChangeRoleAttributeValue() {
		SysSystemDto systemDto = helper.createTestResourceSystem(true);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(systemDto.getId());

		IdmRoleDto roleWithSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, systemDto);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		assertNotNull(primeContract);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, roleWithSystem, null, null);

		identity.setFirstName(helper.createName());
		identityService.save(identity);

		TestResource resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		String valueOnResource = resource.getFirstname();
		assertEquals(identity.getFirstName(), valueOnResource);

		// Change first name without call provisioning
		identity.setFirstName(helper.createName());
		identityService.saveInternal(identity);

		resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		assertNotEquals(identity.getFirstName(), resource.getFirstname());

		// Create request
		IdmRoleRequestDto request = getHelper().createRoleRequest(identity);
		// Create change role-concept
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(request.getId());
		conceptRoleRequest.setRole(identityRole.getRole());
		conceptRoleRequest.setIdentityRole(identityRole.getId());
		conceptRoleRequest.setValidFrom(identityRole.getValidFrom());
		conceptRoleRequest.setValidTill(identityRole.getValidTill());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.UPDATE);
		conceptRoleRequest = conceptRoleRequestService.save(conceptRoleRequest);
		conceptRoleRequest.getEavs().clear();

		// Execution of the request must execute provisioning
		request = getHelper().executeRequest(request, false, true);
		// Check request
		assertEquals(RoleRequestState.EXECUTED, request.getState());

		resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		assertEquals(identity.getFirstName(), resource.getFirstname());
	}
}
