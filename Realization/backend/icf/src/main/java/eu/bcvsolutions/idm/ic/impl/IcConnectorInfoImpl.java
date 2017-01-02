package eu.bcvsolutions.idm.icf.impl;

import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;

/**
 * Information about connector. Keep connector key, display name and category
 * @author svandav
 *
 */
public class IcfConnectorInfoImpl implements IcfConnectorInfo {

	public IcfConnectorInfoImpl() {
		super();
	}
	
	public IcfConnectorInfoImpl(String connectorDisplayName, String connectorCategory, IcfConnectorKeyImpl connectorKey) {
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
	private IcfConnectorKeyImpl connectorKey;

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
	public IcfConnectorKeyImpl getConnectorKey() {
		return connectorKey;
	}

	public void setConnectorKey(IcfConnectorKeyImpl connectorKey) {
		this.connectorKey = connectorKey;
	}

	@Override
	public String toString() {
		return "IcfConnectorInfoImpl [connectorDisplayName=" + connectorDisplayName + ", connectorCategory="
				+ connectorCategory + ", connectorKey=" + connectorKey + "]";
	}

}
