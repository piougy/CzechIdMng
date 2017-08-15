package eu.bcvsolutions.idm.ic.api;

import java.util.List;

/**
 * Connector witch implements this interface supports update operation.
 * @author svandav
 *
 */
public interface IcConnectorUpdate {

	/**
	 * Update given object by connector
	 * @param uid
	 * @param objectClass
	 * @param attributes
	 * @return
	 */
	public IcUidAttribute update(IcUidAttribute uid, IcObjectClass objectClass,  List<IcAttribute> attributes);
}
