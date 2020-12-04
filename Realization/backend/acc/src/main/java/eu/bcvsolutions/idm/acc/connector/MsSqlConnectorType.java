package eu.bcvsolutions.idm.acc.connector;

import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * MS SQL connector type extends standard table connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Component(MsSqlConnectorType.NAME)
public class MsSqlConnectorType extends AbstractJdbcConnectorType {

	public static final String NAME = "mssql-connector-type";

	@Override
	public String getIconKey() {
		return "mssql-connector";
	}

	@Override
	public String getJdbcDriverName() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}

	@Override
	public String getJdbcUrlTemplate(){
		return "jdbc:sqlserver://%h:%p;databaseName=%d";
	}

	@Override
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("SQL server system", 1));
		metadata.put(HOST, "localhost");
		metadata.put(PORT, "1433");
		return metadata;
	}

	@Override
	public int getOrder() {
		return 170;
	}

}
