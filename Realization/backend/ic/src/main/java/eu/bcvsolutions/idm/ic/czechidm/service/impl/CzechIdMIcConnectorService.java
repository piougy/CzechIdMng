package eu.bcvsolutions.idm.ic.czechidm.service.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSyncResultsHandler;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.api.operation.IcCanCreate;
import eu.bcvsolutions.idm.ic.api.operation.IcCanDelete;
import eu.bcvsolutions.idm.ic.api.operation.IcCanRead;
import eu.bcvsolutions.idm.ic.api.operation.IcCanSearch;
import eu.bcvsolutions.idm.ic.api.operation.IcCanUpdate;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcResultsHandler;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorService;

@Service
/**
 * Implementation for call CzechIdM connectors
 * 
 * @author svandav
 *
 */
public class CzechIdMIcConnectorService implements IcConnectorService {

	private static final Logger LOG = LoggerFactory.getLogger(CzechIdMIcConnectorService.class);

	private CzechIdMIcConfigurationService configurationService;
	private ApplicationContext applicationContext;

	@Autowired
	public CzechIdMIcConnectorService(IcConnectorFacade icConnectorAggregator,
			CzechIdMIcConfigurationService configurationService, ApplicationContext applicationContext) {
		Assert.notNull(applicationContext);

		this.applicationContext = applicationContext;

		if (icConnectorAggregator.getIcConnectors() == null) {
			throw new IcException("Map of IC implementations is not defined!");
		}
		if (icConnectorAggregator.getIcConnectors().containsKey(IMPLEMENTATION_TYPE)) {
			throw new IcException(
					MessageFormat.format("IC implementation duplicity for key: {0}", IMPLEMENTATION_TYPE));
		}
		icConnectorAggregator.getIcConnectors().put(IMPLEMENTATION_TYPE, this);
		this.configurationService = configurationService;
	}

	final private static String IMPLEMENTATION_TYPE = "czechidm";

	@Override
	public String getImplementationType() {
		return IMPLEMENTATION_TYPE;
	}

	@Override
	public IcUidAttribute createObject(IcConnectorInstance connectorInstance,
			IcConnectorConfiguration connectorConfiguration, IcObjectClass objectClass, List<IcAttribute> attributes) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(attributes);
		String key = connectorInstance.getConnectorKey().toString();

		LOG.debug("Create object - CzechIdM ({} {})", key, attributes.toString());

		IcConnector connector = this.getConnectorInstance(connectorInstance, connectorConfiguration);
		if (!(connector instanceof IcCanCreate)) {
			throw new IcException(MessageFormat.format("Connector [{0}] not supports create operation!", key));
		}

		IcUidAttribute uid = ((IcCanCreate) connector).create(objectClass, attributes);

