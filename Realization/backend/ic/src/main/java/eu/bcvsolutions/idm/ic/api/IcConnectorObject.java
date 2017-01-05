package eu.bcvsolutions.idm.ic.api;

import java.io.Serializable;
import java.util.List;
/**
 * Interface for connector object
 * @author svandav
 *
 */
public interface IcConnectorObject extends Serializable {
	
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