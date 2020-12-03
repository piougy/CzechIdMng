package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.connector.AbstractConnectorType;
import eu.bcvsolutions.idm.acc.connector.AbstractJdbcConnectorType;
import eu.bcvsolutions.idm.acc.connector.CsvConnectorType;
import eu.bcvsolutions.idm.acc.connector.DefaultConnectorType;
import eu.bcvsolutions.idm.acc.connector.PostgresqlConnectorType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.rest.impl.SysSystemController;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for connector types
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Transactional
public class DefaultConnectorManagerTest extends AbstractIntegrationTest {

	@Autowired
	private CsvConnectorType csvConnectorType;
	@Autowired
	private SysSystemController systemController;
	@Autowired
	private TestHelper helper;
	@Autowired
	private ConnectorManager connectorManager;
	@Autowired
	private SysRoleSystemService roleSystemService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testSupportsConnectorTypes() {
		String defaultTableConnectorName = "net.tirasa.connid.bundles.db.table.DatabaseTableConnector";

		Resources<ConnectorTypeDto> supportedTypes = systemController.getSupportedTypes();

		// Find connector without connector type (it is default table connector
		// = table connector has tree connector types and one default connector type).
		DefaultConnectorType mockDefaultConnectorType = new DefaultConnectorType();
		ConnectorTypeDto defaultConnectorTypeDto = supportedTypes.getContent()
				.stream()
				.filter(connectorTypeDto -> defaultTableConnectorName.equals(connectorTypeDto.getId()))
				.findFirst()
				.orElse(null);
		assertNotNull(defaultConnectorTypeDto);
		assertEquals(defaultConnectorTypeDto.getIconKey(), mockDefaultConnectorType.getIconKey());

		// Find PostgreSQL connector (table connector has tree connector types and one default connector type).
		ConnectorTypeDto postgresqlConnectorTypeDto = supportedTypes.getContent()
				.stream()
				.filter(connectorTypeDto -> PostgresqlConnectorType.NAME.equals(connectorTypeDto.getId()))
				.findFirst()
				.orElse(null);
		assertNotNull(postgresqlConnectorTypeDto);

		// Find CSV connector type
		ConnectorTypeDto csvConnectorTypeDto = supportedTypes.getContent()
				.stream()
				.filter(connectorTypeDto -> CsvConnectorType.NAME.equals(connectorTypeDto.getId()))
				.findFirst()
				.orElse(null);
		assertNotNull(csvConnectorTypeDto);

	}

	@Test
	public void testLoadDefaultValuesConnectorType() {
		ConnectorTypeDto mockCsvConnectorTypeDto = new ConnectorTypeDto();
		mockCsvConnectorTypeDto.setReopened(false);
		mockCsvConnectorTypeDto.setId(CsvConnectorType.NAME);
		ResponseEntity<ConnectorTypeDto> responseEntity = systemController.loadConnectorType(mockCsvConnectorTypeDto);
		ConnectorTypeDto csvConnectorTypeDto = responseEntity.getBody();
		assertNotNull(csvConnectorTypeDto);

		Map<String, String> metadata = csvConnectorTypeDto.getMetadata();
		Map<String, String> beanMetadata = csvConnectorType.getMetadata();
		assertEquals(beanMetadata.get(CsvConnectorType.FILE_PATH), metadata.get(CsvConnectorType.FILE_PATH));
		assertEquals(beanMetadata.get(CsvConnectorType.SEPARATOR), metadata.get(CsvConnectorType.SEPARATOR));
		assertEquals(beanMetadata.get(CsvConnectorType.SYSTEM_NAME), metadata.get(CsvConnectorType.SYSTEM_NAME));
	}

	@Test
	public void testMappingStep() {

		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(systemDto);
		Assert.assertEquals(DefaultConnectorType.NAME, connectorType.getConnectorName());

		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDto.setReopened(true);
		connectorTypeDto = connectorManager.load(connectorTypeDto);
		// One mapping already exists.
		BaseDto mapping = connectorTypeDto.getEmbedded().get(AbstractConnectorType.MAPPING_DTO_KEY);
		Assert.assertTrue(mapping instanceof SysSystemMappingDto);
		// Only one mapping exists now.
		String moreMappings = connectorTypeDto.getMetadata().get(AbstractConnectorType.ALERT_MORE_MAPPINGS);
		Assert.assertNull(moreMappings);

		// Execute mapping step.
		connectorTypeDto.setReopened(false);
		connectorTypeDto.setWizardStepName(AbstractConnectorType.STEP_MAPPING);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.SCHEMA_ID, ((SysSystemMappingDto) mapping).getObjectClass().toString());
		connectorTypeDto.getMetadata().put(AbstractConnectorType.OPERATION_TYPE, SystemOperationType.SYNCHRONIZATION.name());
		connectorTypeDto.getMetadata().put(AbstractConnectorType.ENTITY_TYPE, SystemEntityType.IDENTITY.name());
		systemController.executeConnectorType(connectorTypeDto);

		ConnectorTypeDto connectorTypeDtoAfterMappingStep = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDtoAfterMappingStep.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDtoAfterMappingStep.setReopened(true);
		connectorTypeDtoAfterMappingStep = connectorManager.load(connectorTypeDtoAfterMappingStep);
		// Two mappings have to exists now.
		moreMappings = connectorTypeDtoAfterMappingStep.getMetadata().get(AbstractConnectorType.ALERT_MORE_MAPPINGS);
		Assert.assertEquals(Boolean.TRUE.toString(), moreMappings);

	}

	@Test
	public void testFinishStep() {

		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(systemDto);
		Assert.assertEquals(DefaultConnectorType.NAME, connectorType.getConnectorName());

		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDto.setReopened(true);
		connectorTypeDto = connectorManager.load(connectorTypeDto);
		// One mapping already exists.
		BaseDto mapping = connectorTypeDto.getEmbedded().get(AbstractConnectorType.MAPPING_DTO_KEY);
		Assert.assertTrue(mapping instanceof SysSystemMappingDto);

		// Execute finish step.
		String roleName = getHelper().createName();
		connectorTypeDto.setReopened(false);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());
		connectorTypeDto.getMetadata().put(AbstractConnectorType.MAPPING_ID, mapping.getId().toString());
		connectorTypeDto.setWizardStepName(AbstractConnectorType.STEP_FINISH);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.NEW_ROLE_WITH_SYSTEM_CODE, roleName);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.CREATES_ROLE_WITH_SYSTEM, Boolean.TRUE.toString());
		systemController.executeConnectorType(connectorTypeDto);

		// A new role-system was to be created.
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setSystemId(systemDto.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null)
				.getContent();
		Assert.assertEquals(1, roleSystemDtos.size());
		IdmRoleDto roleDto = (IdmRoleDto) roleSystemDtos.get(0).getEmbedded().get(SysRoleSystem_.role.getName());
		Assert.assertEquals(roleName, roleDto.getBaseCode());
	}
}