		LOG.debug("Created object - CzechIdM ({} {}) Uid= {}", key, attributes.toString(), uid);
		return null;
	}

	@Override
	public IcUidAttribute updateObject(IcConnectorInstance connectorInstance,
			IcConnectorConfiguration connectorConfiguration, IcObjectClass objectClass, IcUidAttribute uid,
			List<IcAttribute> replaceAttributes) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(replaceAttributes);
		Assert.notNull(uid);

		String key = connectorInstance.getConnectorKey().toString();
		LOG.debug("Update object - CzechIdM (Uid= {} {} {})", uid, key, replaceAttributes.toString());

		IcConnector connector = this.getConnectorInstance(connectorInstance, connectorConfiguration);
		if (!(connector instanceof IcCanUpdate)) {
			throw new IcException(MessageFormat.format("Connector [{0}] not supports update operation!", key));
		}

		IcUidAttribute updatedUid = ((IcCanUpdate) connector).update(uid, objectClass, replaceAttributes);
		LOG.debug("Updated object - CzechIdM ({} {}) Uid= {})", connectorInstance.getConnectorKey().toString(),
				replaceAttributes.toString(), updatedUid);
		return updatedUid;
	}

	@Override
	public void deleteObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(uid);
		String key = connectorInstance.getConnectorKey().toString();
		LOG.debug("Delete object - CzechIdM (Uid= {} {})", uid, key);

		IcConnector connector = this.getConnectorInstance(connectorInstance, connectorConfiguration);
		if (!(connector instanceof IcCanDelete)) {
			throw new IcException(MessageFormat.format("Connector [{0}] not supports delete operation!", key));
		}

		((IcCanDelete) connector).delete(uid, objectClass);
		LOG.debug("Deleted object - CzechIdM ({}) Uid= {}", key, uid);
	}

	@Override
	public IcConnectorObject readObject(IcConnectorInstance connectorInstance,
			IcConnectorConfiguration connectorConfiguration, IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(uid);

		String key = connectorInstance.getConnectorKey().toString();
		LOG.debug("Read object - CzechIdM (Uid= {} {})", uid, key);

		IcConnector connector = this.getConnectorInstance(connectorInstance, connectorConfiguration);
		if (!(connector instanceof IcCanRead)) {
			throw new IcException(MessageFormat.format("Connector [{0}] not supports read operation!", key));
		}

		IcConnectorObject object = ((IcCanRead) connector).read(uid, objectClass);
		LOG.debug("Readed object - CzechIdM ({}) Uid= {}", object, uid);
		return object;
	}

	@Override
	public IcUidAttribute authenticateObject(IcConnectorInstance connectorInstance,
			IcConnectorConfiguration connectorConfiguration, IcObjectClass objectClass, String username,
			GuardedString password) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(username);

		String key = connectorInstance.getConnectorKey().toString();
		LOG.debug("Authenticate object - CzechIdM (username= {} {})", username, key);
		throw new IcException(MessageFormat.format("Connector [{0}] not supports authentication operation!", key));
	}

	@Override
	public IcSyncToken synchronization(IcConnectorInstance connectorInstance,
			IcConnectorConfiguration connectorConfiguration, IcObjectClass objectClass, IcSyncToken token,
			IcSyncResultsHandler handler) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(objectClass);
		Assert.notNull(handler);

		String key = connectorInstance.getConnectorKey().toString();
		LOG.debug("Start synchronization for connector {} and objectClass {} - CzechIdM", key,
				objectClass.getDisplayName());

		throw new IcException(MessageFormat.format("Connector [{0}] not supports sync operation!", key));
	}

	@Override
	public void search(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcFilter filter, IcResultsHandler handler) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(objectClass);
		Assert.notNull(handler);

		String key = connectorInstance.getConnectorKey().toString();
		LOG.debug("Start search for connector {} and objectClass {} and filter {} - CzechIdM", key,
				objectClass.getDisplayName(), filter);

		IcConnector connector = this.getConnectorInstance(connectorInstance, connectorConfiguration);
		if (!(connector instanceof IcCanSearch)) {
			throw new IcException(MessageFormat.format("Connector [{0}] not supports search operation!", key));
		}

		((IcCanSearch) connector).search(objectClass, filter, handler);

	}

	@Override
	public IcConnector getConnectorInstance(IcConnectorInstance connectorInstance,
			IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);

		String key = connectorInstance.getConnectorKey().getFullName();
		// Cache is not thread safety
		// if(this.connectorInstances.containsKey(key)){
		// IcConnector connector = connectorInstances.get(key);
		// connector.init(connectorConfiguration);
		// return connector;
		// }

		Class<? extends IcConnector> connectorClass = this.configurationService.getConnectorClass(connectorInstance);
		try {

			IcConnector connector = connectorClass.newInstance();
			// Manually autowire on this connector instance
			this.applicationContext.getAutowireCapableBeanFactory().autowireBean(connector);
			connector.init(connectorConfiguration);
			// this.connectorInstances.put(key, connector);
			return connector;

		} catch (InstantiationException | IllegalAccessException e) {
			throw new IcException(e);
		}
	}

}
