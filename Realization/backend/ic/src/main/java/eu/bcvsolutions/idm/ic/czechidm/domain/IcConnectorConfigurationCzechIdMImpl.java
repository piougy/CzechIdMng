package eu.bcvsolutions.idm.ic.czechidm.domain;

import java.util.UUID;

import eu.bcvsolutions.idm.ic.impl.IcConnectorConfigurationImpl;

/**
 * Extends standard connector configuration for additional information (for CzechIdM framework).
 * For example we need CzechIdM system ID for virtual system.
 * @author svandav
 *
 */
public class IcConnectorConfigurationCzechIdMImpl extends IcConnectorConfigurationImpl {

	/**
	 * Id of CzechIdM system. From this system is connector call
	 */
	private UUID systemId;

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}
	
}
