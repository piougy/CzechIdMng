package eu.bcvsolutions.idm.icf.service.api;

import java.util.List;

import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfConnectorObject;
import eu.bcvsolutions.idm.icf.api.IcfObjectClass;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.security.domain.GuardedString;

public interface IcfConnectorFacade {

	/**
	 * Create new object in resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param attributes - Attributes for new object
	 * @return
	 */
	IcfUidAttribute createObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, List<IcfAttribute> attributes);
	
	/**
	 * Replace attributes in exist object in resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 * @param replaceAttributes - Attributes to replace in resource object
	 * @return
	 */
	IcfUidAttribute updateObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid, List<IcfAttribute> replaceAttributes);
	
	/**
	 * Delete object with same uid from resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 */
	public void deleteObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid);
	
	/**
	 * Read object with same uid from resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 * @return
	 */
	public IcfConnectorObject readObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid);
	
	/**
	 * Authenticate user
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param username
	 * @param password
	 * @return
	 */
	public IcfUidAttribute authenticateObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, String username, GuardedString password);

}