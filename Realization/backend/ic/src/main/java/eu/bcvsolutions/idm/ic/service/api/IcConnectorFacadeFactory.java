package eu.bcvsolutions.idm.ic.service.api;

import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;

/**
 * Basic factory to provide {@link ConnectorFacade} instances.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
public interface IcConnectorFacadeFactory {

	/**
	 * Basic factory method to obtain {@link ConnectorFacade} instance based on given configuration. It uses
	 * {@link ConnectorFacadeFactory} internally.
	 *
	 * @param connectorInstance {@link IcConnectorInstance} to obtain connector info
	 * @param connectorConfiguration {@link IcConnectorConfiguration} to create api configuration
	 * @return New instance of {@link ConnectorFacade} created based on given arguments
	 */
	ConnectorFacade getConnectorFacade(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration);

}
