package eu.bcvsolutions.idm.icf.dto;

import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;

public class IcfConnectorInfoDto implements IcfConnectorInfo {

	public IcfConnectorInfoDto() {
		super();
	}
	
	public IcfConnectorInfoDto(String connectorDisplayName, String connectorCategory, IcfConnectorKeyDto connectorKey) {
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
	private IcfConnectorKeyDto connectorKey;

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
	public IcfConnectorKeyDto getConnectorKey() {
		return connectorKey;
	}

	public void setConnectorKey(IcfConnectorKeyDto connectorKey) {
		this.connectorKey = connectorKey;
	}

	@Override
	public String toString() {
		return "IcfConnectorInfoDto [connectorDisplayName=" + connectorDisplayName + ", connectorCategory="
				+ connectorCategory + ", connectorKey=" + connectorKey + "]";
	}

}
