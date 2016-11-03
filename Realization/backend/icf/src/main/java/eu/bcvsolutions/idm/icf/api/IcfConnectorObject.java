package eu.bcvsolutions.idm.icf.api;

import java.util.List;
/**
 * Interface for connector object
 * @author svandav
 *
 */
public interface IcfConnectorObject {

	IcfObjectClass getObjectClass();

	List<IcfAttribute> getAttributes();

}