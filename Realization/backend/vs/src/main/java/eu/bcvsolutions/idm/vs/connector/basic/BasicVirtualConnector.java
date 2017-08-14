package eu.bcvsolutions.idm.vs.connector.basic;

import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.annotation.IcConnectorClass;

@IcConnectorClass(displayName = "Basic virtual system for CzechIdM", framework = "czechidm", name = "virtual-basic", version = "0.2.0", configurationClass = BasicVirtualConfiguration.class)
public class BasicVirtualConnector implements IcConnector {

	@Override
	public void init(IcConnectorConfiguration configuration) {
		// TODO Auto-generated method stub

	}
}
