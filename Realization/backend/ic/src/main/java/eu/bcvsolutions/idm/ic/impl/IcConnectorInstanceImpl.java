/**
 * 
 */
package eu.bcvsolutions.idm.ic.impl;

import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;

/**
 * Connector instance impl
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IcConnectorInstanceImpl implements IcConnectorInstance {
	
	private IcConnectorServer connectorServer;
	
	private IcConnectorKey connectorKey;
	
	private boolean remote = false;
	
	public IcConnectorInstanceImpl(IcConnectorServer connectorServer, IcConnectorKey connectorKey, boolean remote) {
		this.connectorKey = connectorKey;
		this.connectorServer = connectorServer;
		this.remote = remote;
		
	}
	
	public IcConnectorInstanceImpl() {
	}	
	
	public void setConnectorServer(IcConnectorServer connectorServer) {
		this.connectorServer = connectorServer;
	}

	public void setConnectorKey(IcConnectorKey connectorKey) {
		this.connectorKey = connectorKey;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.ic.api.IcConnectorInstance#getConnectorKey()
	 */
	@Override
	public IcConnectorKey getConnectorKey() {
		return connectorKey;
	}

	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.ic.api.IcConnectorInstance#getConnectorServer()
	 */
	@Override
	public IcConnectorServer getConnectorServer() {
		return connectorServer;
	}

	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.ic.api.IcConnectorInstance#getRemote()
	 */
	@Override
	public boolean getRemote() {
		return remote;
	}

}
