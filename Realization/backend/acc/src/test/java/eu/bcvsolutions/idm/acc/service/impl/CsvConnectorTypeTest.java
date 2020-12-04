package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.connector.CsvConnectorType;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.rest.impl.CsvConnectorTypeController;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperties;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Tests for CSV connector type.
 *
 * @author Vít Švanda
 */
@Transactional
public class CsvConnectorTypeTest extends AbstractIntegrationTest {

	private final String CSV_TEST_FILE = "src/test/resources/csv/idm_test.csv";

	@Autowired
	private ConnectorManager connectorManager;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private CsvConnectorTypeController csvConnectorTypeController;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testDeployCsv() throws IOException {
		String csvName = "idm_test.csv";
		ConnectorTypeDto mockCsvConnectorTypeDto = new ConnectorTypeDto();
		mockCsvConnectorTypeDto.setReopened(false);
		mockCsvConnectorTypeDto.setId(CsvConnectorType.NAME);

		ConnectorTypeDto csvConnectorTypeDto = connectorManager.load(mockCsvConnectorTypeDto);
		assertNotNull(csvConnectorTypeDto);
		String defaultPath = csvConnectorTypeDto.getMetadata().get(CsvConnectorType.FILE_PATH);
		assertNotNull(defaultPath);
		// Convert test file to the bytes and create mock MultipartFile.
		byte[] bytes = Files.readAllBytes(Paths.get(CSV_TEST_FILE));
		MultipartFile multipartFile = new MockMultipartFile(csvName, bytes);

		// Deploy file. CSV file should be copied to the default path.
		ResponseEntity<ConnectorTypeDto> deployResponse = csvConnectorTypeController.deploy(csvName, defaultPath, multipartFile);
		ConnectorTypeDto deployResult = deployResponse.getBody();
		assertNotNull(deployResult);
		assertEquals(Paths.get(defaultPath, csvName).toString(),
				Paths.get(deployResult.getMetadata().get(CsvConnectorType.FILE_PATH)).toString());
		// Check if deployed file exists.
		assertTrue(Files.exists(Paths.get(defaultPath, csvName)));
	}

	@Test
	public void testCsvFirstStep() {
		ConnectorTypeDto mockCsvConnectorTypeDto = new ConnectorTypeDto();
		mockCsvConnectorTypeDto.setReopened(false);
		mockCsvConnectorTypeDto.setId(CsvConnectorType.NAME);

		ConnectorTypeDto csvConnectorTypeDto = connectorManager.load(mockCsvConnectorTypeDto);
		assertNotNull(csvConnectorTypeDto);

		csvConnectorTypeDto.getMetadata().put(CsvConnectorType.FILE_PATH, CSV_TEST_FILE);
		csvConnectorTypeDto.setWizardStepName(CsvConnectorType.STEP_ONE_CREATE_SYSTEM);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(csvConnectorTypeDto);

		// The system had to be created.
		BaseDto system = stepExecutedResult.getEmbedded().get(CsvConnectorType.SYSTEM_DTO_KEY);
		assertTrue(system instanceof SysSystemDto);
		SysSystemDto systemDto = systemService.get(system.getId());
		assertNotNull(systemDto);

		// Load connector properties from created system.
		IcConnectorInstance connectorInstance = systemService.getConnectorInstance(systemDto);
		assertEquals("eu.bcvsolutions.idm.connectors.csv.CSVConnConnector",
				connectorInstance.getConnectorKey().getConnectorName());
		IcConnectorConfiguration connectorConfiguration = systemService.getConnectorConfiguration(systemDto);
		IcConfigurationProperties configurationProperties = connectorConfiguration.getConfigurationProperties();

		// Check source path of CSV.
		IcConfigurationProperty sourcePathProperty = configurationProperties.getProperties()
				.stream()
				.filter(property -> CsvConnectorType.CONNECTOR_SOURCE_PATH.equals(property.getName()))
				.findFirst()
				.orElse(null);
		assertNotNull(sourcePathProperty);
		assertEquals(CSV_TEST_FILE.replace('/','\\'),
				((String) sourcePathProperty.getValue()).replace('/','\\'));

		// Check separator.
		IcConfigurationProperty separatorProperty = configurationProperties.getProperties()
				.stream()
				.filter(property -> CsvConnectorType.CONNECTOR_SOURCE_PATH.equals(property.getName()))
				.findFirst()
				.orElse(null);
		assertNotNull(separatorProperty);

		// Check include headers.
		IcConfigurationProperty includeHeadersProperty = configurationProperties.getProperties()
				.stream()
				.filter(property -> CsvConnectorType.CONNECTOR_INCLUDES_HEADERS.equals(property.getName()))
				.findFirst()
				.orElse(null);
		assertNotNull(includeHeadersProperty);
		// Headers have to be always included in CSV file.
		assertEquals(true, includeHeadersProperty.getValue());

		// Check uid attribute (in first step has value "...random...").
		IcConfigurationProperty uidProperty = configurationProperties.getProperties()
				.stream()
				.filter(property -> CsvConnectorType.CONNECTOR_UID.equals(property.getName()))
				.findFirst()
				.orElse(null);
		assertNotNull(uidProperty);
		// UID attribute have to be filled, but will be selected in the next wizard step -> is use temporary value "...random...".
		assertEquals("...random...", uidProperty.getValue());

		// Delete created system.
		systemService.delete(systemDto);
	}

