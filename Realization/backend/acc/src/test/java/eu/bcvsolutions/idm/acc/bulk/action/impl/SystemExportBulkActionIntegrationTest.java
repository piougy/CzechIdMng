package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.identityconnectors.framework.common.objects.OperationOptions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncContractConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.ExportImportType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.api.service.ImportManager;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ImportTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcObjectPoolConfiguration;
import eu.bcvsolutions.idm.test.api.AbstractExportBulkActionTest;

/**
 * Export system integration test
 * 
 * @author Vít Švanda
 *
 */
public class SystemExportBulkActionIntegrationTest extends AbstractExportBulkActionTest {

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysProvisioningBreakConfigService provisioningBreakService;
	@Autowired
	private SysProvisioningBreakRecipientService provisioningBreakRecipientService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysSyncConfigService synchronizationConfigService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired
	private TestHelper helper;
	@Autowired
	private ImportManager importManager;
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private SysRemoteServerService remoteServerService;
	@Autowired 
	private ConfidentialStorage confidentialStorage;
	
	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	/**
	 * Green line test.
	 */
	@Test
	public void testExportAndImportSystem() {
		SysSystemDto system = createSystem();
		Assert.assertFalse(system.isDisabled());

		// Make export, upload and import
		executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);
		Assert.assertTrue(system.isDisabled());
	}

	@Test
	public void testExportAndImportRemoteSystem() {
		SysConnectorServerDto connectorServer = new SysConnectorServerDto();
		connectorServer.setHost(getHelper().createName());
		connectorServer.setPort(43);
		connectorServer.setPassword(new GuardedString("password"));
		connectorServer = remoteServerService.save(connectorServer);
		SysSystemDto system = createSystem();
		system.setRemoteServer(connectorServer.getId());
		system = systemService.save(system);
		Assert.assertFalse(system.isDisabled());

		// Make export, upload, delete system and import
		executeExportAndImport(system, SystemExportBulkAction.NAME, 
				ImmutableMap.of(EXECUTE_BEFORE_DTO_DELETE, this::deleteRemoteServer));
		
		system = systemService.get(system.getId());
		Assert.assertNotNull(system);
		Assert.assertNotNull(system.getRemoteServer());
		Assert.assertTrue(system.isDisabled());
		Assert.assertNotNull(system.getConnectorServer());
		Assert.assertNotNull(system.getConnectorServer().getHost());
		// Password is not imported -> for new created system must be null!
		Assert.assertNull(system.getConnectorServer().getPassword());
		//
		// get remote server
		SysConnectorServerDto importedConnectorServer = remoteServerService.get(system.getRemoteServer());
		Assert.assertEquals(connectorServer.getHost(), importedConnectorServer.getHost());
		Assert.assertEquals(connectorServer.getPort(), importedConnectorServer.getPort());
		Assert.assertEquals(connectorServer.isUseSsl(), importedConnectorServer.isUseSsl());
		Assert.assertEquals(connectorServer.getTimeout(), importedConnectorServer.getTimeout());
		// Password is not imported -> for new created remote server must be null!
		Assert.assertTrue(remoteServerService.getPassword(importedConnectorServer.getId()).asString().isEmpty());
	}
	
	@Test
	public void testExportAndUseRemoteSystem() {
		SysConnectorServerDto connectorServer = new SysConnectorServerDto();
		String host = getHelper().createName();
		connectorServer.setHost(host);
		connectorServer.setPort(43);
		String password = "password";
		connectorServer.setPassword(new GuardedString(password));
		connectorServer = remoteServerService.save(connectorServer);
		SysSystemDto system = createSystem();
		system.setRemoteServer(connectorServer.getId());
		system = systemService.save(system);
		Assert.assertFalse(system.isDisabled());

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);
		
		system = systemService.get(system.getId());
		Assert.assertNotNull(system);
		Assert.assertNotNull(system.getRemoteServer());
		Assert.assertTrue(system.isDisabled());
		Assert.assertNotNull(system.getConnectorServer());
		Assert.assertNotNull(system.getConnectorServer().getHost());
		// Password is preserved from remote server
		Assert.assertEquals(password, confidentialStorage.getGuardedString(system.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD).asString());
		//
		// get remote server
		SysConnectorServerDto importedConnectorServer = remoteServerService.get(system.getRemoteServer());
		Assert.assertEquals(connectorServer.getId(), importedConnectorServer.getId());
		Assert.assertEquals(connectorServer.getHost(), importedConnectorServer.getHost());
		Assert.assertEquals(connectorServer.getPort(), importedConnectorServer.getPort());
		Assert.assertEquals(connectorServer.isUseSsl(), importedConnectorServer.isUseSsl());
		Assert.assertEquals(connectorServer.getTimeout(), importedConnectorServer.getTimeout());
		// Password is preserved
		Assert.assertEquals(password, remoteServerService.getPassword(importedConnectorServer.getId()).asString());
		
		// delete remote server and create new with the same setting => find by example
		deleteRemoteServer(system);
		//
		connectorServer = new SysConnectorServerDto();
		connectorServer.setHost(host);
		connectorServer.setPort(43);
		connectorServer.setPassword(new GuardedString(password));
		connectorServer = remoteServerService.save(connectorServer);
		
		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());
		//
		system = systemService.get(system);
		importedConnectorServer = remoteServerService.get(system.getRemoteServer());
		Assert.assertEquals(connectorServer.getId(), importedConnectorServer.getId());
		Assert.assertEquals(connectorServer.getHost(), importedConnectorServer.getHost());
		Assert.assertEquals(connectorServer.getPort(), importedConnectorServer.getPort());
		Assert.assertEquals(connectorServer.isUseSsl(), importedConnectorServer.isUseSsl());
		Assert.assertEquals(connectorServer.getTimeout(), importedConnectorServer.getTimeout());
		// Password is preserved
		Assert.assertEquals(password, confidentialStorage.getGuardedString(system.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD).asString());
		Assert.assertEquals(password, remoteServerService.getPassword(importedConnectorServer.getId()).asString());
	}

	@Test
	public void testExportAndImportConnectorConfigs() {
		SysSystemDto system = createSystem();
		// Load configurations
		IcConnectorConfiguration originalConnectorConfiguration = systemService.getConnectorConfiguration(system);
		Assert.assertNotNull(originalConnectorConfiguration);
		Assert.assertTrue(originalConnectorConfiguration.getConfigurationProperties().getProperties().size() > 0);

		// Make export, upload, delete system and import
		executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		checkConnectorConfiguration(system, originalConnectorConfiguration);
	}
	
	@Test
	public void testExportAndImportConnectorConfigsForTwoSystems() {
		SysSystemDto systemOne = createSystem();
		SysSystemDto systemTwo = createSystem();
		// Load configurations
		IcConnectorConfiguration originalConnectorConfigurationOne = systemService.getConnectorConfiguration(systemOne);
		Assert.assertNotNull(originalConnectorConfigurationOne);
		Assert.assertTrue(originalConnectorConfigurationOne.getConfigurationProperties().getProperties().size() > 0);
	
		IcConnectorConfiguration originalConnectorConfigurationTwo = systemService.getConnectorConfiguration(systemTwo);
		Assert.assertNotNull(originalConnectorConfigurationTwo);
		Assert.assertTrue(originalConnectorConfigurationTwo.getConfigurationProperties().getProperties().size() > 0);

		ArrayList<AbstractDto> systems = Lists.newArrayList(systemOne, systemTwo);

		// Make export, upload, delete system and import
		executeExportAndImport(systems, SysSystemDto.class, SystemExportBulkAction.NAME, null);
		
		systemOne = systemService.get(systemOne.getId());
		Assert.assertNotNull(systemOne);
		systemTwo = systemService.get(systemTwo.getId());
		Assert.assertNotNull(systemTwo);

		checkConnectorConfiguration(systemOne, originalConnectorConfigurationOne);
		checkConnectorConfiguration(systemTwo, originalConnectorConfigurationTwo);
	}

	private void checkConnectorConfiguration(SysSystemDto systemOne, IcConnectorConfiguration originalConnectorConfigurationOne) {
		IcConnectorConfiguration connectorConfiguration = systemService.getConnectorConfiguration(systemOne);
		Assert.assertNotNull(connectorConfiguration);

		// Number of imported config non-confidential properties must be same as in
		// source system.
		long originalCountNoConfidentialConfigProperties = originalConnectorConfigurationOne.getConfigurationProperties()
				.getProperties().stream()//
				.filter(property -> !property.getName().equals("password"))//
				.count();
		long countNoConfidentialConfigProperties = connectorConfiguration.getConfigurationProperties().getProperties()
				.stream()//
				.filter(property -> !property.getName().equals("password"))//
				.count();

		Assert.assertEquals(originalCountNoConfidentialConfigProperties, countNoConfidentialConfigProperties);

		// Confidential properties are not imported!
		long originalCountConfidentialConfigProperties = originalConnectorConfigurationOne.getConfigurationProperties()
				.getProperties().stream()//
				.filter(property -> property.getName().equals("password"))//
				.count();
		long countConfidentialConfigProperties = connectorConfiguration.getConfigurationProperties().getProperties()
				.stream()//
				.filter(property -> property.getName().equals("password"))//
				.count();

		Assert.assertEquals(1, originalCountConfidentialConfigProperties);
		// Confidential properties are not imported!
		Assert.assertEquals(0, countConfidentialConfigProperties);
	}

	@Test
	public void testExportAndImportConnectorPoolingConfigs() {
		SysSystemDto system = createSystem();

		IcConnectorInstance connectorInstance = systemService.getConnectorInstance(system);
		Assert.assertNotNull(connectorInstance);
		IdmFormDefinitionDto formDefinition = systemService.getPoolingConnectorFormDefinition(connectorInstance);
		Assert.assertNotNull(formDefinition);
		systemService.save(system);

		List<IdmFormValueDto> values = formService.getValues(system, formDefinition);
		Assert.assertNotNull(values);
		Assert.assertEquals(0, values.size());
		values = Lists.newArrayList();

		IdmFormValueDto formValueDto = new IdmFormValueDto(
				formService.getAttribute(formDefinition, SysSystemService.POOLING_SUPPORTED_PROPERTY));
		// Change value
		formValueDto.setValue(true);
		values.add(formValueDto);

		formValueDto = new IdmFormValueDto(
				formService.getAttribute(formDefinition, SysSystemService.MAX_IDLE_PROPERTY));
		// Change value
		formValueDto.setValue(111);
		values.add(formValueDto);

		formValueDto = new IdmFormValueDto(
				formService.getAttribute(formDefinition, SysSystemService.MAX_OBJECTS_PROPERTY));
		// Change value
		formValueDto.setValue(222);
		values.add(formValueDto);

		formValueDto = new IdmFormValueDto(
				formService.getAttribute(formDefinition, SysSystemService.MIN_IDLE_PROPERTY));
		// Change value
		formValueDto.setValue(333);
		values.add(formValueDto);

		formValueDto = new IdmFormValueDto(
				formService.getAttribute(formDefinition, SysSystemService.MAX_WAIT_PROPERTY));
		// Change value
		formValueDto.setValue((long) 444);
		values.add(formValueDto);

		formValueDto = new IdmFormValueDto(
				formService.getAttribute(formDefinition, SysSystemService.MIN_TIME_TO_EVIC_PROPERTY));
		// Change value
		formValueDto.setValue((long) 555);
		values.add(formValueDto);

		// Save all values
		formService.saveValues(system, formDefinition, values);

		IcConnectorConfiguration connectorConfiguration = systemService.getConnectorConfiguration(system);
		Assert.assertNotNull(connectorConfiguration);
		Assert.assertTrue(connectorConfiguration.isConnectorPoolingSupported());

		IcObjectPoolConfiguration poolConfiguration = connectorConfiguration.getConnectorPoolConfiguration();
		Assert.assertNotNull(poolConfiguration);

		Assert.assertEquals(111, poolConfiguration.getMaxIdle());
		Assert.assertEquals(222, poolConfiguration.getMaxObjects());
		Assert.assertEquals(333, poolConfiguration.getMinIdle());
		Assert.assertEquals(444, poolConfiguration.getMaxWait());
		Assert.assertEquals(555, poolConfiguration.getMinEvictableIdleTimeMillis());

		// Make export, upload, delete system and import
		executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		connectorConfiguration = systemService.getConnectorConfiguration(system);
		Assert.assertNotNull(connectorConfiguration);
		Assert.assertTrue(connectorConfiguration.isConnectorPoolingSupported());

		poolConfiguration = connectorConfiguration.getConnectorPoolConfiguration();
		Assert.assertNotNull(poolConfiguration);

		Assert.assertEquals(111, poolConfiguration.getMaxIdle());
		Assert.assertEquals(222, poolConfiguration.getMaxObjects());
		Assert.assertEquals(333, poolConfiguration.getMinIdle());
		Assert.assertEquals(444, poolConfiguration.getMaxWait());
		Assert.assertEquals(555, poolConfiguration.getMinEvictableIdleTimeMillis());
	}
	
	@Test
	public void testExportAndImportConnectorOperationOptions() {
		final Integer testPageSizeVal = new Integer(123456);
		final String testAttrsToGetVal = "testVAlue1";
		SysSystemDto system = createSystem();

		IcConnectorInstance connectorInstance = systemService.getConnectorInstance(system);
		Assert.assertNotNull(connectorInstance);
		IdmFormDefinitionDto formDefinition = systemService.getOperationOptionsConnectorFormDefinition(connectorInstance);
		Assert.assertNotNull(formDefinition);
		systemService.save(system);
		
		List<IdmFormValueDto> values = formService.getValues(system, formDefinition);
		Assert.assertNotNull(values);
		Assert.assertEquals(0, values.size());
		values = Lists.newArrayList();
		
		IdmFormValueDto formValueDto = new IdmFormValueDto(
				formService.getAttribute(formDefinition, OperationOptions.OP_PAGE_SIZE));
		// Change value
		formValueDto.setValue(testPageSizeVal);
		values.add(formValueDto);
		
		formValueDto = new IdmFormValueDto(
				formService.getAttribute(formDefinition, OperationOptions.OP_ATTRIBUTES_TO_GET));
		// Change value
		formValueDto.setValue(testAttrsToGetVal);
		values.add(formValueDto);
		
		// Save all values
		formService.saveValues(system, formDefinition, values);
		
		IcConnectorConfiguration connectorConfiguration = systemService.getConnectorConfiguration(system);
		Assert.assertNotNull(connectorConfiguration);
		Map<String, Object> optionMap  = connectorConfiguration.getSystemOperationOptions();
		Assert.assertEquals(testPageSizeVal, (Integer)optionMap.get(OperationOptions.OP_PAGE_SIZE));
		Assert.assertEquals(testAttrsToGetVal, (String)optionMap.get(OperationOptions.OP_ATTRIBUTES_TO_GET));
		
		// Make export, upload, delete system and import
		executeExportAndImport(system, SystemExportBulkAction.NAME);
		
		system = systemService.get(system.getId());
		Assert.assertNotNull(system);
		connectorConfiguration = systemService.getConnectorConfiguration(system);
		Assert.assertNotNull(connectorConfiguration);
		
		optionMap  = connectorConfiguration.getSystemOperationOptions();
		Assert.assertEquals(testPageSizeVal, (Integer)optionMap.get(OperationOptions.OP_PAGE_SIZE));
		Assert.assertEquals(testAttrsToGetVal, (String)optionMap.get(OperationOptions.OP_ATTRIBUTES_TO_GET));
	}

	@Test
	public void testExportAndImportMapping() {
		SysSystemDto system = createSystem();
		// Load configurations
		List<SysSystemMappingDto> mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto originalMapping = mappings.get(0);

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto mapping = mappings.get(0);
		Assert.assertEquals(originalMapping.getId(), mapping.getId());

		SysSchemaObjectClassDto objectClassDto = new SysSchemaObjectClassDto();
		objectClassDto.setId(mapping.getObjectClass());
		helper.createMappingSystem(SystemEntityType.ROLE, objectClassDto);
		mappings = findMappings(system);
		Assert.assertEquals(2, mappings.size());

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Second mapping had to be deleted!
		mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		mapping = mappings.get(0);
		Assert.assertEquals(originalMapping.getId(), mapping.getId());
	}

	@Test
	public void testExportAndImportMappingWithTreeType() {
		SysSystemDto system = createSystem();
		IdmTreeTypeDto treeType = helper.createTreeType();

		// Load configurations
		List<SysSystemMappingDto> mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto originalMapping = mappings.get(0);
		originalMapping.setTreeType(treeType.getId());
		originalMapping = systemMappingService.save(originalMapping);

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto mapping = mappings.get(0);
		Assert.assertEquals(originalMapping.getId(), mapping.getId());

		SysSchemaObjectClassDto objectClassDto = new SysSchemaObjectClassDto();
		objectClassDto.setId(mapping.getObjectClass());
		helper.createMappingSystem(SystemEntityType.ROLE, objectClassDto);
		mappings = findMappings(system);
		Assert.assertEquals(2, mappings.size());

		// Remove original tree-type. And create new with same code (simulate a different IdM ... same tree-type with different IDs).
		originalMapping.setTreeType(null);
		originalMapping = systemMappingService.save(originalMapping);
		treeTypeService.delete(treeType);
		IdmTreeTypeDto newTreeType = helper.createTreeType(treeType.getCode());

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Second mapping had to be deleted!
		mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		mapping = mappings.get(0);
		Assert.assertEquals(originalMapping.getId(), mapping.getId());
		Assert.assertEquals(newTreeType.getId(), mapping.getTreeType());
	}

	@Test
	public void testExportAndImportAttributeMapping() {
		SysSystemDto system = createSystem();
		List<SysSystemMappingDto> mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto originalMapping = mappings.get(0);
		List<SysSystemAttributeMappingDto> originalAttributes = findAttributeMappings(system);

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		mappings = findMappings(system);
		List<SysSystemAttributeMappingDto> attributes = findAttributeMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto mapping = mappings.get(0);
		Assert.assertEquals(originalMapping.getId(), mapping.getId());
		Assert.assertEquals(originalAttributes.size(), attributes.size());

		// Create redundant attribute
		SysSystemAttributeMappingDto redundantAttribute = new SysSystemAttributeMappingDto();
		redundantAttribute.setSystemMapping(mapping.getId());
		redundantAttribute.setName(getHelper().createName());
		redundantAttribute.setSchemaAttribute(attributes.get(0).getSchemaAttribute());
		redundantAttribute = systemAttributeMappingService.save(redundantAttribute);

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Redundant attribute had to be deleted!
		redundantAttribute = systemAttributeMappingService.get(redundantAttribute.getId());
		Assert.assertNull(redundantAttribute);
	}

	@Test
	public void testExportAndImportSystemSchema() {
		SysSystemDto system = createSystem();
		List<SysSchemaObjectClassDto> schemas = findSchemas(system);
		Assert.assertEquals(1, schemas.size());
		SysSchemaObjectClassDto originalSchema = schemas.get(0);

		// Make export, upload and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		schemas = findSchemas(system);
		Assert.assertEquals(1, schemas.size());

		SysSchemaObjectClassDto schema = new SysSchemaObjectClassDto();
		schema.setObjectClassName(getHelper().createName());
		schema.setSystem(system.getId());
		schema = schemaObjectClassService.save(schema);

		schemas = this.findSchemas(system);
		Assert.assertEquals(2, schemas.size());

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Second incompatibility had to be deleted!
		schemas = this.findSchemas(system);
		Assert.assertEquals(1, schemas.size());
		Assert.assertEquals(originalSchema.getId(), schemas.get(0).getId());
	}

	@Test
	public void testExportAndImportSync() {
		SysSystemDto system = createSystem();
		List<SysSystemMappingDto> mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto originalMapping = mappings.get(0);
		List<SysSystemAttributeMappingDto> originalAttributes = findAttributeMappings(system);
		SysSystemAttributeMappingDto originalAttribute = originalAttributes.get(0);

		SysSyncConfigDto originalSync = new SysSyncConfigDto();
		originalSync.setSystemMapping(originalMapping.getId());
		originalSync.setName(getHelper().createName());
		originalSync.setCorrelationAttribute(originalAttribute.getId());
		originalSync = (SysSyncConfigDto) synchronizationConfigService.save(originalSync);

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		mappings = findMappings(system);
		List<SysSystemAttributeMappingDto> attributes = findAttributeMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto mapping = mappings.get(0);
		Assert.assertEquals(originalMapping.getId(), mapping.getId());
		Assert.assertEquals(originalAttributes.size(), attributes.size());

		// Create redundant sync
		SysSyncConfigDto redundantSync = new SysSyncConfigDto();
		redundantSync.setSystemMapping(originalMapping.getId());
		redundantSync.setName(getHelper().createName());
		redundantSync.setCorrelationAttribute(originalAttribute.getId());
		redundantSync = (SysSyncConfigDto) synchronizationConfigService.save(redundantSync);

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Redundant sync had to be deleted!
		redundantSync = (SysSyncConfigDto) synchronizationConfigService.get(redundantSync.getId());
		Assert.assertNull(redundantSync);
	}

	@Test
	public void testExportAndImportContractSyncAdvancedPairing() {
		SysSystemDto system = createSystem();
		List<SysSystemMappingDto> mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto originalMapping = mappings.get(0);
		List<SysSystemAttributeMappingDto> originalAttributes = findAttributeMappings(system);
		SysSystemAttributeMappingDto originalAttribute = originalAttributes.get(0);

		IdmTreeTypeDto treeType = getHelper().createTreeType();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode(treeType, null);

		SysSyncContractConfigDto originalSync = new SysSyncContractConfigDto();
		originalSync.setSystemMapping(originalMapping.getId());
		originalSync.setName(getHelper().createName());
		originalSync.setCorrelationAttribute(originalAttribute.getId());
		originalSync.setDefaultTreeType(treeType.getId());
		originalSync.setDefaultTreeNode(treeNode.getId());
		originalSync = (SysSyncContractConfigDto) synchronizationConfigService.save(originalSync);

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		mappings = findMappings(system);
		List<SysSystemAttributeMappingDto> attributes = findAttributeMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto mapping = mappings.get(0);
		Assert.assertEquals(originalMapping.getId(), mapping.getId());
		Assert.assertEquals(originalAttributes.size(), attributes.size());

		// Create redundant sync
		SysSyncConfigDto redundantSync = new SysSyncConfigDto();
		redundantSync.setSystemMapping(originalMapping.getId());
		redundantSync.setName(getHelper().createName());
		redundantSync.setCorrelationAttribute(originalAttribute.getId());
		redundantSync = (SysSyncConfigDto) synchronizationConfigService.save(redundantSync);

		// Clear tree type and tree node.
		originalSync.setDefaultTreeType(null);
		originalSync.setDefaultTreeNode(null);
		originalSync = (SysSyncContractConfigDto) synchronizationConfigService.save(originalSync);

		// Delete original tree-type and tree-node and create new with same code (test of advanced paring).
		getHelper().deleteTreeNode(treeNode.getId());
		getHelper().deleteTreeType(treeType.getId());
		IdmTreeTypeDto treeTypeNew = getHelper().createTreeType(treeType.getCode());
		IdmTreeNodeDto treeNodeNew = getHelper().createTreeNode(treeTypeNew, treeNode.getCode(), null);

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Redundant sync had to be deleted!
		redundantSync = (SysSyncConfigDto) synchronizationConfigService.get(redundantSync.getId());
		Assert.assertNull(redundantSync);

		// Check advanced paring for tree-type and tree-node.
		originalSync = (SysSyncContractConfigDto) synchronizationConfigService.get(originalSync.getId());
		Assert.assertNotNull(originalSync);
		Assert.assertEquals(treeTypeNew.getId(), originalSync.getDefaultTreeType());
		Assert.assertEquals(treeNodeNew.getId(), originalSync.getDefaultTreeNode());
	}

	@Test
	public void testExportAndImportSyncExcludedToken() {
		SysSystemDto system = createSystem();
		List<SysSystemMappingDto> mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto originalMapping = mappings.get(0);
		List<SysSystemAttributeMappingDto> originalAttributes = findAttributeMappings(system);
		SysSystemAttributeMappingDto originalAttribute = originalAttributes.get(0);

		SysSyncConfigDto originalSync = new SysSyncConfigDto();
		originalSync.setSystemMapping(originalMapping.getId());
		originalSync.setName(getHelper().createName());
		originalSync.setCorrelationAttribute(originalAttribute.getId());
		originalSync.setToken(helper.createName());
		originalSync = (SysSyncConfigDto) synchronizationConfigService.save(originalSync);

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);
		// New token
		originalSync.setToken(helper.createName());
		originalSync = (SysSyncConfigDto) synchronizationConfigService.save(originalSync);

		// Execute import (check excluded token)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		SysSyncConfigDto currentSync = (SysSyncConfigDto) synchronizationConfigService.get(originalSync.getId());
		// Token was excluded. Value in current sync was not changed.
		Assert.assertEquals(originalSync.getToken(), currentSync.getToken());
	}

	@Test
	public void testExportAndImportNewSyncExcludedToken() {
		SysSystemDto system = createSystem();
		List<SysSystemMappingDto> mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto originalMapping = mappings.get(0);
		List<SysSystemAttributeMappingDto> originalAttributes = findAttributeMappings(system);
		SysSystemAttributeMappingDto originalAttribute = originalAttributes.get(0);

		SysSyncConfigDto originalSync = new SysSyncConfigDto();
		originalSync.setSystemMapping(originalMapping.getId());
		originalSync.setName(getHelper().createName());
		originalSync.setCorrelationAttribute(originalAttribute.getId());
		originalSync.setToken(helper.createName());
		originalSync = (SysSyncConfigDto) synchronizationConfigService.save(originalSync);

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);
		systemService.delete(system);
		system = systemService.get(system.getId());
		Assert.assertNull(system);

		// Execute import (check excluded token)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		SysSyncConfigDto currentSync = (SysSyncConfigDto) synchronizationConfigService.get(originalSync.getId());
		// Token was excluded. Value in current sync was not changed.
		Assert.assertEquals(null, currentSync.getToken());
	}

	@Test
	public void testExportAndImportBreak() {
		SysSystemDto system = createSystem();
		SysProvisioningBreakConfigDto originalBreak = new SysProvisioningBreakConfigDto();
		originalBreak.setSystem(system.getId());
		originalBreak.setOperationType(ProvisioningEventType.CREATE);
		originalBreak.setPeriod(10l);
		originalBreak = provisioningBreakService.save(originalBreak);

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		List<SysProvisioningBreakConfigDto> breaks = findBreaks(system);
		Assert.assertEquals(1, breaks.size());
		Assert.assertEquals(originalBreak.getId(), breaks.get(0).getId());

		// Create redundant break
		SysProvisioningBreakConfigDto redundantBreak = new SysProvisioningBreakConfigDto();
		redundantBreak.setSystem(system.getId());
		redundantBreak.setPeriod(10l);
		redundantBreak.setOperationType(ProvisioningEventType.UPDATE);
		redundantBreak = provisioningBreakService.save(redundantBreak);

		// Execute import (check authoritative mode)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		// Redundant sync had to be deleted!
		redundantBreak = provisioningBreakService.get(redundantBreak.getId());
		Assert.assertNull(redundantBreak);
	}

	@Test
	public void testExportAndImportBreakIdentityAdvancedPairing() {
		SysSystemDto system = createSystem();
		SysProvisioningBreakConfigDto originalBreak = new SysProvisioningBreakConfigDto();
		originalBreak.setSystem(system.getId());
		originalBreak.setOperationType(ProvisioningEventType.CREATE);
		originalBreak.setPeriod(10l);
		originalBreak = provisioningBreakService.save(originalBreak);

		IdmIdentityDto originalRecipient = getHelper().createIdentity();

		SysProvisioningBreakRecipientDto breakRecipient = new SysProvisioningBreakRecipientDto();
		breakRecipient.setIdentity(originalRecipient.getId());
		breakRecipient.setBreakConfig(originalBreak.getId());
		breakRecipient = provisioningBreakRecipientService.save(breakRecipient);

		// Make export, upload, delete system and import.
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		List<SysProvisioningBreakConfigDto> breaks = findBreaks(system);
		Assert.assertEquals(1, breaks.size());
		Assert.assertEquals(originalBreak.getId(), breaks.get(0).getId());

		breakRecipient = provisioningBreakRecipientService.get(breakRecipient.getId());
		Assert.assertNotNull(breakRecipient);
		Assert.assertEquals(originalRecipient.getId(), breakRecipient.getIdentity());

		// Delete original recipient.
		identityService.delete(originalRecipient);
		// Execute failed import (check advanced pairing -> will failed -> identity
		// missing).
		ImportTaskExecutor lrt = new ImportTaskExecutor(importBatch.getId(), false);
		try {
			importManager.internalExecuteImport(importBatch, false, lrt);
			Assert.assertTrue(false);
		} catch (ResultCodeException ex) {
			if (ex.getError() != null && ex.getError().getError() != null
					&& CoreResultCode.IMPORT_ADVANCED_PARING_FAILED_NOT_FOUND.name()
							.equals(ex.getError().getError().getStatusEnum())) {
				// I expect the exception here.				
			}else {
				Assert.assertTrue(false);
			}
		}

		// Create recipient with same user name.
		IdmIdentityDto newRecipient = getHelper().createIdentity(originalRecipient.getUsername());

		// Execute import (check advanced pairing).
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		breakRecipient = provisioningBreakRecipientService.get(breakRecipient.getId());
		Assert.assertNotNull(breakRecipient);
		Assert.assertEquals(newRecipient.getId(), breakRecipient.getIdentity());

	}

	@Test
	public void testExportAndImportBreakRoleAdvancedPairing() {
		SysSystemDto system = createSystem();
		SysProvisioningBreakConfigDto originalBreak = new SysProvisioningBreakConfigDto();
		originalBreak.setSystem(system.getId());
		originalBreak.setOperationType(ProvisioningEventType.CREATE);
		originalBreak.setPeriod(10l);
		originalBreak = provisioningBreakService.save(originalBreak);

		IdmRoleDto originalRecipient = getHelper().createRole();

		SysProvisioningBreakRecipientDto breakRecipient = new SysProvisioningBreakRecipientDto();
		breakRecipient.setRole(originalRecipient.getId());
		breakRecipient.setBreakConfig(originalBreak.getId());
		breakRecipient = provisioningBreakRecipientService.save(breakRecipient);

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		List<SysProvisioningBreakConfigDto> breaks = findBreaks(system);
		Assert.assertEquals(1, breaks.size());
		Assert.assertEquals(originalBreak.getId(), breaks.get(0).getId());

		breakRecipient = provisioningBreakRecipientService.get(breakRecipient.getId());
		Assert.assertNotNull(breakRecipient);
		Assert.assertEquals(originalRecipient.getId(), breakRecipient.getRole());

		// Delete original recipient and create new with same code
		roleService.delete(originalRecipient);
		IdmRoleDto newRecipient = getHelper().createRole(originalRecipient.getCode());

		// Execute import (check advanced pairing)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		breakRecipient = provisioningBreakRecipientService.get(breakRecipient.getId());
		Assert.assertNotNull(breakRecipient);
		Assert.assertEquals(newRecipient.getId(), breakRecipient.getRole());

	}

	@Test
	public void testExportAndImportRoleSystemAdvancedPairing() {
		SysSystemDto system = createSystem();
		IdmRoleDto originalRoleOne = getHelper().createRole();
		IdmRoleDto originalRoleTwo = getHelper().createRole();

		// Set default mapping to provisioning (for easy creation of role-system)
		List<SysSystemMappingDto> mappings = findMappings(system);
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto originalMapping = mappings.get(0);
		originalMapping.setOperationType(SystemOperationType.PROVISIONING);
		originalMapping = systemMappingService.save(originalMapping);

		helper.createRoleSystem(originalRoleOne, system);
		helper.createRoleSystem(originalRoleTwo, system);

		// Make export, upload, delete system and import
		IdmExportImportDto importBatch = executeExportAndImport(system, SystemExportBulkAction.NAME);

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);

		List<SysRoleSystemDto> roleSystems = findRoleSystems(system);
		Assert.assertEquals(2, roleSystems.size());

		// Delete original role and create new with same code.
		roleService.delete(originalRoleOne);
		IdmRoleDto newRoleOne = getHelper().createRole(originalRoleOne.getCode());
		// Delete original roleTwo, for check optional relations feature (is set for
		// role-system).
		roleService.delete(originalRoleTwo);

		// Execute import (check advanced pairing)
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());

		roleSystems = findRoleSystems(system);
		Assert.assertEquals(1, roleSystems.size());
		Assert.assertEquals(newRoleOne.getId(), roleSystems.get(0).getRole());
	}

	private SysSystemDto createSystem() {
		// create test system
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME);
		Assert.assertNotNull(system);
		SysSystemMappingDto mapping = helper.createMapping(system);
		// Change type of mapping to sync (we need to use more schema attribute)
		mapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		systemMappingService.save(mapping);

		return system;
	}

	private List<SysProvisioningBreakConfigDto> findBreaks(SysSystemDto system) {
		SysProvisioningBreakConfigFilter filter = new SysProvisioningBreakConfigFilter();
		filter.setSystemId(system.getId());

		return provisioningBreakService.find(filter, null).getContent();
	}

	private List<SysSchemaObjectClassDto> findSchemas(SysSystemDto system) {
		SysSchemaObjectClassFilter filter = new SysSchemaObjectClassFilter();
		filter.setSystemId(system.getId());

		return schemaObjectClassService.find(filter, null).getContent();
	}

	private List<SysSystemAttributeMappingDto> findAttributeMappings(SysSystemDto system) {
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());

		return systemAttributeMappingService.find(filter, null).getContent();
	}

	private List<SysSystemMappingDto> findMappings(SysSystemDto system) {
		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(system.getId());

		return systemMappingService.find(filter, null).getContent();
	}

	private List<SysRoleSystemDto> findRoleSystems(SysSystemDto system) {
		SysRoleSystemFilter filter = new SysRoleSystemFilter();
		filter.setSystemId(system.getId());

		return roleSystemService.find(filter, null).getContent();
	}
	
	private void deleteRemoteServer(SysSystemDto system) {
		if (system.getRemoteServer() == null) {
			return;
		}
		UUID remoteServerId = system.getRemoteServer();
		system.setRemoteServer(null);
		systemService.save(system);
		//
		remoteServerService.deleteById(remoteServerId);
	}

}
