package eu.bcvsolutions.idm.ic.connid.service.impl;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
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
import eu.bcvsolutions.idm.ic.filter.impl.IcResultsHandler;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorService;

@Service
public class ConnIdIcConnectorService implements IcConnectorService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnIdIcConnectorService.class);

	private ConnIdIcConfigurationService configurationServiceConnId;

	@Autowired
	public ConnIdIcConnectorService(IcConnectorFacade icConnectorAggregator,
			ConnIdIcConfigurationService configurationServiceConnId) {
		if (icConnectorAggregator.getIcConnectors() == null) {
			throw new IcException("Map of IC implementations is not defined!");
		}
		if (icConnectorAggregator.getIcConnectors().containsKey(IMPLEMENTATION_TYPE)) {
			throw new IcException(
					MessageFormat.format("IC implementation duplicity for key: {0}", IMPLEMENTATION_TYPE));
		}
		icConnectorAggregator.getIcConnectors().put(IMPLEMENTATION_TYPE, this);
		this.configurationServiceConnId = configurationServiceConnId;
	}

	final private static String IMPLEMENTATION_TYPE = "connId";

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
		log.debug("Create object - ConnId ({} {})", connectorInstance.getConnectorKey().toString(), attributes.toString());

		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);
		Set<Attribute> connIdAttributes = new HashSet<>();
		for (IcAttribute icAttribute : attributes) {
			connIdAttributes.add(ConnIdIcConvertUtil.convertIcAttribute(icAttribute));
		}
		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		Uid uid = conn.create(objectClassConnId, connIdAttributes, null);
		log.debug("Created object - ConnId ({} {}) Uid= {}", connectorInstance.getConnectorKey().toString(), attributes.toString(), uid);
		return ConnIdIcConvertUtil.convertConnIdUid(uid);
	}

	@Override
	public IcUidAttribute updateObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid, List<IcAttribute> replaceAttributes) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(replaceAttributes);
		Assert.notNull(uid);

		log.debug("Update object - ConnId (Uid= {} {} {})", uid, connectorInstance.getConnectorKey().toString(), replaceAttributes.toString());

		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);
		Set<Attribute> connIdAttributes = new HashSet<>();
		for (IcAttribute icAttribute : replaceAttributes) {
			connIdAttributes.add(ConnIdIcConvertUtil.convertIcAttribute(icAttribute));
		}
		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		Uid updatedUid = conn.update(objectClassConnId, ConnIdIcConvertUtil.convertIcUid(uid), connIdAttributes,
				null);
		log.debug("Updated object - ConnId ({} {}) Uid= {})", connectorInstance.getConnectorKey().toString(), replaceAttributes.toString(), updatedUid);
		return ConnIdIcConvertUtil.convertConnIdUid(updatedUid);
	}

	@Override
	public void deleteObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(uid);
		log.debug("Delete object - ConnId (Uid= {} {})", uid, connectorInstance.getConnectorKey().toString());

		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);

		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		conn.delete(objectClassConnId, ConnIdIcConvertUtil.convertIcUid(uid), null);
		log.debug("Deleted object - ConnId ({}) Uid= {}", connectorInstance.getConnectorKey().toString(), uid);
	}

	@Override
	public IcConnectorObject readObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(uid);
		log.debug("Read object - ConnId (Uid= {} {})", uid, connectorInstance.getConnectorKey().toString());

		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);

		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		ConnectorObject connObject = conn.getObject(objectClassConnId, ConnIdIcConvertUtil.convertIcUid(uid), null);
		log.debug("Readed object - ConnId ({}) Uid= {}", connObject, uid);
		return ConnIdIcConvertUtil.convertConnIdConnectorObject(connObject);
	}

	@Override
	public IcUidAttribute authenticateObject(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, String username, GuardedString password) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(username);
		log.debug("Authenticate object - ConnId (username= {} {})", username, connectorInstance.getConnectorKey().toString());

		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);

		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}
		try {
			IcUidAttribute uid = ConnIdIcConvertUtil.convertConnIdUid(conn.authenticate(objectClassConnId, username,
					new org.identityconnectors.common.security.GuardedString(password.asString().toCharArray()), null));
			log.debug("Authenticated object - ConnId (Uid= {})", uid);
			return uid;
		} catch (InvalidCredentialException ex) {
			throw new ResultCodeException(IcResultCode.AUTH_FAILED, ex);
		}

	}

	@Override
	public IcSyncToken synchronization(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcSyncToken token, IcSyncResultsHandler handler) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(objectClass);
		Assert.notNull(handler);
		log.debug("Start synchronization for connector {} and objectClass {} - ConnId", connectorInstance.getConnectorKey().toString(), objectClass.getDisplayName());
		
		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);

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
		
		SyncToken resultToken =  conn.sync(objectClassConnId, syncToken, handlerConnId, null);
		return ConnIdIcConvertUtil.convertConnIdSyncToken(resultToken);

	}
	
	@Override
	public void search(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcFilter filter, IcResultsHandler handler){
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		Assert.notNull(objectClass);
		Assert.notNull(handler);
		
		log.debug("Start search for connector {} and objectClass {} and filter {} - ConnId", connectorInstance.getConnectorKey().toString(), objectClass.getDisplayName(), filter);
		
		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);

		ObjectClass objectClassConnId = ConnIdIcConvertUtil.convertIcObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}
		
		ResultsHandler handlerConnId = new ResultsHandler() {
			
			@Override
			public boolean handle(ConnectorObject connectorObject) {
				
				return handler.handle(ConnIdIcConvertUtil.convertConnIdConnectorObject(connectorObject));
			}
		};
		Filter filterConnId = ConnIdIcConvertUtil.convertIcFilter(filter);
	
		conn.search(objectClassConnId, filterConnId, handlerConnId, null);
	}

	private ConnectorFacade getConnectorFacade(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorConfiguration);
		ConnectorInfo connIdInfo = configurationServiceConnId.getConnIdConnectorInfo(connectorInstance);
		Assert.notNull(connIdInfo, "ConnId connector info not found!");
		APIConfiguration config = connIdInfo.createDefaultAPIConfiguration();
		Assert.notNull(config.getConfigurationProperties(), "ConnId connector configuration properties not found!");
		config = ConnIdIcConvertUtil.convertIcConnectorConfiguration(connectorConfiguration, config);
		// Use the ConnectorFacadeFactory's newInstance() method to get a new
		// connector.
		ConnectorFacade conn = ConnectorFacadeFactory.getManagedInstance().newInstance(config);

		// Make sure we have set up the Configuration properly
		conn.validate();
		return conn;
	}

}
