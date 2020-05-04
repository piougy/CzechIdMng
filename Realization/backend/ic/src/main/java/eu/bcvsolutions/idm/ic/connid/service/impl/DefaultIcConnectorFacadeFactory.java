package eu.bcvsolutions.idm.ic.connid.service.impl;

import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.connid.domain.ConnIdIcConvertUtil;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacadeFactory;

/**
 * Basic factory to provide {@link ConnectorFacade} instances.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Service
public class DefaultIcConnectorFacadeFactory implements IcConnectorFacadeFactory {

	@Autowired
	private ConnIdIcConfigurationService configurationServiceConnId;

	/**
	 * Basic factory method to obtain {@link ConnectorFacade} instance based on given configuration. It uses
	 * {@link ConnectorFacadeFactory} internally.
	 *
	 * @param connectorInstance {@link IcConnectorInstance} to obtain connector info
	 * @param connectorConfiguration {@link IcConnectorConfiguration} to create api configuration
	 * @return New instance of {@link ConnectorFacade} created based on given arguments
	 */
	@Override
	public ConnectorFacade getConnectorFacade(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		ConnectorInfo connIdInfo = configurationServiceConnId.getConnIdConnectorInfo(connectorInstance);
		Assert.notNull(connIdInfo, "ConnId connector info not found!");
		APIConfiguration config = connIdInfo.createDefaultAPIConfiguration();
		Assert.notNull(config.getConfigurationProperties(), "ConnId connector configuration properties not found!");
		config = ConnIdIcConvertUtil.convertIcConnectorConfiguration(connectorConfiguration, config);
		// Use the ConnectorFacadeFactory's newInstance() method to get a new
		// connector.
		ConnectorFacade conn = ConnectorFacadeFactory.getManagedInstance().newInstance(config);
		// Make sure we have set up the Configuration properly
		conn.validate();
		return conn;
	}

}
