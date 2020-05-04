package eu.bcvsolutions.idm.ic.connid.service.impl;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
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
import eu.bcvsolutions.idm.ic.connid.domain.ConnIdIcConvertUtil;
import eu.bcvsolutions.idm.ic.domain.IcResultCode;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcResultsHandler;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacadeFactory;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorService;

@Service
public class ConnIdIcConnectorService implements IcConnectorService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConnIdIcConnectorService.class);

	private final IcConnectorFacadeFactory facadeFactory;

	@Autowired
	public ConnIdIcConnectorService(IcConnectorFacade icConnectorAggregator,
									IcConnectorFacadeFactory facadeFactory) {
		if (icConnectorAggregator.getIcConnectors() == null) {
			throw new IcException("Map of IC implementations is not defined!");
		}
		if (icConnectorAggregator.getIcConnectors().containsKey(IMPLEMENTATION_TYPE)) {
			throw new IcException(
					MessageFormat.format("IC implementation duplicity for key: {0}", IMPLEMENTATION_TYPE));
		}
		icConnectorAggregator.getIcConnectors().put(IMPLEMENTATION_TYPE, this);
		this.facadeFactory = facadeFactory;
	}

	final private static String IMPLEMENTATION_TYPE = "connId";

	@Override
	public String getImplementationType() {
		return IMPLEMENTATION_TYPE;
	}

	@Override
	public IcUidAttribute createObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, List<IcAttribute> attributes) {
		Assert.notNull(connectorInstance, "Connector instance is required.");
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		Assert.notNull(attributes, "Attributes are required.");
		LOG.debug("Create object - ConnId ({} {})", connectorInstance.getConnectorKey().toString(), attributes.toString());

		ConnectorFacade conn = facadeFactory.getConnectorFacade(connectorInstance, connectorConfiguration);
		Set<Attribute> connIdAttributes = new HashSet<>(attributes.size());
		for (IcAttribute icAttribute : attributes) {
			connIdAttributes.add(ConnIdIcConvertUtil.convertIcAttribute(icAttribute));
		}
		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		Uid uid = conn.create(objectClassConnId, connIdAttributes, new OperationOptions(connectorConfiguration.getSystemOperationOptions()));
		LOG.debug("Created object - ConnId ({} {}) Uid= {}", connectorInstance.getConnectorKey().toString(), attributes.toString(), uid);
		return ConnIdIcConvertUtil.convertConnIdUid(uid);
	}

	@Override
	public IcUidAttribute updateObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid, List<IcAttribute> replaceAttributes) {
		Assert.notNull(connectorInstance, "Connector instance is required.");
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		Assert.notNull(replaceAttributes, "Replace attributes are required.");
		Assert.notNull(uid, "Uid is required.");

		LOG.debug("Update object - ConnId (Uid= {} {} {})", uid, connectorInstance.getConnectorKey().toString(), replaceAttributes.toString());

		ConnectorFacade conn = facadeFactory.getConnectorFacade(connectorInstance, connectorConfiguration);
		Set<Attribute> connIdAttributes = new HashSet<>(replaceAttributes.size());
		for (IcAttribute icAttribute : replaceAttributes) {
			connIdAttributes.add(ConnIdIcConvertUtil.convertIcAttribute(icAttribute));
		}
		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		Uid updatedUid = conn.update(objectClassConnId, ConnIdIcConvertUtil.convertIcUid(uid), connIdAttributes,
				new OperationOptions(connectorConfiguration.getSystemOperationOptions()));
		LOG.debug("Updated object - ConnId ({} {}) Uid= {})", connectorInstance.getConnectorKey().toString(), replaceAttributes.toString(), updatedUid);
		return ConnIdIcConvertUtil.convertConnIdUid(updatedUid);
	}

	@Override
	public void deleteObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(connectorInstance, "Connector instance is required.");
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		Assert.notNull(uid, "Uid is required.");
		LOG.debug("Delete object - ConnId (Uid= {} {})", uid, connectorInstance.getConnectorKey().toString());

		ConnectorFacade conn = facadeFactory.getConnectorFacade(connectorInstance, connectorConfiguration);

		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		conn.delete(objectClassConnId, ConnIdIcConvertUtil.convertIcUid(uid), new OperationOptions(connectorConfiguration.getSystemOperationOptions()));
		LOG.debug("Deleted object - ConnId ({}) Uid= {}", connectorInstance.getConnectorKey().toString(), uid);
	}

	@Override
	public IcConnectorObject readObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(connectorInstance, "Connector instance is required.");
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		Assert.notNull(uid, "Uid is required.");
		LOG.debug("Read object - ConnId (Uid= {} {})", uid, connectorInstance.getConnectorKey().toString());

		ConnectorFacade conn = facadeFactory.getConnectorFacade(connectorInstance, connectorConfiguration);

		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		ConnectorObject connObject = conn.getObject(objectClassConnId, ConnIdIcConvertUtil.convertIcUid(uid),
				new OperationOptions(connectorConfiguration.getSystemOperationOptions()));
		LOG.debug("Readed object - ConnId ({}) Uid= {}", connObject, uid);
		return ConnIdIcConvertUtil.convertConnIdConnectorObject(connObject);
	}

	@Override
	public IcUidAttribute authenticateObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, String username, GuardedString password) {
		Assert.notNull(connectorInstance, "Connector instance is required.");
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		Assert.notNull(username, "Username is required.");
		LOG.debug("Authenticate object - ConnId (username= {} {})", username, connectorInstance.getConnectorKey().toString());

		ConnectorFacade conn = facadeFactory.getConnectorFacade(connectorInstance, connectorConfiguration);

		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}
		try {
			IcUidAttribute uid = ConnIdIcConvertUtil.convertConnIdUid(conn.authenticate(objectClassConnId, username,
					new org.identityconnectors.common.security.GuardedString(password.asString().toCharArray()),
					new OperationOptions(connectorConfiguration.getSystemOperationOptions())));
			LOG.debug("Authenticated object - ConnId (Uid= {})", uid);
			return uid;
		} catch (InvalidCredentialException ex) {
			throw new ResultCodeException(IcResultCode.AUTH_FAILED, ex);
		}

	}

	@Override
	public IcSyncToken synchronization(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcSyncToken token, IcSyncResultsHandler handler) {
		Assert.notNull(connectorInstance, "Connector instance is required.");
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		Assert.notNull(objectClass, "Object class is required.");
		Assert.notNull(handler, "Handler is required.");
		LOG.debug("Start synchronization for connector {} and objectClass {} - ConnId", connectorInstance.getConnectorKey().toString(), objectClass.getDisplayName());
		
		ConnectorFacade conn = facadeFactory.getConnectorFacade(connectorInstance, connectorConfiguration);

		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}
		
		SyncToken syncToken = ConnIdIcConvertUtil.convertIcSyncToken(token);
		
		SyncResultsHandler handlerConnId = new SyncResultsHandler() {
			
			@Override
			public boolean handle(SyncDelta delta) {
				return handler.handle(ConnIdIcConvertUtil.convertConnIdSyncDelta(delta));
			}
		};
		
		SyncToken resultToken =  conn.sync(objectClassConnId, syncToken, handlerConnId, new OperationOptions(connectorConfiguration.getSystemOperationOptions()));
		return ConnIdIcConvertUtil.convertConnIdSyncToken(resultToken);

	}

	@Override
	public void search(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcFilter filter, IcResultsHandler handler) {
		Assert.notNull(connectorInstance, "Connector instance is required.");
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		Assert.notNull(objectClass, "Object class is required.");
		Assert.notNull(handler, "Handler is required.");
		
		LOG.debug("Start search for connector {} and objectClass {} and filter {} - ConnId", connectorInstance.getConnectorKey().toString(), objectClass.getDisplayName(), filter);
		ConnectorFacade conn = facadeFactory.getConnectorFacade(connectorInstance, connectorConfiguration);

		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}
		
		final SearchResultsHandler handlerConnId = new SearchResultsHandler() {
			
			@Override
			public boolean handle(ConnectorObject connectorObject) {
				
				return handler.handle(ConnIdIcConvertUtil.convertConnIdConnectorObject(connectorObject));
			}

			@Override
			public void handleResult(SearchResult result) {
				// VS TODO: For all my tests was search result Null and this method (handle result) was not called!
				LOG.debug("SearchResul was returned (pagination): cookie: {}  --- remaining paged results: {}", result.getPagedResultsCookie(), result.getRemainingPagedResults());
			}
		};
		Filter filterConnId = ConnIdIcConvertUtil.convertIcFilter(filter);
	    	
		this.pageSearch(conn, objectClassConnId, filterConnId, handlerConnId, new OperationOptions(connectorConfiguration.getSystemOperationOptions()));
	}
	
	private void pageSearch(ConnectorFacade conn, ObjectClass objectClass, Filter filter,
            ResultsHandler handler, OperationOptions options){
		SearchResult searchResutl = conn.search(objectClass, filter, handler, options);
		// For all my tests was search result Null.
		if(searchResutl != null){
			LOG.debug("SearchResul was returned (pagination): cookie: {}  --- offset: {}", options.getPagedResultsCookie(), options.getPagedResultsOffset());
			String cookie = searchResutl.getPagedResultsCookie();
			int remainingResult = searchResutl.getRemainingPagedResults();
			if(remainingResult > 0 && cookie != null){
				options.getOptions().put(OperationOptions.OP_PAGED_RESULTS_COOKIE, cookie);
				options.getOptions().put(OperationOptions.OP_PAGED_RESULTS_OFFSET, options.getPagedResultsOffset()+1);
				this.pageSearch(conn, objectClass, filter, handler, options);
			}
		}
	}

	@Override
	public IcConnector getConnectorInstance(IcConnectorInstance connectorInstance,
			IcConnectorConfiguration connectorConfiguration) {
		throw new IcException("Not supported yet!");
	}

}
