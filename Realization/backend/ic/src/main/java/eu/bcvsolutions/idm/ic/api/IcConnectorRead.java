package eu.bcvsolutions.idm.ic.api;

/**
 * Connector witch implements this interface supports read operation.
 * @author svandav
 *
 */
public interface IcConnectorRead {

	/**
	 * Read object by given UID from connector
	 * @param uid
	 * @param objectClass
	 * @return IcConnectorObject
	 */
	public IcConnectorObject read(IcUidAttribute uid, IcObjectClass objectClass);
}
