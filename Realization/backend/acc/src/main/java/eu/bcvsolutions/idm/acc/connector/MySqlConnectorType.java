package eu.bcvsolutions.idm.acc.connector;

import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * MYSQL connector type extends standard table connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Component(MySqlConnectorType.NAME)
public class MySqlConnectorType extends AbstractJdbcConnectorType {

	public static final String NAME = "mysql-connector-type";

	@Override
	public String getIconKey() {
		return "mysql-connector";
	}

	@Override
	public String getJdbcDriverName() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	public String getJdbcUrlTemplate(){
		return "jdbc:mysql://%h:%p/%d";
	}

	@Override
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("MySQL system", 1));
		metadata.put(HOST, "localhost");
		metadata.put(PORT, "3306");
		return metadata;
	}

	@Override
	public int getOrder() {
		return 160;
	}

}
