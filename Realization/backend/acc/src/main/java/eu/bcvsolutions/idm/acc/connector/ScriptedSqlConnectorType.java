package eu.bcvsolutions.idm.acc.connector;

import org.springframework.stereotype.Component;

/**
 * Scripted SQL connector type extends standard table connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Component(ScriptedSqlConnectorType.NAME)
public class ScriptedSqlConnectorType extends DefaultConnectorType {

	public static final String NAME = "scripted-sql-connector-type";

	@Override
	public String getConnectorName() {
		return "net.tirasa.connid.bundles.db.scriptedsql.ScriptedSQLConnector";
	}

	@Override
	public String getIconKey() {
		return "scripted-sql-connector-icon";
	}

	@Override
	public int getOrder() {
		return 260;
	}

}
