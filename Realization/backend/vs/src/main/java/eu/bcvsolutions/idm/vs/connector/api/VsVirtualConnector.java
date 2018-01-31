package eu.bcvsolutions.idm.vs.connector.api;

import java.util.List;

import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.api.operation.IcCanCreate;
import eu.bcvsolutions.idm.ic.api.operation.IcCanDelete;
import eu.bcvsolutions.idm.ic.api.operation.IcCanGenSchema;
import eu.bcvsolutions.idm.ic.api.operation.IcCanRead;
import eu.bcvsolutions.idm.ic.api.operation.IcCanSearch;
import eu.bcvsolutions.idm.ic.api.operation.IcCanUpdate;
import eu.bcvsolutions.idm.vs.connector.basic.BasicVirtualConfiguration;

/**
 * Basic interface for all virtual connectors
 * @author svandav
 *
 */
public interface VsVirtualConnector extends IcConnector, IcCanRead, IcCanCreate, IcCanUpdate, IcCanDelete, IcCanGenSchema, IcCanSearch{

	/**
	 * Creates account on system. Is called after VS request ending.
	 * @param objectClass
	 * @param attributes
	 * @return
	 */
	IcUidAttribute internalCreate(IcObjectClass objectClass, List<IcAttribute> attributes);

	/**
	 * Update account on system. Is called after VS request ending.
	 * @param uid
	 * @param objectClass
	 * @param attributes
	 * @return
	 */
	IcUidAttribute internalUpdate(IcUidAttribute uid, IcObjectClass objectClass, List<IcAttribute> attributes);

	/**
	 * Delete account on system. Is called after VS request ending.
	 * @param uid
	 * @param objectClass
	 */
	void internalDelete(IcUidAttribute uid, IcObjectClass objectClass);

	/**
	 * Get configuration
	 * @return
	 */
	BasicVirtualConfiguration getVirtualConfiguration();

}
