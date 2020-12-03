package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.connector.MsSqlConnectorType;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for MS SQL connector type.
 * Tests are use only if environment use MS SQL database. Otherwise are these tests skipped.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Transactional
public class MssqlConnectorTypeTest extends AbstractJdbcConnectorTypeTest {

	@Test
	@Override
	public void testJdbcFirstStep() {
		super.testJdbcFirstStep();
	}

	@Test
	@Override
	public void testReopenSystemInWizard() {
		super.testReopenSystemInWizard();
	}

	protected String getHost() {
		//jdbc:sqlserver://localhost:1433;databaseName=bcv_idm_10
		String jdbcUrl = env.getProperty("spring.datasource.url");
		return jdbcUrl.split("//")[1].split(":")[0];
	}

	protected String getUsername() {
		return env.getProperty("spring.datasource.username");
	}

	protected String getPassword() {
		return env.getProperty("spring.datasource.password");
	}

	protected String getPort() {
		String jdbcUrl = env.getProperty("spring.datasource.url");
		return jdbcUrl.split("//")[1].split(":")[1].split(";")[0];
	}

	protected String getDatabase() {
		String jdbcUrl = env.getProperty("spring.datasource.url");
		return jdbcUrl.split("databaseName=")[1];
	}

	protected String getJdbcConnectorType() {
		return MsSqlConnectorType.NAME;
	}

	protected String getJdbcConnectorTypeDriverName() {
		MsSqlConnectorType type = new MsSqlConnectorType();
		return type.getJdbcDriverName();
	}
}
