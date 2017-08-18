package eu.bcvsolutions.idm.ic.api.operation;

import eu.bcvsolutions.idm.ic.api.IcSchema;

/**
 * Connector witch implements this interface supports generate schema operation.
 * @author svandav
 *
 */
public interface IcCanGenSchema {

	/**
	 * Generate schema for this connector
	 * @return
	 */
	public IcSchema schema();
}
