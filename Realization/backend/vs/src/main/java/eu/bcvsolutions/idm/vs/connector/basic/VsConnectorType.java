package eu.bcvsolutions.idm.vs.connector.basic;

import eu.bcvsolutions.idm.acc.connector.DefaultConnectorType;
import org.springframework.stereotype.Component;

/**
 * Virtual system connector type extends standard VS connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Component(VsConnectorType.NAME)
public class VsConnectorType extends DefaultConnectorType {

	public static final String NAME = "vs-connector-type";

	@Override
	public String getConnectorName() {
		return "virtual-system-basic";
	}

	@Override
	public String getIconKey() {
		return "virtual-reality";
	}

	@Override
	public int getOrder() {
		return 300;
	}

}
