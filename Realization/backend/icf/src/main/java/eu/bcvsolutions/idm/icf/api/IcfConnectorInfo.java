package eu.bcvsolutions.idm.icf.api;

/**
 * Information about connector. Keep connector key, display name and category
 * @author svandav
 *
 */
public interface IcfConnectorInfo {
	/**
	 * Friendly name suitable for display in the UI.
	 */
	String getConnectorDisplayName();

	/**
	 * Get the category this connector belongs to.
	 */
	String getConnectorCategory();

	IcfConnectorKey getConnectorKey();

}