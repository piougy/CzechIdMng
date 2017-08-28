package eu.bcvsolutions.idm.ic.impl;

import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;

/**
 * Information about connector. Keep connector key, display name and category
 * @author svandav
 *
 */
public class IcConnectorInfoImpl implements IcConnectorInfo {

	public IcConnectorInfoImpl() {
		super();
	}
	
	public IcConnectorInfoImpl(String connectorDisplayName, String connectorCategory, IcConnectorKey connectorKey) {
		super();
		this.connectorDisplayName = connectorDisplayName;
		this.connectorCategory = connectorCategory;
		this.connectorKey = connectorKey;
	}

	/**
	 * Friendly name suitable for display in the UI.
	 */
	private String connectorDisplayName;

	/**
	 * Get the category this connector belongs to.
	 */
	private String connectorCategory;
	private IcConnectorKey connectorKey;

	@Override
	public String getConnectorDisplayName() {
		return connectorDisplayName;
	}

	public void setConnectorDisplayName(String connectorDisplayName) {
		this.connectorDisplayName = connectorDisplayName;
	}

	@Override
	public String getConnectorCategory() {
		return connectorCategory;
	}

	public void setConnectorCategory(String connectorCategory) {
		this.connectorCategory = connectorCategory;
	}

	@Override
	public IcConnectorKey getConnectorKey() {
		return connectorKey;
	}

	public void setConnectorKey(IcConnectorKeyImpl connectorKey) {
		this.connectorKey = connectorKey;
	}

	@Override
	public String toString() {
		return "IcConnectorInfoImpl [connectorDisplayName=" + connectorDisplayName + ", connectorCategory="
				+ connectorCategory + ", connectorKey=" + connectorKey + "]";
	}

}
