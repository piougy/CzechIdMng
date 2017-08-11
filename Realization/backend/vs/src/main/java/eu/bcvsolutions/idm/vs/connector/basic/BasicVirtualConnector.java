package eu.bcvsolutions.idm.vs.connector.basic;

import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorClass;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorConfigurationImpl;

@IcConnectorClass(displayName = "Basic virtual system for CzechIdM", framework = "czechidm", name = "virtual-basic", version = "0.2.0")
public class BasicVirtualConnector implements IcConnector {

	private IcConnectorConfiguration defaultConfiguration = null;

	@Override
	public void init(IcConnectorConfiguration configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public IcConnectorConfiguration getDefaultConfiguration() {
		if (defaultConfiguration == null) {
			IcConnectorConfigurationImpl config = new IcConnectorConfigurationImpl();
			config.setConnectorPoolingSupported(false);
			IcConfigurationPropertiesImpl configurationProperties = new IcConfigurationPropertiesImpl();
			configurationProperties
					.addProperty("properties", "name, firstName, lastName", String.class.getName(), "Property names",
							"Properties for create EAV model. Values must be split by comma.", true)//
					.addProperty("approvers", null, String.class.getName(), "Approvers",
							"For this approvers will be created realization tasks. Every approver must be identity in CzechIdM. Value are UUIDs of identities split by comma.",
							false);

			config.setConfigurationProperties(configurationProperties);

			this.defaultConfiguration = config;

		}
		return defaultConfiguration;
	}

}