	@Test
	public void testCsvSecondStep() {
		ConnectorTypeDto mockCsvConnectorTypeDto = new ConnectorTypeDto();
		mockCsvConnectorTypeDto.setReopened(false);
		mockCsvConnectorTypeDto.setId(CsvConnectorType.NAME);

		ConnectorTypeDto csvConnectorTypeDto = connectorManager.load(mockCsvConnectorTypeDto);
		assertNotNull(csvConnectorTypeDto);

		csvConnectorTypeDto.getMetadata().put(CsvConnectorType.FILE_PATH, CSV_TEST_FILE);
		csvConnectorTypeDto.setWizardStepName(CsvConnectorType.STEP_ONE_CREATE_SYSTEM);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(csvConnectorTypeDto);

		// The system had to be created.
		BaseDto system = stepExecutedResult.getEmbedded().get(CsvConnectorType.SYSTEM_DTO_KEY);
		assertTrue(system instanceof SysSystemDto);
		SysSystemDto systemDto = systemService.get(system.getId());
		assertNotNull(systemDto);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(systemDto.getId());
		schemaAttributeFilter.setName("username");

		SysSchemaAttributeDto usernameAttributeDto = schemaAttributeService.find(schemaAttributeFilter, null)
				.getContent()
				.stream()
				.findFirst()
				.orElse(null);
		assertNotNull(usernameAttributeDto);

		// Set username attribute as primary attribute for second wizard step.
		stepExecutedResult.getMetadata().put(CsvConnectorType.PRIMARY_SCHEMA_ATTRIBUTE, usernameAttributeDto.getId().toString());
		// Execute the second step.
		csvConnectorTypeDto.setWizardStepName(CsvConnectorType.STEP_TWO_SELECT_PK);
		csvConnectorTypeDto.getMetadata().put(CsvConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());
		connectorManager.execute(csvConnectorTypeDto);

		// Load connector properties from created system.
		IcConnectorConfiguration connectorConfiguration = systemService.getConnectorConfiguration(systemDto);
		IcConfigurationProperties configurationProperties = connectorConfiguration.getConfigurationProperties();

		// Check uid attribute.
		IcConfigurationProperty uidProperty = configurationProperties.getProperties()
				.stream()
				.filter(property -> CsvConnectorType.CONNECTOR_UID.equals(property.getName()))
				.findFirst()
				.orElse(null);
		assertNotNull(uidProperty);
		// UID attribute have to be "username" now.
		assertEquals(usernameAttributeDto.getName(), uidProperty.getValue());

		// Delete created system.
		systemService.delete(systemDto);
	}

	@Test
	public void testReopenSystemInWizard() {
		ConnectorTypeDto mockCsvConnectorTypeDto = new ConnectorTypeDto();
		mockCsvConnectorTypeDto.setReopened(false);
		mockCsvConnectorTypeDto.setId(CsvConnectorType.NAME);

		ConnectorTypeDto csvConnectorTypeDto = connectorManager.load(mockCsvConnectorTypeDto);
		assertNotNull(csvConnectorTypeDto);

		csvConnectorTypeDto.getMetadata().put(CsvConnectorType.FILE_PATH, CSV_TEST_FILE);
		csvConnectorTypeDto.setWizardStepName(CsvConnectorType.STEP_ONE_CREATE_SYSTEM);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(csvConnectorTypeDto);

		// The system had to be created.
		BaseDto system = stepExecutedResult.getEmbedded().get(CsvConnectorType.SYSTEM_DTO_KEY);
		assertTrue(system instanceof SysSystemDto);
		SysSystemDto systemDto = systemService.get(system.getId());
		assertNotNull(systemDto);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(systemDto.getId());
		schemaAttributeFilter.setName("username");

		SysSchemaAttributeDto usernameAttributeDto = schemaAttributeService.find(schemaAttributeFilter, null)
				.getContent()
				.stream()
				.findFirst()
				.orElse(null);
		assertNotNull(usernameAttributeDto);

		// Set username attribute as primary attribute for second wizard step.
		stepExecutedResult.getMetadata().put(CsvConnectorType.PRIMARY_SCHEMA_ATTRIBUTE, usernameAttributeDto.getId().toString());
		// Execute the second step.
		csvConnectorTypeDto.setWizardStepName(CsvConnectorType.STEP_TWO_SELECT_PK);
		csvConnectorTypeDto.getMetadata().put(CsvConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());
		connectorManager.execute(csvConnectorTypeDto);

		// Load wizard data for existed system;
		ConnectorType connectorTypeBySystem = connectorManager.findConnectorTypeBySystem(systemDto);
		ConnectorTypeDto reopenSystem = new ConnectorTypeDto();
		reopenSystem.setReopened(true);
		reopenSystem.setId(connectorTypeBySystem.getId());
		reopenSystem.getEmbedded().put(CsvConnectorType.SYSTEM_DTO_KEY, systemDto);
		reopenSystem = connectorManager.load(reopenSystem);
		assertNotNull(reopenSystem);
		assertEquals(systemDto.getName(), reopenSystem.getMetadata().get(CsvConnectorType.SYSTEM_NAME));
		assertEquals(";", reopenSystem.getMetadata().get(CsvConnectorType.SEPARATOR));
		assertEquals(usernameAttributeDto.getId().toString(), reopenSystem.getMetadata().get(CsvConnectorType.PRIMARY_SCHEMA_ATTRIBUTE));

		// Delete created system.
		systemService.delete(systemDto);
	}

}
