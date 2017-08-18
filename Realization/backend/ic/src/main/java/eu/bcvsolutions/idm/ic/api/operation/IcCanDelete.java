package eu.bcvsolutions.idm.ic.api.operation;

import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Connector witch implements this interface supports delete operation.
 * @author svandav
 *
 */
public interface IcCanDelete {

	/**
	 * Delete given object by connector
	 * @param uid
	 * @param objectClass
	 * @return
	 */
	public void delete(IcUidAttribute uid, IcObjectClass objectClass);
}
