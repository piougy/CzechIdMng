package eu.bcvsolutions.idm.ic.api;

/**
 * Connector witch implements this interface supports generate schema operation.
 * @author svandav
 *
 */
public interface IcConnectorSchema {

	/**
	 * Generate schema for this connector
	 * @return
	 */
	public IcSchema schema();
}
