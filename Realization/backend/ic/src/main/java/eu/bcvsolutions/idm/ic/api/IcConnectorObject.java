package eu.bcvsolutions.idm.ic.api;

import java.util.List;
/**
 * Interface for connector object
 * @author svandav
 *
 */
public interface IcConnectorObject {
	
	/**
	 * Define type of object on resource
	 * @return
	 */
	IcObjectClass getObjectClass();

	/**
	 * Return attributes for this connector object
	 * @return
	 */
	List<IcAttribute> getAttributes();

}