package eu.bcvsolutions.idm.icf.connid.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfConnectorObject;
import eu.bcvsolutions.idm.icf.api.IcfObjectClass;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.icf.connid.domain.IcfConvertUtilConnId;
import eu.bcvsolutions.idm.icf.domain.IcfResultCode;
import eu.bcvsolutions.idm.icf.exception.IcfException;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorService;
import eu.bcvsolutions.idm.icf.service.impl.IcfConnectorFacadeDefault;
import eu.bcvsolutions.idm.security.domain.GuardedString;

@Service
public class IcfConnectorServiceConnId implements IcfConnectorService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IcfConnectorServiceConnId.class);

	private IcfConfigurationServiceConnId configurationServiceConnId;

	@Autowired
	public IcfConnectorServiceConnId(IcfConnectorFacadeDefault icfConnectorAggregator,
			IcfConfigurationServiceConnId configurationServiceConnId) {
		if (icfConnectorAggregator.getIcfConnectors() == null) {
			throw new IcfException("Map of ICF implementations is not defined!");
		}
		if (icfConnectorAggregator.getIcfConnectors().containsKey(this.getIcfType())) {
			throw new IcfException("ICF implementation duplicity for key: " + this.getIcfType());
		}
		icfConnectorAggregator.getIcfConnectors().put(this.getIcfType(), this);
		this.configurationServiceConnId = configurationServiceConnId;
	}

	@Override
	public String getIcfType() {
		return "connId";
	}

	@Override
	public IcfUidAttribute createObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, List<IcfAttribute> attributes) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		Assert.notNull(attributes);
		log.debug("Create object - ConnId (" + key.toString() + " " + attributes.toString() + ")");

		ConnectorFacade conn = getConnectorFacade(key, connectorConfiguration);
		Set<Attribute> connIdAttributes = new HashSet<>();
		if (attributes != null) {
			for (IcfAttribute icfAttribute : attributes) {
				connIdAttributes.add(IcfConvertUtilConnId.convertIcfAttribute(icfAttribute));
			}
		}
		ObjectClass objectClassConnId = IcfConvertUtilConnId.convertIcfObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		Uid uid = conn.create(objectClassConnId, connIdAttributes, null);
		log.debug("Created object - ConnId (" + key.toString() + " " + attributes.toString() + ") Uid= " + uid);
		return IcfConvertUtilConnId.convertConnIdUid(uid);
	}

	@Override
	public IcfUidAttribute updateObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid, List<IcfAttribute> replaceAttributes) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		Assert.notNull(replaceAttributes);
		Assert.notNull(uid);
		log.debug("Update object - ConnId (Uid= " + uid + " " + key.toString() + " " + replaceAttributes.toString()
				+ ")");

		ConnectorFacade conn = getConnectorFacade(key, connectorConfiguration);
		Set<Attribute> connIdAttributes = new HashSet<>();
		if (replaceAttributes != null) {
			for (IcfAttribute icfAttribute : replaceAttributes) {
				connIdAttributes.add(IcfConvertUtilConnId.convertIcfAttribute(icfAttribute));
			}
		}
		ObjectClass objectClassConnId = IcfConvertUtilConnId.convertIcfObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		Uid updatedUid = conn.update(objectClassConnId, IcfConvertUtilConnId.convertIcfUid(uid), connIdAttributes,
				null);
		log.debug("Updated object - ConnId (" + key.toString() + " " + replaceAttributes.toString() + ") Uid= "
				+ updatedUid);
		return IcfConvertUtilConnId.convertConnIdUid(updatedUid);
	}

	@Override
	public void deleteObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		Assert.notNull(uid);
		log.debug("Delete object - ConnId (Uid= " + uid + " " + key.toString() + ")");

		ConnectorFacade conn = getConnectorFacade(key, connectorConfiguration);

		ObjectClass objectClassConnId = IcfConvertUtilConnId.convertIcfObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		conn.delete(objectClassConnId, IcfConvertUtilConnId.convertIcfUid(uid), null);
		log.debug("Deleted object - ConnId (" + key.toString() + ") Uid= " + uid);
	}

	@Override
	public IcfConnectorObject readObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		Assert.notNull(uid);
		log.debug("Read object - ConnId (Uid= " + uid + " " + key.toString() + ")");

		ConnectorFacade conn = getConnectorFacade(key, connectorConfiguration);

		ObjectClass objectClassConnId = IcfConvertUtilConnId.convertIcfObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}

		ConnectorObject connObject = conn.getObject(objectClassConnId, IcfConvertUtilConnId.convertIcfUid(uid), null);
		log.debug("Readed object - ConnId (" + connObject + ") Uid= " + uid);
		return IcfConvertUtilConnId.convertConnIdConnectorObject(connObject);
	}

	@Override
	public IcfUidAttribute authenticateObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, String username, GuardedString password) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		Assert.notNull(username);
		log.debug("Authenticate object - ConnId (username= " + username + " " + key.toString() + ")");

		ConnectorFacade conn = getConnectorFacade(key, connectorConfiguration);

		ObjectClass objectClassConnId = IcfConvertUtilConnId.convertIcfObjectClass(objectClass);
		if (objectClassConnId == null) {
			objectClassConnId = ObjectClass.ACCOUNT;
		}
		try {
			IcfUidAttribute uid = IcfConvertUtilConnId.convertConnIdUid(conn.authenticate(objectClassConnId, username,
					new org.identityconnectors.common.security.GuardedString(password.asString().toCharArray()), null));
			log.debug("Authenticated object - ConnId (" + uid + ") Uid= " + uid);
			return uid;
		} catch (InvalidCredentialException ex) {
			throw new ResultCodeException(IcfResultCode.AUTH_FAILED, ex);
		}

	}
	
	public void synchronization(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass) {
		
		throw new NotImplementedException();

	}

	private ConnectorFacade getConnectorFacade(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		ConnectorInfo connIdInfo = configurationServiceConnId.getConnIdConnectorInfo(key);
		Assert.notNull(connIdInfo, "ConnId connector info not found!");
		APIConfiguration config = connIdInfo.createDefaultAPIConfiguration();
		Assert.notNull(config.getConfigurationProperties(), "ConnId connector configuration properties not found!");
		config = IcfConvertUtilConnId.convertIcfConnectorConfiguration(connectorConfiguration, config);
		// Use the ConnectorFacadeFactory's newInstance() method to get a new
		// connector.
		ConnectorFacade conn = ConnectorFacadeFactory.getManagedInstance().newInstance(config);

		// Make sure we have set up the Configuration properly
		conn.validate();
		return conn;
	}

}
