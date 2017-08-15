package eu.bcvsolutions.idm.ic.api;

/**
 * Connector witch implements this interface supports delete operation.
 * @author svandav
 *
 */
public interface IcConnectorDelete {

	/**
	 * Delete given object by connector
	 * @param uid
	 * @param objectClass
	 * @return
	 */
	public void delete(IcUidAttribute uid, IcObjectClass objectClass);
}
