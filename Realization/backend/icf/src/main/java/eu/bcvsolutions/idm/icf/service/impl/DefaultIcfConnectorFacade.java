package eu.bcvsolutions.idm.icf.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfConnectorObject;
import eu.bcvsolutions.idm.icf.api.IcfObjectClass;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.icf.domain.IcfResultCode;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorFacade;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

@Service
/**
 * Service for do active operations on all ICF implementations
 * 
 * @author svandav
 *
 */
public class DefaultIcfConnectorFacade implements IcfConnectorFacade {

	private Map<String, IcfConnectorService> icfConnectors = new HashMap<>();

	/**
	 * @return Connector services for all ICFs
	 */
	@Override
	public Map<String, IcfConnectorService> getIcfConnectors() {
		return icfConnectors;
	}

	@Override
	public IcfUidAttribute createObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, List<IcfAttribute> attributes) {
		Assert.notNull(key);
		checkIcfType(key);
		return icfConnectors.get(key.getFramework()).createObject(key, connectorConfiguration, objectClass, attributes);

	}

	@Override
	public IcfUidAttribute updateObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid, List<IcfAttribute> replaceAttributes) {
		Assert.notNull(key);
		checkIcfType(key);
		return icfConnectors.get(key.getFramework()).updateObject(key, connectorConfiguration, objectClass, uid,
				replaceAttributes);

	}

	@Override
	public void deleteObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid) {
		Assert.notNull(key);
		checkIcfType(key);
		icfConnectors.get(key.getFramework()).deleteObject(key, connectorConfiguration, objectClass, uid);

	}
	
	@Override
	public IcfConnectorObject readObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid) {
		Assert.notNull(key);
		checkIcfType(key);
		return icfConnectors.get(key.getFramework()).readObject(key, connectorConfiguration, objectClass, uid);

	}

	@Override
	public IcfUidAttribute authenticateObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, String username, GuardedString password) {
		Assert.notNull(key);
		checkIcfType(key);
		return icfConnectors.get(key.getFramework()).authenticateObject(key, connectorConfiguration, objectClass,
				username, password);
	}

	private boolean checkIcfType(IcfConnectorKey key) {
		if (!icfConnectors.containsKey(key.getFramework())) {
			throw new ResultCodeException(IcfResultCode.ICF_FRAMEWORK_NOT_FOUND,
					ImmutableMap.of("icf", key.getFramework()));
		}
		return true;
	}

}
