package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.connector.AbstractJdbcConnectorType;
import eu.bcvsolutions.idm.acc.connector.PostgresqlConnectorType;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * Abstract test for JDBC connector types.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
public abstract class AbstractJdbcConnectorTypeTest extends AbstractIntegrationTest {
	@Autowired
	protected Environment env;
	@Autowired
	private ConnectorManager connectorManager;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private FormService formService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	abstract protected String getHost();

	abstract protected String getUsername();

	abstract protected String getPassword();

	abstract protected String getPort();

	abstract protected String getDatabase();

	abstract protected String getJdbcConnectorType();

	abstract protected String getJdbcConnectorTypeDriverName();

	protected String getDriver() {
		return env.getProperty("spring.datasource.driver-class-name");
	}

	public void testJdbcFirstStep() {
		// Check if current running environment use driver same as this test.
		// If not, whole test will be skipped.
		if (!getJdbcConnectorTypeDriverName().equals(getDriver())) {
			// Skip test.
			return;
		}
		ConnectorTypeDto mockPostgresqlConnectorTypeDto = new ConnectorTypeDto();
		mockPostgresqlConnectorTypeDto.setReopened(false);
		mockPostgresqlConnectorTypeDto.setId(this.getJdbcConnectorType());

		ConnectorTypeDto jdbcConnectorTypeDto = connectorManager.load(mockPostgresqlConnectorTypeDto);
		assertNotNull(jdbcConnectorTypeDto);

		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.HOST, this.getHost());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.PORT, this.getPort());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.DATABASE, this.getDatabase());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.USER, this.getUsername());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.PASSWORD, this.getPassword());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.TABLE, "idm_identity");
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.KEY_COLUMN, "username");
		jdbcConnectorTypeDto.setWizardStepName(AbstractJdbcConnectorType.STEP_ONE_CREATE_SYSTEM);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(jdbcConnectorTypeDto);

		// The system had to be created.
		BaseDto system = stepExecutedResult.getEmbedded().get(AbstractJdbcConnectorType.SYSTEM_DTO_KEY);
		assertTrue(system instanceof SysSystemDto);
		SysSystemDto systemDto = systemService.get(system.getId());
		assertNotNull(systemDto);

		// Load connector properties from created system.
		IcConnectorInstance connectorInstance = systemService.getConnectorInstance(systemDto);
		assertEquals("net.tirasa.connid.bundles.db.table.DatabaseTableConnector",
				connectorInstance.getConnectorKey().getConnectorName());

		IdmFormDefinitionDto connectorFormDef = this.systemService.getConnectorFormDefinition(systemDto);
		// Check host.
		Assert.assertEquals(this.getHost(),
				getValueFromConnectorInstance(AbstractJdbcConnectorType.HOST, systemDto, connectorFormDef));
		// Check port.
		Assert.assertEquals(this.getPort(),
				getValueFromConnectorInstance(AbstractJdbcConnectorType.PORT, systemDto, connectorFormDef));
		// Check database.
		Assert.assertEquals(this.getDatabase(),
				getValueFromConnectorInstance(AbstractJdbcConnectorType.DATABASE, systemDto, connectorFormDef));
		// Check user.
		Assert.assertEquals(this.getUsername(),
				getValueFromConnectorInstance(AbstractJdbcConnectorType.USER, systemDto, connectorFormDef));
		// Check password.
		assertEquals("idm_identity",
				getValueFromConnectorInstance(AbstractJdbcConnectorType.TABLE, systemDto, connectorFormDef));

		// Delete created system.
		systemService.delete(systemDto);
	}

	public void testReopenSystemInWizard() {
		// Check if current running environment use driver same as this test.
		// If not, whole test will be skipped.
		if (!getJdbcConnectorTypeDriverName().equals(getDriver())) {
			// Skip test.
			return;
		}

		ConnectorTypeDto mockPostgresqlConnectorTypeDto = new ConnectorTypeDto();
		mockPostgresqlConnectorTypeDto.setReopened(false);
		mockPostgresqlConnectorTypeDto.setId(this.getJdbcConnectorType());

		ConnectorTypeDto jdbcConnectorTypeDto = connectorManager.load(mockPostgresqlConnectorTypeDto);
		assertNotNull(jdbcConnectorTypeDto);

		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.HOST, this.getHost());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.PORT, this.getPort());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.DATABASE, this.getDatabase());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.USER, this.getUsername());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.PASSWORD, this.getPassword());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.TABLE, "idm_identity");
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.KEY_COLUMN, "username");
		jdbcConnectorTypeDto.setWizardStepName(AbstractJdbcConnectorType.STEP_ONE_CREATE_SYSTEM);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(jdbcConnectorTypeDto);

		// The system had to be created.
		BaseDto system = stepExecutedResult.getEmbedded().get(AbstractJdbcConnectorType.SYSTEM_DTO_KEY);
		assertTrue(system instanceof SysSystemDto);
		SysSystemDto systemDto = systemService.get(system.getId());
		assertNotNull(systemDto);

		// Load wizard data for existed system;
		ConnectorType connectorTypeBySystem = connectorManager.findConnectorTypeBySystem(systemDto);
		ConnectorTypeDto reopenSystem = new ConnectorTypeDto();
		reopenSystem.setReopened(true);
		reopenSystem.setId(connectorTypeBySystem.getId());
		reopenSystem.getEmbedded().put(PostgresqlConnectorType.SYSTEM_DTO_KEY, systemDto);
		reopenSystem = connectorManager.load(reopenSystem);
		assertNotNull(reopenSystem);
		assertEquals(systemDto.getName(), reopenSystem.getMetadata().get(AbstractJdbcConnectorType.SYSTEM_NAME));
		Assert.assertEquals(this.getHost(), reopenSystem.getMetadata().get(AbstractJdbcConnectorType.HOST));
		Assert.assertEquals(this.getPort(), reopenSystem.getMetadata().get(AbstractJdbcConnectorType.PORT));
		Assert.assertEquals(this.getDatabase(), reopenSystem.getMetadata().get(AbstractJdbcConnectorType.DATABASE));
		Assert.assertEquals(this.getUsername(), reopenSystem.getMetadata().get(AbstractJdbcConnectorType.USER));
		// Password cannot be returned!
		Assert.assertNull(reopenSystem.getMetadata().get(AbstractJdbcConnectorType.PASSWORD));
		assertEquals("idm_identity", reopenSystem.getMetadata().get(AbstractJdbcConnectorType.TABLE));

		// Delete created system.
		systemService.delete(systemDto);
	}

	protected String getValueFromConnectorInstance(String attributeCode, SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		IdmFormAttributeDto attribute = connectorFormDef.getMappedAttributeByCode(attributeCode);
		List<IdmFormValueDto> values = formService.getValues(systemDto, attribute, IdmBasePermission.READ);
		if (values != null && values.size() == 1) {
			return values.get(0).getStringValue();
		}
		return null;
	}
}
