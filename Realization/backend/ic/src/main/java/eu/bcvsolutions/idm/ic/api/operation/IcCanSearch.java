package eu.bcvsolutions.idm.ic.api.operation;

import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcResultsHandler;

/**
 * Connector witch implements this interface supports search operation.
 * @author svandav
 *
 */
public interface IcCanSearch {

	/**
	 * Search for given filter. For every result call given handler.
	 * @param objectClass
	 * @param filter
	 * @param handler
	 */
	public void search(IcObjectClass objectClass, IcFilter filter, IcResultsHandler handler);
}
