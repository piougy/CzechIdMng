package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningResult;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.service.api.NotificationManager;

/**
 * Prepare provisioning - resolve account properties and resolve create or update operations
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class PrepareAccountProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PrepareAccountProcessor.class);
	private final SysSystemMappingService systemMappingService;
	private final SysSystemAttributeMappingService attributeMappingService;
	private final IcConnectorFacade connectorFacade;
	private final SysSystemService systemService;
	private final NotificationManager notificationManager;
	private final SysProvisioningOperationRepository provisioningOperationRepository;
	
	@Autowired
	public PrepareAccountProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			SysSystemEntityService systemEntityService,
			NotificationManager notificationManager,
			SysProvisioningOperationRepository provisioningOperationRepository,
			SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService) {
		super(ProvisioningOperationType.CREATE, ProvisioningOperationType.UPDATE);
		//
		Assert.notNull(systemEntityService);
		Assert.notNull(systemMappingService);
		Assert.notNull(attributeMappingService);
		Assert.notNull(connectorFacade);
		Assert.notNull(systemService);
		Assert.notNull(notificationManager);
		Assert.notNull(provisioningOperationRepository);
		//
		this.systemMappingService = systemMappingService;
		this.attributeMappingService = attributeMappingService;
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.notificationManager = notificationManager;
		this.provisioningOperationRepository = provisioningOperationRepository;
	}
	
	/**
	 * Prepare provisioning operation execution
	 */
	@Override
	public EventResult<SysProvisioningOperation> process(EntityEvent<SysProvisioningOperation> event) {		
		SysProvisioningOperation provisioningOperation = event.getContent();
		SysSystem system = provisioningOperation.getSystem();
		IcObjectClass objectClass = provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass();
		LOG.debug("Start preparing attribubes for provisioning operation [{}] for object with uid [{}] and connector object [{}]", 
				provisioningOperation.getOperationType(),
				provisioningOperation.getSystemEntityUid(),
				objectClass.getType());
		//
		// Find connector identification persisted in system
		if (system.getConnectorKey() == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", provisioningOperation.getSystem().getName()));
		}
		// load connector configuration
		IcConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(provisioningOperation.getSystem());
		if (connectorConfig == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		//
		try {
			IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, provisioningOperation.getSystemEntityUid(), null);	
			IcConnectorObject existsConnectorObject = connectorFacade.readObject(
					provisioningOperation.getSystem().getConnectorKey(), 
					connectorConfig, 
					objectClass, 
					uidAttribute);
			if (existsConnectorObject == null) {
				processCreate(provisioningOperation, connectorConfig);
			} else {
				processUpdate(provisioningOperation, connectorConfig, existsConnectorObject);
			}
			//
			provisioningOperationRepository.save(provisioningOperation);
			//
			LOG.debug("Preparing attribubes for provisioning operation [{}] for object with uid [{}] and connector object [{}] is sucessfully completed", 
					provisioningOperation.getOperationType(), 
					provisioningOperation.getSystemEntityUid(),
					objectClass.getType());
			return new DefaultEventResult<>(event, this);
		} catch (Exception ex) {	
			LOG.error("Preparing attribubes for provisioning operation [{}] for object with uid [{}] and connector object [{}] failed", 
					provisioningOperation.getOperationType(), 
					provisioningOperation.getSystemEntityUid(),
					objectClass.getType(),
					ex);
			SysProvisioningRequest request = provisioningOperation.getRequest();
			if (provisioningOperation.getRequest() == null) {
				request = new SysProvisioningRequest(provisioningOperation);
				provisioningOperation.setRequest(request);
			}
			request.setResult(new SysProvisioningResult.Builder(ResultState.EXCEPTION).setCode("EX").setCause(ex).build()); // TODO: code
			//
			provisioningOperationRepository.save(provisioningOperation);
			//
			notificationManager.send(
					AccModuleDescriptor.TOPIC_PROVISIONING, 
					new IdmMessage.Builder(NotificationLevel.ERROR)
						.setSubject("Provisioning účtu [" + provisioningOperation.getSystemEntityUid() + "] neproběhl")
						.setMessage("Provisioning účtu [" + provisioningOperation.getSystemEntityUid() + "] na systém [" + provisioningOperation.getSystem().getName() + "] selhal.")
						.build());
			return new DefaultEventResult<>(event, this, true);
		}
	}
	
	/**
	 * Create object on target system
	 * 
	 * @param provisioningOperation
	 * @param connectorConfig
	 */
	private void processCreate(SysProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig) {
		SysSystem system = provisioningOperation.getSystem();
		ProvisioningContext provisioningContext = provisioningOperation.getProvisioningContext();
		IcConnectorObject connectorObject = provisioningContext.getConnectorObject();
		//
		// prepare provisioning attributes from account attributes
		if (provisioningContext.getAccountObject() != null) {
			for (AttributeMapping attributeHandling : findAttributeMappings(system, provisioningOperation.getEntityType())) {
				if (!provisioningContext.getAccountObject().containsKey(attributeHandling.getSchemaAttribute().getId())) {
					continue;
				}
				IcAttribute createdAttribute = createAttribute( 
						attributeHandling,
						provisioningContext.getAccountObject().get(attributeHandling.getSchemaAttribute().getId()));
				if (createdAttribute != null) {
					connectorObject.getAttributes().add(createdAttribute);
				}
			}
		}
		provisioningOperation.setOperationType(ProvisioningOperationType.CREATE);
	}
	
	private void processUpdate(SysProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig, IcConnectorObject existsConnectorObject) {
		SysSystem system = provisioningOperation.getSystem();
		String systemEntityUid = provisioningOperation.getSystemEntityUid();
		ProvisioningContext provisioningContext = provisioningOperation.getProvisioningContext();
		IcConnectorObject connectorObject = provisioningContext.getConnectorObject();
		IcObjectClass objectClass = connectorObject.getObjectClass();
		//
		IcConnectorObject updateConnectorObject;
		if (provisioningContext.getAccountObject() == null) {
			updateConnectorObject = connectorObject;
		} else {
			updateConnectorObject = new IcConnectorObjectImpl(systemEntityUid, objectClass, null);
			for (AttributeMapping attributeMapping : findAttributeMappings(system, provisioningOperation.getEntityType())) {
				if (!provisioningContext.getAccountObject().containsKey(attributeMapping.getSchemaAttribute().getId())) {
					continue;
				}
				SysSchemaAttribute schemaAttribute = attributeMapping.getSchemaAttribute();
				if (schemaAttribute.isUpdateable()) {
					if (!schemaAttribute.isReturnedByDefault()) {
						// TODO update for attributes not returned by default
						// (for example __PASSWORD__)
					} else {
						// Update attribute on resource by given mapping
						// attribute and mapped value in entity
						IcAttribute updatedAttribute = updateAttribute(
								systemEntityUid, 
								provisioningContext.getAccountObject().get(schemaAttribute.getId()), 
								attributeMapping, 
								existsConnectorObject);
						if (updatedAttribute != null) {
							updateConnectorObject.getAttributes().add(updatedAttribute);
						}
					}
				}
			}
		}
		//
		provisioningOperation.getProvisioningContext().setConnectorObject(updateConnectorObject);
		provisioningOperation.setOperationType(ProvisioningOperationType.UPDATE);
	}
	
	private SysSystemMapping getMapping(SysSystem system, SystemEntityType entityType) {
		List<SysSystemMapping> systemMappings = systemMappingService.findBySystem(system, SystemOperationType.PROVISIONING, entityType);
		if (systemMappings == null || systemMappings.isEmpty()) {
			throw new IllegalStateException(MessageFormat.format("System [{0}] does not have mapping, provisioning will not be executed. Add some mapping for entity type [{1}]", system.getName(), entityType));
		}
		if (systemMappings.size() != 1) {
			throw new IllegalStateException(MessageFormat.format("System [{0}] is wrong configured! Remove duplicit mapping for entity type [{1}]", system.getName(), entityType));
		}
		return systemMappings.get(0);
	}
	
	/**
	 * Find list of {@link SysSystemAttributeMapping} by provisioning type and
	 * entity type on given system
	 * 
	 * @param provisioningType
	 * @param entityType
	 * @param system
	 * @return
	 */
	private List<? extends AttributeMapping> findAttributeMappings(SysSystem system, SystemEntityType entityType) {
		return attributeMappingService.findBySystemMapping(getMapping(system, entityType));
	}
	
	/**
	 * Create IC attribute by schema attribute. IC attribute will be set with
	 * value obtained form given entity. This value will be transformed to
	 * system value first.
	 * 
	 * @param uid
	 * @param entity
	 * @param connectorObjectForCreate
	 * @param attributeMapping
	 * @param schemaAttribute
	 * @param objectClassName
	 */
	private IcAttribute createAttribute(AttributeMapping attributeMapping, Object idmValue){
		if (!attributeMapping.getSchemaAttribute().isCreateable()) {
			return null;
		}
		return attributeMappingService.createIcAttribute(attributeMapping, idmValue);
	}

	/**
	 * Update attribute on resource by given handling attribute and mapped value
	 * in entity
	 * 
	 * @param uid
	 * @param entity
	 * @param attributeMapping
	 * @param schemaAttribute
	 * @param connectorObject
	 */
	private IcAttribute updateAttribute(String uid, Object idmValue, AttributeMapping attributeMapping, IcConnectorObject existsConnectorObject) {
		List<IcAttribute> icAttributes = existsConnectorObject.getAttributes();
		SysSchemaAttribute schemaAttribute = attributeMapping.getSchemaAttribute();
		//
		Optional<IcAttribute> icAttributeOptional = icAttributes.stream()
				.filter(ica -> {
					return schemaAttribute.getName().equals(ica.getName());
					})
				.findFirst();
		IcAttribute icAttribute = null;
		if (icAttributeOptional.isPresent()) {
			icAttribute = icAttributeOptional.get();
		}
	
		return updateAttributeValue(uid, idmValue, attributeMapping, icAttribute, icAttributes);
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
	 * @param icAttribute
	 */
	private IcAttribute updateAttributeValue(String uid, Object idmValue, AttributeMapping attributeHandling, IcAttribute icAttribute, List<IcAttribute> icAttributes){

		Object icValueTransformed = null;
		if (attributeHandling.getSchemaAttribute().isMultivalued()) {
			// Multi value
			List<Object> icValues = icAttribute != null ? icAttribute.getValues() : null;
			icValueTransformed = attributeMappingService.transformValueFromResource(icValues, attributeHandling,
					icAttributes);
		} else {
			// Single value
			Object icValue = icAttribute != null ? icAttribute.getValue() : null;
			icValueTransformed = attributeMappingService.transformValueFromResource(icValue, attributeHandling,
					icAttributes);
		}
		if (!Objects.equals(idmValue, icValueTransformed)) {
			// values is not equals
			return attributeMappingService.createIcAttribute(attributeHandling, idmValue);
		}
		return null;
	}
	
	@Override
	public int getOrder() {
		// before realization
		return -1000;
	}
}
