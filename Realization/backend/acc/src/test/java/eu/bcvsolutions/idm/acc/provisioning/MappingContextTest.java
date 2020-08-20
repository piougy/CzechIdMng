package eu.bcvsolutions.idm.acc.provisioning;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.MappingContext;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Basic account management tests (tests for identity ACM are in {@link IdentityAccountManagementTest})
 *
 * @author Svanda
 */
public class MappingContextTest extends AbstractIntegrationTest {

	private static final String ATTRIBUTE_NAME = "__NAME__";
	private static final String ATTRIBUTE_EMAIL = "email";

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private SysSystemEntityService systemEntityService;


	@Before
	public void init() {
		loginAsAdmin();
		this.getBean().deleteAllResourceData();
	}

	@After
	public void logout() {
		super.logout();
	}


	@Test
	public void testMappingContext() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		Assert.assertNotNull(system);
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);

		// Create the description attribute (print context as string).
		createDescriptionAttribute(system, mapping);
		// Set context transformation to the mapping.
		mapping = initContextForMapping(mapping);

		IdmRoleDto roleWithSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, system);
		IdmIdentityDto identity = helper.createIdentity();
		helper.createIdentityRole(identity, roleWithSystem, null, null);

		TestResource resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		assertEquals(identity.getFirstName(), resource.getFirstname());
		MappingContext context = new MappingContext();
		context.put("test", "TestValueOne");

		assertEquals(context.toString(), resource.getDescrip());

		// Delete role mapping
		systemMappingService.delete(mapping);
	}

	@Test
	public void testMappingContextContract() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		Assert.assertNotNull(system);
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);

		// Create the description attribute (print context as string).
		createDescriptionAttribute(system, mapping);
		// Set context transformation to the mapping
		// Add identity contracts to the context.
		mapping.setAddContextContracts(true);
		mapping = initContextForMapping(mapping);

		IdmRoleDto roleWithSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, system);
		IdmIdentityDto identity = helper.createIdentity();
		helper.createIdentityContact(identity, null, LocalDate.now(), null);
		List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, contracts.size());

		helper.createIdentityRole(identity, roleWithSystem, null, null);

		TestResource resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		assertEquals(identity.getFirstName(), resource.getFirstname());
		MappingContext context = new MappingContext();
		context.put("test", "TestValueOne");
		context.setContracts(contracts);

		assertEquals(context.toString(), resource.getDescrip());

		// Delete role mapping
		systemMappingService.delete(mapping);
	}

	@Test
	public void testMappingContextIdentityRoles() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		Assert.assertNotNull(system);
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);

		// Create the description attribute (print context as string).
		createDescriptionAttribute(system, mapping);
		// Set context transformation to the mapping.
		// Add identity roles to the context.
		mapping.setAddContextIdentityRoles(true);
		mapping = initContextForMapping(mapping);

		IdmRoleDto roleWithSystem = helper.createRole();
		IdmRoleDto roleWithoutSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, system);
		IdmIdentityDto identity = helper.createIdentity();

		helper.createIdentityRole(identity, roleWithoutSystem, null, null);
		helper.createIdentityRole(identity, roleWithSystem, null, null);

		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		List<IdmIdentityRoleDto> identityRoles = identityRoleService
				.find(identityRoleFilter,
						PageRequest.of(0, Integer.MAX_VALUE, Sort.by(IdmIdentityRole_.created.getName())))
				.getContent();
		Assert.assertEquals(2, identityRoles.size());

		TestResource resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		assertEquals(identity.getFirstName(), resource.getFirstname());
		MappingContext context = new MappingContext();
		context.put("test", "TestValueOne");
		context.setIdentityRoles(identityRoles);

		assertEquals(context.toString(), resource.getDescrip());

		// Delete role mapping
		systemMappingService.delete(mapping);
	}

	@Test
	public void testMappingContextIdentityRolesForSystem() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		Assert.assertNotNull(system);
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);

		// Create the description attribute (print context as string).
		createDescriptionAttribute(system, mapping);
		// Set context transformation to the mapping.
		// Add identity roles for this system to the context.
		mapping.setAddContextIdentityRolesForSystem(true);
		mapping = initContextForMapping(mapping);

		IdmRoleDto roleWithSystem = helper.createRole();
		IdmRoleDto roleWithoutSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, system);
		IdmIdentityDto identity = helper.createIdentity();

		helper.createIdentityRole(identity, roleWithoutSystem, null, null);
		IdmIdentityRoleDto identityRoleWithSystem = helper.createIdentityRole(identity, roleWithSystem, null, null);

		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		List<IdmIdentityRoleDto> identityRoles = identityRoleService
				.find(identityRoleFilter,
						PageRequest.of(0, Integer.MAX_VALUE, Sort.by(IdmIdentityRole_.created.getName())))
				.getContent();
		Assert.assertEquals(2, identityRoles.size());

		TestResource resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		assertEquals(identity.getFirstName(), resource.getFirstname());
		MappingContext context = new MappingContext();
		context.put("test", "TestValueOne");
		context.setIdentityRolesForSystem(Lists.newArrayList(identityRoleWithSystem));

		assertEquals(context.toString(), resource.getDescrip());

		// Delete role mapping
		systemMappingService.delete(mapping);
	}

	@Test
	public void testMappingContextConnectorObject() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		Assert.assertNotNull(system);
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);

		// Create the description attribute (print context as string).
		createDescriptionAttribute(system, mapping);
		// Set context transformation to the mapping
		// Add connector object to the context.
		mapping.setAddContextConnectorObject(true);
		mapping = initContextForMapping(mapping);

		IdmRoleDto roleWithSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, system);
		IdmIdentityDto identity = helper.createIdentity();
		helper.createIdentityContact(identity, null, LocalDate.now(), null);
		List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, contracts.size());

		helper.createIdentityRole(identity, roleWithSystem, null, null);

		TestResource resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		assertEquals(identity.getFirstName(), resource.getFirstname());
		MappingContext context = new MappingContext();
		context.put("test", "TestValueOne");
		assertEquals(context.toString(), resource.getDescrip());

		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, identity.getUsername());
		IcConnectorObject connectorObject = systemEntityService.getConnectorObject(systemEntity);
		Assert.assertNotNull(connectorObject);

		// Invoke provisioning
		identityService.save(identity);
		resource = helper.findResource(identity.getUsername());
		assertNotNull(resource);
		assertEquals(identity.getFirstName(), resource.getFirstname());
		context = new MappingContext();
		context.put("test", "TestValueOne");
		context.setConnectorObject(connectorObject);
		assertEquals(context.toString(), resource.getDescrip());

		// Delete role mapping
		systemMappingService.delete(mapping);
	}

	@Test(expected = ProvisioningException.class)
	public void testMappingContextWrongType() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		Assert.assertNotNull(system);

		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping.setMappingContextScript("return \"WrongType\";");
		mapping = systemMappingService.save(mapping);

		IdmRoleDto roleWithSystem = helper.createRole();
		helper.createRoleSystem(roleWithSystem, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		assertNotNull(primeContract);
		IdmContractPositionDto contractPosition = helper.createContractPosition(primeContract);

		helper.createIdentityRole(identity, roleWithSystem, null, null);
		Assert.fail("ProvisioningException: (Script 'get mapping context' must return 'MappingContext' type!) must be throw!");
	}


	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM " + TestResource.TABLE_NAME);
		q.executeUpdate();
	}

	private SysSystemMappingDto initContextForMapping(SysSystemMappingDto mapping) {
		// Set context transformation to the mapping.
		mapping.setMappingContextScript(
				"context.put(\"test\", \"TestValueOne\");\n" +
						"\n" +
						"return context;");
		mapping = systemMappingService.save(mapping);
		return mapping;
	}

	private void createDescriptionAttribute(SysSystemDto system, SysSystemMappingDto mapping) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		schemaAttributeFilter.setName(helper.ATTRIBUTE_MAPPING_DESCRIPTION);
		SysSchemaAttributeDto descriptionSchemaAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
				.getContent()
				.get(0);
		Assert.assertNotNull(descriptionSchemaAttribute);
		SysSystemAttributeMappingDto descriptionAttribute = new SysSystemAttributeMappingDto();
		descriptionAttribute.setUid(false);
		descriptionAttribute.setEntityAttribute(false);
		descriptionAttribute.setName(descriptionSchemaAttribute.getName());
		descriptionAttribute.setSchemaAttribute(descriptionSchemaAttribute.getId());
		descriptionAttribute.setSystemMapping(mapping.getId());
		descriptionAttribute.setTransformToResourceScript("return context.toString();");
		descriptionAttribute = attributeMappingService.save(descriptionAttribute);
	}

	private SysSystemDto initIdentityData() {

		// create test system
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME);
		Assert.assertNotNull(system);

		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);

		// Create mapping
		SysSystemMappingDto syncSystemMapping = new SysSystemMappingDto();
		syncSystemMapping.setName("default_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(SystemEntityType.IDENTITY);
		syncSystemMapping.setOperationType(SystemOperationType.PROVISIONING);
		syncSystemMapping.setObjectClass(objectClasses.get(0).getId());
		final SysSystemMappingDto syncMapping = systemMappingService.save(syncSystemMapping);
		createIdentityMapping(system, syncMapping);
		return system;

	}

	private void createIdentityMapping(SysSystemDto system, final SysSystemMappingDto entityHandlingResult) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if (ATTRIBUTE_NAME.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(true);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName("username");
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				attributeMappingService.save(attributeMapping);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("firstName");
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				attributeMappingService.save(attributeMapping);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("lastName");
				attributeMapping.setTransformToResourceScript("return context.toString();");
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				attributeMappingService.save(attributeMapping);

			} else if (ATTRIBUTE_EMAIL.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("email");
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				attributeMappingService.save(attributeMapping);

			}
		});
	}

	private MappingContextTest getBean() {
		return applicationContext.getAutowireCapableBeanFactory().createBean(this.getClass());
	}

}