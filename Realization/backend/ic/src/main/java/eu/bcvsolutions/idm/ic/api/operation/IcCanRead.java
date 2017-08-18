package eu.bcvsolutions.idm.ic.api.operation;

import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Connector witch implements this interface supports read operation.
 * @author svandav
 *
 */
public interface IcCanRead {

	/**
	 * Read object by given UID from connector
	 * @param uid
	 * @param objectClass
	 * @return IcConnectorObject
	 */
	public IcConnectorObject read(IcUidAttribute uid, IcObjectClass objectClass);
}
