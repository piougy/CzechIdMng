package eu.bcvsolutions.idm.acc.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.SysSchemaAttributeHandlingService;
import eu.bcvsolutions.idm.acc.service.SysSystemEntityHandlingService;
import eu.bcvsolutions.idm.acc.service.SysSystemService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.SysProvisioningService;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfConnectorObject;
import eu.bcvsolutions.idm.icf.api.IcfObjectClass;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.icf.dto.IcfAttributeDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorObjectDto;
import eu.bcvsolutions.idm.icf.dto.IcfObjectClassDto;
import eu.bcvsolutions.idm.icf.dto.IcfUidAttributeDto;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorFacade;

/**
 * Service for do provisioning or synchronisation or reconciliation
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysProvisioningService implements SysProvisioningService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSysProvisioningService.class);
	private SysSystemEntityHandlingService entityHandlingService;
	private SysSchemaAttributeHandlingService attributeHandlingService;
	private IcfConnectorFacade connectorFacade;
	private SysSystemService systemService;
	private AccIdentityAccountService identityAccoutnService;

	@Autowired
	public DefaultSysProvisioningService(SysSystemEntityHandlingService entityHandlingService,
			SysSchemaAttributeHandlingService attributeHandlingService, IcfConnectorFacade connectorFacade,
			SysSystemService systemService, AccIdentityAccountService identityAccoutnService) {
		super();
		this.entityHandlingService = entityHandlingService;
		this.attributeHandlingService = attributeHandlingService;
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.identityAccoutnService = identityAccoutnService;
	}

	@Override
	public void doIdentityProvisioning(IdmIdentity identity) {
		Assert.notNull(identity);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentity(identity);
		Page<AccIdentityAccount> identityAccounts = identityAccoutnService.find(filter, null);
		List<AccIdentityAccount> idenittyAccoutnList = identityAccounts.getContent();
		if (idenittyAccoutnList == null) {
			return;
		}
		idenittyAccoutnList.stream().forEach((identityAccount) -> {
			doOperation(identityAccount.getAccount().getUid(), identity, SystemOperationType.PROVISIONING,
					SystemEntityType.IDENTITY, identityAccount.getAccount().getSystem());
		});
	}

	/**
	 * Do provisioning/synchronisation/reconciliation for given identity account
	 * 
	 * @param identityAccount
	 * @param operation
	 */
	public void doAccountOperation(AccIdentityAccount identityAccount, SystemOperationType operation) {
		Assert.notNull(operation);
		Assert.notNull(identityAccount);

		doOperation(identityAccount.getAccount().getUid(), identityAccount.getIdentity(), operation,
				SystemEntityType.IDENTITY, identityAccount.getAccount().getSystem());
	}

	/**
	 * Do provisioning/synchronisation/reconciliation on given system for given
	 * entity
	 * 
	 * @param uid
	 * @param entity
	 * @param operation
	 * @param entityType
	 * @param system
	 */
	public void doOperation(String uid, AbstractEntity entity, SystemOperationType operation,
			SystemEntityType entityType, SysSystem system) {
		Assert.notNull(uid);
		Assert.notNull(operation);
		Assert.notNull(system);
		Assert.notNull(entityType);

		List<SysSystemEntityHandling> entityHandlingList = entityHandlingService.findBySystem(system, operation,
				entityType);
		if (entityHandlingList == null || entityHandlingList.size() != 1) {
			return;
		}

		SysSystemEntityHandling entityHandling = entityHandlingList.get(0);
		List<SysSchemaAttributeHandling> attributes = attributeHandlingService.findByEntityHandling(entityHandling);
		if (attributes == null || attributes.isEmpty()) {
			return;
		}

		// Find connector identification persisted in system
		IcfConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcfConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		IcfUidAttribute uidAttribute = new IcfUidAttributeDto(null, uid, null);

		Map<String, IcfConnectorObject> objectByClassMap = new HashMap<>();

		for (SysSchemaAttributeHandling ah : attributes) {
			String objectClassName = ah.getSchemaAttribute().getObjectClass().getObjectClassName();
			if (!objectByClassMap.containsKey(objectClassName)) {
				IcfObjectClass icfObjectClass = new IcfObjectClassDto(objectClassName);
				IcfConnectorObject connectorObject = connectorFacade.readObject(connectorKey, connectorConfig, icfObjectClass, uidAttribute);
				objectByClassMap.put(objectClassName, connectorObject);
			}
		}

		if (SystemOperationType.PROVISIONING == operation) {
			// Provisioning
			doProvisioning(uid, entity, attributes, connectorKey, connectorConfig, objectByClassMap);

		} else {
			// TODO Synchronisation or reconciliace
		}

	}

	/**
	 * Do provisioning for given entity
	 * 
	 * @param uid
	 * @param entity
	 * @param attributes
	 * @param connectorKey
	 * @param connectorConfig
	 * @param uidAttribute
	 * @param objectByClassMap
	 */
	private void doProvisioning(String uid, AbstractEntity entity, List<SysSchemaAttributeHandling> attributes,
			IcfConnectorKey connectorKey, IcfConnectorConfiguration connectorConfig,
			Map<String, IcfConnectorObject> objectByClassMap) {

		Map<String, IcfConnectorObject> objectByClassMapForUpdate = new HashMap<>();
		Map<String, IcfConnectorObject> objectByClassMapForCreate = new HashMap<>();
		IcfUidAttribute uidAttribute = new IcfUidAttributeDto(null, uid, null);

		for (SysSchemaAttributeHandling attributeHandling : attributes) {
			SysSchemaAttribute schemaAttribute = attributeHandling.getSchemaAttribute();
			String objectClassName = schemaAttribute.getObjectClass().getObjectClassName();
			IcfConnectorObject connectorObject = objectByClassMap.get(objectClassName);
			if (connectorObject == null) {
				// Create new connector object for this object class
				createAttributeForNewConnectorObject(uid, entity, objectByClassMapForCreate, attributeHandling,
						schemaAttribute, objectClassName);

			} else {
				// Update connector object
				if (schemaAttribute.isUpdateable()) {
					if (!schemaAttribute.isReturnedByDefault()) {
						// TODO update for attributes not returned by default
						// (for example __PASSWORD__)
					} else {
						// Update attribute on resource by given handling
						// attribute and mapped value in entity
						updateAttributeForExistingConnecotrObject(uid, entity, objectByClassMapForUpdate,
								attributeHandling, schemaAttribute, objectClassName, connectorObject);
					}
				}
			}
		}

		// call create on ICF module
		objectByClassMapForCreate.forEach((objectClassName, connectorObject) -> {
			connectorFacade.createObject(connectorKey, connectorConfig,
					connectorObject.getObjectClass(), connectorObject.getAttributes());
		});

		// call update on ICF module
		objectByClassMapForUpdate.forEach((objectClassName, connectorObject) -> {
			connectorFacade.updateObject(connectorKey, connectorConfig,
					connectorObject.getObjectClass(), uidAttribute, connectorObject.getAttributes());
		});

	}

	private void createAttributeForNewConnectorObject(String uid, AbstractEntity entity,
			Map<String, IcfConnectorObject> objectByClassMapForCreate, SysSchemaAttributeHandling attributeHandling,
			SysSchemaAttribute schemaAttribute, String objectClassName) {
		if (schemaAttribute.isCreateable()) {
			try {
				Object idmValue = null;
				if (attributeHandling.isUid()) {
					// When is attribute marked as UID, then we use as
					// value input uid
					idmValue = uid;
				} else {
					// Find value from entity by property name
					idmValue = getEntityValue(entity, attributeHandling.getIdmPropertyName());

				}
				IcfAttribute icfAttributeForCreate = createIcfAttribute(attributeHandling, schemaAttribute.getName(),
						idmValue);

				IcfConnectorObject connectorObjectForCreate = objectByClassMapForCreate.get(objectClassName);
				if (connectorObjectForCreate == null) {
					IcfObjectClass ioc = new IcfObjectClassDto(objectClassName);
					connectorObjectForCreate = new IcfConnectorObjectDto(ioc, null);
					objectByClassMapForCreate.put(objectClassName, connectorObjectForCreate);
				}

				connectorObjectForCreate.getAttributes().add(icfAttributeForCreate);

			} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
						ImmutableMap.of("uid", uid, "property", attributeHandling.getIdmPropertyName()), e);
			}
		}
	}

	/**
	 * Update attribute on resource by given handling attribute and mapped value
	 * in entity
	 * 
	 * @param uid
	 * @param entity
	 * @param objectByClassMapForUpdate
	 * @param attributeHandling
	 * @param schemaAttribute
	 * @param objectClassName
	 * @param connectorObject
	 */
	private void updateAttributeForExistingConnecotrObject(String uid, AbstractEntity entity,
			Map<String, IcfConnectorObject> objectByClassMapForUpdate, SysSchemaAttributeHandling attributeHandling,
			SysSchemaAttribute schemaAttribute, String objectClassName, IcfConnectorObject connectorObject) {
		List<IcfAttribute> icfAttributes = connectorObject.getAttributes();

		Optional<IcfAttribute> icfAttributeOptional = icfAttributes.stream().filter(icfa -> {
			return schemaAttribute.getName().equals(icfa.getName());
		}).findFirst();
		if (!icfAttributeOptional.isPresent()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_RESOURCE_ATTRIBUTE_NOT_FOUND,
					ImmutableMap.of("uid", uid, "attribute", schemaAttribute.getName()));
		}
		IcfAttribute icfAttribute = icfAttributeOptional.get();
		if (schemaAttribute.isMultivalued()) {
			// TODO multi value
		} else {
			// Single value
			updateAttributeSingleValue(uid, entity, objectByClassMapForUpdate, attributeHandling, objectClassName,
					icfAttribute);
		}
	}

	/**
	 * Check difference of attribute value on resource and in entity for given
	 * attribute. When is value changed, then add update of this attribute to
	 * map
	 * 
	 * @param uid
	 * @param entity
	 * @param objectByClassMapForUpdate
	 * @param attributeHandling
	 * @param objectClassName
	 * @param icfAttribute
	 */
	private void updateAttributeSingleValue(String uid, AbstractEntity entity,
			Map<String, IcfConnectorObject> objectByClassMapForUpdate, SysSchemaAttributeHandling attributeHandling,
			String objectClassName, IcfAttribute icfAttribute) {

		Object icfValue = icfAttribute.getValue();
		Object icfValueTransformed = attributeHandlingService.transformValueFromSystem(icfValue, attributeHandling);

		try {
			Object idmValue = null;
			if (attributeHandling.isUid()) {
				// When is attribute marked as UID, then we use as value input
				// uid
				idmValue = uid;
			} else {
				// Find value from entity by property name
				idmValue = getEntityValue(entity, attributeHandling.getIdmPropertyName());

			}

			if (!Objects.equals(idmValue, icfValueTransformed)) {
				// values is not equals
				IcfAttribute icfAttributeForUpdate = createIcfAttribute(attributeHandling, icfAttribute.getName(),
						idmValue);

				IcfConnectorObject connectorObjectForUpdate = objectByClassMapForUpdate.get(objectClassName);
				if (connectorObjectForUpdate == null) {
					IcfObjectClass ioc = new IcfObjectClassDto(objectClassName);
					connectorObjectForUpdate = new IcfConnectorObjectDto(ioc, null);
				}
				connectorObjectForUpdate.getAttributes().add(icfAttributeForUpdate);
				objectByClassMapForUpdate.put(objectClassName, connectorObjectForUpdate);
			}

		} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
					ImmutableMap.of("uid", uid, "property", attributeHandling.getIdmPropertyName()), e);
		}
	}

	/**
	 * Create instance of ICF attribute for given name. Given idm value will be
	 * transformed to resource.
	 * 
	 * @param attributeHandling
	 * @param icfAttribute
	 * @param idmValue
	 * @return
	 */
	private IcfAttribute createIcfAttribute(SysSchemaAttributeHandling attributeHandling, String attributeName,
			Object idmValue) {
		Object idmValueTransformed = attributeHandlingService.transformValueToSystem(idmValue, attributeHandling);
		IcfAttribute icfAttributeForUpdate = new IcfAttributeDto(attributeName, idmValueTransformed);
		return icfAttributeForUpdate;
	}

	/**
	 * Return object from entity for given property name
	 * 
	 * @param entity
	 * @param propertyName
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private Object getEntityValue(AbstractEntity entity, String propertyName)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Optional<PropertyDescriptor> propertyDescriptionOptional = Arrays
				.asList(Introspector.getBeanInfo(entity.getClass(), AbstractEntity.class).getPropertyDescriptors())
				.stream().filter(propertyDescriptor -> {
					return propertyName.equals(propertyDescriptor.getName());
				}).findFirst();
		if (!propertyDescriptionOptional.isPresent()) {
			throw new IllegalAccessException("Field " + propertyName + " not found!");
		}
		PropertyDescriptor propertyDescriptor = propertyDescriptionOptional.get();

		return propertyDescriptor.getReadMethod().invoke(entity);
	}

}
