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
import eu.bcvsolutions.idm.ic.api.IcConnectorCreate;
import eu.bcvsolutions.idm.ic.api.IcConnectorDelete;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcConnectorRead;
import eu.bcvsolutions.idm.ic.api.IcConnectorUpdate;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSyncResultsHandler;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcResultsHandler;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorService;

@Service
/**
 * Implementation for call CzechIdM connectors
 * @author svandav
 *
 */
public class CzechIdMIcConnectorService implements IcConnectorService {

	private static final Logger LOG = LoggerFactory.getLogger(CzechIdMIcConnectorService.class);

	private CzechIdMIcConfigurationService configurationService;
	private ApplicationContext applicationContext;
	// Cached connector instances
	private Map<String, IcConnector> connectorInstances = new HashMap<>();

	@Autowired
	public CzechIdMIcConnectorService(
			IcConnectorFacade icConnectorAggregator,
			CzechIdMIcConfigurationService configurationService,
			ApplicationContext applicationContext) {
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
	public IcUidAttribute createObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, List<IcAttribute> attributes) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(attributes);
		String key = connectorInstance.getConnectorKey().toString();
		
		LOG.debug("Create object - CzechIdM ({} {})", key, attributes.toString());
		
		IcConnector connector = this.getConnectorInstance(connectorInstance, connectorConfiguration);
		if(!(connector instanceof IcConnectorCreate)){
			throw new IcException(MessageFormat.format("Connector [{0}] not supports create operation!", key));
		}
		
		IcUidAttribute uid = ((IcConnectorCreate)connector).create(objectClass, attributes);

		LOG.debug("Created object - CzechIdM ({} {}) Uid= {}", key, attributes.toString(), uid);
		return null;
	}

	@Override
	public IcUidAttribute updateObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid, List<IcAttribute> replaceAttributes) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(replaceAttributes);
		Assert.notNull(uid);
		
		String key = connectorInstance.getConnectorKey().toString();
		LOG.debug("Update object - CzechIdM (Uid= {} {} {})", uid, key, replaceAttributes.toString());
		
		IcConnector connector = this.getConnectorInstance(connectorInstance, connectorConfiguration);
		if(!(connector instanceof IcConnectorUpdate)){
			throw new IcException(MessageFormat.format("Connector [{0}] not supports update operation!", key));
		}
		
		IcUidAttribute updatedUid = ((IcConnectorUpdate)connector).update(uid, objectClass, replaceAttributes);
		LOG.debug("Updated object - CzechIdM ({} {}) Uid= {})", connectorInstance.getConnectorKey().toString(), replaceAttributes.toString(), updatedUid);
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
		if(!(connector instanceof IcConnectorDelete)){
			throw new IcException(MessageFormat.format("Connector [{0}] not supports delete operation!", key));
		}
		
		((IcConnectorDelete)connector).delete(uid, objectClass);
		LOG.debug("Deleted object - CzechIdM ({}) Uid= {}", key, uid);
	}

	@Override
	public IcConnectorObject readObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(uid);
		
		String key = connectorInstance.getConnectorKey().toString();
		LOG.debug("Read object - CzechIdM (Uid= {} {})", uid, key);
		
		IcConnector connector = this.getConnectorInstance(connectorInstance, connectorConfiguration);
		if(!(connector instanceof IcConnectorRead)){
			throw new IcException(MessageFormat.format("Connector [{0}] not supports read operation!", key));
		}
		
		IcConnectorObject object = ((IcConnectorRead)connector).read(uid, objectClass);
		LOG.debug("Readed object - CzechIdM ({}) Uid= {}", object, uid);
		return object;
	}

	@Override
	public IcUidAttribute authenticateObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, String username, GuardedString password) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(username);
		LOG.debug("Authenticate object - CzechIdM (username= {} {})", username, connectorInstance.getConnectorKey().toString());

