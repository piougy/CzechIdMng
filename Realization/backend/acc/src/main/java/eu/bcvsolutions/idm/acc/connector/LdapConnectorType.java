package eu.bcvsolutions.idm.acc.connector;

import org.springframework.stereotype.Component;

/**
 * LDAP connector type extends standard table connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Component(LdapConnectorType.NAME)
public class LdapConnectorType extends DefaultConnectorType {

	public static final String NAME = "ldap-connector-type";

	@Override
	public String getConnectorName() {
		return "net.tirasa.connid.bundles.ldap.LdapConnector";
	}

	@Override
	public String getIconKey() {
		return "ldap-connector-icon";
	}

	@Override
	public int getOrder() {
		return 250;
	}

}
