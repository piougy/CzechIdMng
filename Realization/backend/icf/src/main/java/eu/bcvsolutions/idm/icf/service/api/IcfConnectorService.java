package eu.bcvsolutions.idm.icf.service.api;

import java.util.List;

import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;

public interface IcfConnectorService {

	/**
	 * Return key defined ICF implementation
	 * 
	 * @return
	 */
	String getIcfType();

	IcfUidAttribute createObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			List<IcfAttribute> attributes);

}