//		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);
//
//		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
//		if (objectClassConnId == null) {
//			objectClassConnId = ObjectClass.ACCOUNT;
//		}
//		try {
//			IcUidAttribute uid = ConnIdIcConvertUtil.convertConnIdUid(conn.authenticate(objectClassConnId, username,
//					new org.identityconnectors.common.security.GuardedString(password.asString().toCharArray()), null));
//			log.debug("Authenticated object - CzechIdM (Uid= {})", uid);
//			return uid;
//		} catch (InvalidCredentialException ex) {
//			throw new ResultCodeException(IcResultCode.AUTH_FAILED, ex);
//		}
		
		return null;

	}

	@Override
	public IcSyncToken synchronization(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcSyncToken token, IcSyncResultsHandler handler) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(objectClass);
		Assert.notNull(handler);
		LOG.debug("Start synchronization for connector {} and objectClass {} - CzechIdM", connectorInstance.getConnectorKey().toString(), objectClass.getDisplayName());
		
//		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);
//
//		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
//		if (objectClassConnId == null) {
//			objectClassConnId = ObjectClass.ACCOUNT;
//		}
//		
//		SyncToken syncToken = ConnIdIcConvertUtil.convertIcSyncToken(token);
//		
//		SyncResultsHandler handlerConnId = new SyncResultsHandler() {
//			
//			@Override
//			public boolean handle(SyncDelta delta) {
//				return handler.handle(ConnIdIcConvertUtil.convertConnIdSyncDelta(delta));
//			}
//		};
//		
//		SyncToken resultToken =  conn.sync(objectClassConnId, syncToken, handlerConnId, null);
//		return ConnIdIcConvertUtil.convertConnIdSyncToken(resultToken);
		return null;

	}
	
	@Override
	public void search(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcFilter filter, IcResultsHandler handler){
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(objectClass);
		Assert.notNull(handler);
		
		LOG.debug("Start search for connector {} and objectClass {} and filter {} - CzechIdM", connectorInstance.getConnectorKey().toString(), objectClass.getDisplayName(), filter);
//		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);
//
//		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
//		if (objectClassConnId == null) {
//			objectClassConnId = ObjectClass.ACCOUNT;
//		}
//		
//		final SearchResultsHandler handlerConnId = new SearchResultsHandler() {
//			
//			@Override
//			public boolean handle(ConnectorObject connectorObject) {
//				
//				return handler.handle(ConnIdIcConvertUtil.convertConnIdConnectorObject(connectorObject));
//			}
//
//			@Override
//			public void handleResult(SearchResult result) {
//				// VS TODO: For all my tests was search result Null and this method (handle result) was not called!
//				log.debug("SearchResul was returned (pagination): cookie: " + result.getPagedResultsCookie() + "  --- remaining paged results: "+result.getRemainingPagedResults());
//			}
//		};
//		Filter filterConnId = ConnIdIcConvertUtil.convertIcFilter(filter);
//		
//		// For pagination - TODO
//		Map<String, Object> searchOpt = new HashMap<String, Object>();
//	    searchOpt.put(OperationOptions.OP_PAGE_SIZE, 100);
//	    searchOpt.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, 1);
//	    OperationOptions searchOptions = new OperationOptions(searchOpt);
//	    	
//		this.pageSearch(conn, objectClassConnId, filterConnId, handlerConnId, searchOptions);

	}


	private IcConnector getConnectorInstance(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);

		String key = connectorInstance.getConnectorKey().getFullName();
		if(this.connectorInstances.containsKey(key)){
			return connectorInstances.get(key);
		}
		
		
		Class<? extends IcConnector> connectorClass = this.configurationService.getConnectorClass(connectorInstance);
		try {
			
			IcConnector connector = connectorClass.newInstance();
			// Manually autowire on this connector instance
			this.applicationContext.getAutowireCapableBeanFactory().autowireBean(connector);
			connector.init(connectorConfiguration);
			this.connectorInstances.put(key, connector);
			return connector;
			
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IcException(e);
		}
		
	}

}
