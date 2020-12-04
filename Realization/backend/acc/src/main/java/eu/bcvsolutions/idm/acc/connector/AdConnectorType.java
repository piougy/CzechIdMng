package eu.bcvsolutions.idm.acc.connector;

import org.springframework.stereotype.Component;

/**
 * AD connector type extends standard table connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Component(AdConnectorType.NAME)
public class AdConnectorType extends DefaultConnectorType {

	public static final String NAME = "ad-connector-type";

	@Override
	public String getConnectorName() {
		return "net.tirasa.connid.bundles.ad.ADConnector";
	}

	@Override
	public String getIconKey() {
		return "ad-connector-icon";
	}

	@Override
	public int getOrder() {
		return 200;
	}

}
