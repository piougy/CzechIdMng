package eu.bcvsolutions.idm.ic.api.operation;

import java.util.List;

import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Connector witch implements this interface supports create operation.
 * @author svandav
 *
 */
public interface IcCanCreate {

	/**
	 * Create object by connector.
	 * @param objectClass
	 * @param attributes
	 * @return
	 */
	public IcUidAttribute create(IcObjectClass objectClass,  List<IcAttribute> attributes);
}
