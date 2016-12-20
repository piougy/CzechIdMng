package eu.bcvsolutions.idm.icf.api;

import java.util.List;
/**
 * Interface for connector object
 * @author svandav
 *
 */
public interface IcfConnectorObject {
	
	/**
	 * Define type of object on resource
	 * @return
	 */
	IcfObjectClass getObjectClass();

	/**
	 * Return attributes for this connector object
	 * @return
	 */
	List<IcfAttribute> getAttributes();

}