package eu.bcvsolutions.idm.ic.api.operation;

import java.util.List;

import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Connector witch implements this interface supports update operation.
 * @author svandav
 *
 */
public interface IcCanUpdate {

	/**
	 * Update given object by connector
	 * @param uid
	 * @param objectClass
	 * @param attributes
	 * @return
	 */
	public IcUidAttribute update(IcUidAttribute uid, IcObjectClass objectClass,  List<IcAttribute> attributes);
}
