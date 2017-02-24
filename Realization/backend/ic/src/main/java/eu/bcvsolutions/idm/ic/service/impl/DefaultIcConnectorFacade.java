package eu.bcvsolutions.idm.ic.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSyncResultsHandler;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.domain.IcResultCode;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcResultsHandler;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorService;

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
	public IcUidAttribute createObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, List<IcAttribute> attributes) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		return icConnectors.get(connectorInstance.getConnectorKey().getFramework()).createObject(connectorInstance, connectorConfiguration, objectClass, attributes);

	}

	@Override
	public IcUidAttribute updateObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid, List<IcAttribute> replaceAttributes) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		return icConnectors.get(connectorInstance.getConnectorKey().getFramework()).updateObject(connectorInstance, connectorConfiguration, objectClass, uid,
				replaceAttributes);

	}

	@Override
	public void deleteObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		icConnectors.get(connectorInstance.getConnectorKey().getFramework()).deleteObject(connectorInstance, connectorConfiguration, objectClass, uid);

	}
	
	@Override
	public IcConnectorObject readObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		return icConnectors.get(connectorInstance.getConnectorKey().getFramework()).readObject(connectorInstance, connectorConfiguration, objectClass, uid);

	}

	@Override
	public IcUidAttribute authenticateObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, String username, GuardedString password) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		return icConnectors.get(connectorInstance.getConnectorKey().getFramework()).authenticateObject(connectorInstance, connectorConfiguration, objectClass,
				username, password);
	}

	@Override
	public IcSyncToken synchronization(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcSyncToken token, IcSyncResultsHandler handler) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		return icConnectors.get(connectorInstance.getConnectorKey().getFramework()).synchronization(connectorInstance, connectorConfiguration, objectClass, token, handler);
	}
	
	@Override
	public void search(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration, IcObjectClass objectClass,
			IcFilter filter, IcResultsHandler handler){
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		icConnectors.get(connectorInstance.getConnectorKey().getFramework()).search(connectorInstance, connectorConfiguration, objectClass, filter, handler);
	}
	

	private boolean checkIcType(IcConnectorKey key) {
		if (!icConnectors.containsKey(key.getFramework())) {
			throw new ResultCodeException(IcResultCode.IC_FRAMEWORK_NOT_FOUND,
					ImmutableMap.of("ic", key.getFramework()));
		}
		return true;
	}



}
