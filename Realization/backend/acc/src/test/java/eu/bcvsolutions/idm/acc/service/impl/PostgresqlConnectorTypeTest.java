package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.connector.PostgresqlConnectorType;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for PostgreSQL connector type.
 * Tests are use only if environment use PostgreSQL database. Otherwise are these tests skipped.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Transactional
public class PostgresqlConnectorTypeTest extends AbstractJdbcConnectorTypeTest {

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
		return jdbcUrl.split("//")[1].split(":")[1].split("/")[0];
	}

	protected String getDatabase() {
		String jdbcUrl = env.getProperty("spring.datasource.url");
		return jdbcUrl.split("//")[1].split(":")[1].split("/")[1];
	}

	protected String getJdbcConnectorType() {
		return PostgresqlConnectorType.NAME;
	}

	protected String getJdbcConnectorTypeDriverName() {
		PostgresqlConnectorType type = new PostgresqlConnectorType();
		return type.getJdbcDriverName();
	}
}
