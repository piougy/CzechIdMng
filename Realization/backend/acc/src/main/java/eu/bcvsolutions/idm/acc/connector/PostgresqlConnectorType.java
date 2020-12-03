package eu.bcvsolutions.idm.acc.connector;

import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL connector type extends standard table connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Component(PostgresqlConnectorType.NAME)
public class PostgresqlConnectorType extends AbstractJdbcConnectorType {

	public static final String NAME = "postgresql-connector-type";

	@Override
	public String getIconKey() {
		return "postgresql-connector";
	}

	@Override
	public String getJdbcDriverName() {
		return "org.postgresql.Driver";
	}

	@Override
	public String getJdbcUrlTemplate(){
		return "jdbc:postgresql://%h:%p/%d";
	}

	@Override
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("PostgresSQL system", 1));
		metadata.put(HOST, "localhost");
		metadata.put(PORT, "5432");
		return metadata;
	}


	@Override
	public int getOrder() {
		return 150;
	}
}
