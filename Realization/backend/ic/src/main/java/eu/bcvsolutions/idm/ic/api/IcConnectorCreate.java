package eu.bcvsolutions.idm.ic.api;

import java.util.List;

/**
 * Connector witch implements this interface supports create operation.
 * @author svandav
 *
 */
public interface IcConnectorCreate {

	/**
	 * Create object by connector.
	 * @param objectClass
	 * @param attributes
	 * @return
	 */
	public IcUidAttribute create(IcObjectClass objectClass,  List<IcAttribute> attributes);
}
