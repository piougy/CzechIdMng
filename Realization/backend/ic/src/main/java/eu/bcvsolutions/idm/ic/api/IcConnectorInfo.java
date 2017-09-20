package eu.bcvsolutions.idm.ic.api;

/**
 * Information about connector. Keep connector key, display name and category
 * @author svandav
 *
 */
public interface IcConnectorInfo {
	/**
	 * Friendly name suitable for display in the UI.
	 */
	String getConnectorDisplayName();

	/**
	 * Get the category this connector belongs to.
	 */
	String getConnectorCategory();

	IcConnectorKey getConnectorKey();


}