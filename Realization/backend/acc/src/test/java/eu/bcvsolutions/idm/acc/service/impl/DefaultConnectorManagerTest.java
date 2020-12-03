package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.connector.CsvConnectorType;
import eu.bcvsolutions.idm.acc.connector.DefaultConnectorType;
import eu.bcvsolutions.idm.acc.connector.PostgresqlConnectorType;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.rest.impl.SysSystemController;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.Map;
import org.junit.After;
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
}
