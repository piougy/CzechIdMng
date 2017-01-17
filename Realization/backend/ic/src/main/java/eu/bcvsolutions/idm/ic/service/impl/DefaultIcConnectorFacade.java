package eu.bcvsolutions.idm.ic.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSyncResultsHandler;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.domain.IcResultCode;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Service for do active operations on all IC implementations
 * 
 * @author svandav
 *
 */
@Service
public class DefaultIcConnectorFacade implements IcConnectorFacade {

	private Map<String, IcConnectorService> icConnectors = new HashMap<>();

	/**
	 * @return Connector services for all ICs
	 */
	@Override
	public Map<String, IcConnectorService> getIcConnectors() {
		return icConnectors;
	}

	@Override
	public IcUidAttribute createObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, List<IcAttribute> attributes) {
		Assert.notNull(key);
		checkIcType(key);
		return icConnectors.get(key.getFramework()).createObject(key, connectorConfiguration, objectClass, attributes);

	}

	@Override
	public IcUidAttribute updateObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid, List<IcAttribute> replaceAttributes) {
		Assert.notNull(key);
		checkIcType(key);
		return icConnectors.get(key.getFramework()).updateObject(key, connectorConfiguration, objectClass, uid,
				replaceAttributes);

	}

	@Override
	public void deleteObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(key);
		checkIcType(key);
		icConnectors.get(key.getFramework()).deleteObject(key, connectorConfiguration, objectClass, uid);

	}
	
	@Override
	public IcConnectorObject readObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(key);
		checkIcType(key);
		return icConnectors.get(key.getFramework()).readObject(key, connectorConfiguration, objectClass, uid);

	}

	@Override
	public IcUidAttribute authenticateObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, String username, GuardedString password) {
		Assert.notNull(key);
		checkIcType(key);
		return icConnectors.get(key.getFramework()).authenticateObject(key, connectorConfiguration, objectClass,
				username, password);
	}

	@Override
	public IcSyncToken synchronization(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcSyncToken token, IcSyncResultsHandler handler) {
		Assert.notNull(key);
		checkIcType(key);
		return icConnectors.get(key.getFramework()).synchronization(key, connectorConfiguration, objectClass, token, handler);
	}

	private boolean checkIcType(IcConnectorKey key) {
		if (!icConnectors.containsKey(key.getFramework())) {
			throw new ResultCodeException(IcResultCode.IC_FRAMEWORK_NOT_FOUND,
					ImmutableMap.of("ic", key.getFramework()));
		}
		return true;
	}


}
