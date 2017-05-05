package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.ProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Prepare provisioning - resolve connector object properties from account and resolve create or update operations
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Prepares connector object from account properties. Resolves create or update provisioning operation (reads object from target system).")
public class PrepareConnectorObjectProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {

	public static final String PROCESSOR_NAME = "prepare-connector-object-processor";
	private static final String MODIFIED_FIELD_NAME = "modified";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PrepareConnectorObjectProcessor.class);
	private final SysSystemMappingService systemMappingService;
	private final SysSystemAttributeMappingService attributeMappingService;
	private final IcConnectorFacade connectorFacade;
	private final SysSystemService systemService;
	private final NotificationManager notificationManager;
	private final SysProvisioningOperationService provisioningOperationService;
	private final SysSchemaAttributeService schemaAttributeService;
	private final SysProvisioningArchiveService provisioningArchiveService;
	
	@Autowired
	public PrepareConnectorObjectProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			SysSystemEntityService systemEntityService,
			NotificationManager notificationManager,
			SysProvisioningOperationService provisioningOperationService,
			SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService,
			SysSchemaAttributeService schemaAttributeService,
			SysProvisioningArchiveService provisioningArchiveService) {
		super(ProvisioningEventType.CREATE, ProvisioningEventType.UPDATE);
		//
		Assert.notNull(systemEntityService);
		Assert.notNull(systemMappingService);
		Assert.notNull(attributeMappingService);
		Assert.notNull(connectorFacade);
		Assert.notNull(systemService);
		Assert.notNull(notificationManager);
		Assert.notNull(provisioningOperationService);
		Assert.notNull(schemaAttributeService);
		Assert.notNull(provisioningArchiveService);
		//
		this.systemMappingService = systemMappingService;
		this.attributeMappingService = attributeMappingService;
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.notificationManager = notificationManager;
		this.provisioningOperationService = provisioningOperationService;
		this.schemaAttributeService = schemaAttributeService;
		this.provisioningArchiveService = provisioningArchiveService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
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
					provisioningOperation.getSystem().getConnectorInstance(), 
					connectorConfig, 
					objectClass, 
					uidAttribute);
			if (existsConnectorObject == null) {
				processCreate(provisioningOperation);
			} else {
				processUpdate(provisioningOperation, connectorConfig, existsConnectorObject);
			}
			//
			provisioningOperationService.save(provisioningOperation);
			//
			LOG.debug("Preparing attribubes for provisioning operation [{}] for object with uid [{}] and connector object [{}] is sucessfully completed", 
					provisioningOperation.getOperationType(), 
					provisioningOperation.getSystemEntityUid(),
					objectClass.getType());
			return new DefaultEventResult<>(event, this);
		} catch (Exception ex) {	
			ResultModel resultModel;
			if (ex instanceof ResultCodeException) {
				resultModel = ((ResultCodeException) ex).getError().getError();
			} else {
				resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_PREPARE_ACCOUNT_ATTRIBUTES_FAILED, 
					ImmutableMap.of(
							"name", provisioningOperation.getSystemEntityUid(), 
							"system", system.getName(),
							"operationType", provisioningOperation.getOperationType(),
							"objectClass", objectClass.getType()));
			}
			LOG.error(resultModel.toString(), ex);
			provisioningOperation.getRequest().setResult(
					new OperationResult.Builder(OperationState.EXCEPTION).setModel(resultModel).setCause(ex).build());
			//
			provisioningOperationService.save(provisioningOperation);
			//
			notificationManager.send(
					AccModuleDescriptor.TOPIC_PROVISIONING, new IdmMessageDto.Builder()
					.setModel(resultModel)
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
	private void processCreate(SysProvisioningOperation provisioningOperation) {
		SysSystem system = provisioningOperation.getSystem();
		ProvisioningContext provisioningContext = provisioningOperation.getProvisioningContext();
		IcConnectorObject connectorObject = provisioningContext.getConnectorObject();
		//
		// prepare provisioning attributes from account attributes
		Map<ProvisioningAttributeDto, Object> fullAccountObject = provisioningOperationService.getFullAccountObject(provisioningOperation);
		if (fullAccountObject != null) {
			connectorObject.getAttributes().clear();
			
			SysSystemMapping mapping = getMapping(system, provisioningOperation.getEntityType());
			List<SysSchemaAttribute> schemaAttributes = findSchemaAttributes(system, mapping.getObjectClass());
			
			for (ProvisioningAttributeDto provisioningAttribute : fullAccountObject.keySet()) {
				
				Optional<SysSchemaAttribute> schemaAttributeOptional = schemaAttributes.stream().filter(schemaAttribute -> {
					return provisioningAttribute.getSchemaAttributeName().equals(schemaAttribute.getName());
				}).findFirst();
				
				if(!schemaAttributeOptional.isPresent()){
					throw new ProvisioningException(AccResultCode.PROVISIONING_SCHEMA_ATTRIBUTE_IS_FOUND, ImmutableMap.of("attribute", provisioningAttribute.getSchemaAttributeName()));
				}
				
				Object idmValue = fullAccountObject.get(provisioningAttribute);
				SysSchemaAttribute schemaAttribute = schemaAttributeOptional.get();
				
				if(provisioningAttribute.isSendOnlyIfNotNull()){
					if(this.isValueEmpty(idmValue)){
						// Skip this attribute (marked with flag sendOnlyIfNotNull), because IdM value is null							
						continue;
					}
				}
				
				if(AttributeMappingStrategyType.CREATE == provisioningAttribute.getStrategyType() 
						|| AttributeMappingStrategyType.WRITE_IF_NULL == provisioningAttribute.getStrategyType()){
					
					boolean existSetAttribute = fullAccountObject.keySet().stream().filter(provisioningAttributeKey -> {
						return provisioningAttributeKey.getSchemaAttributeName().equals(schemaAttribute.getName()) 
								&& AttributeMappingStrategyType.SET == provisioningAttributeKey.getStrategyType();
					}).findFirst().isPresent();
					
					boolean existIfResourceNulltAttribute = fullAccountObject.keySet().stream().filter(provisioningAttributeKey -> {
						return provisioningAttributeKey.getSchemaAttributeName().equals(schemaAttribute.getName()) 
								&& AttributeMappingStrategyType.WRITE_IF_NULL == provisioningAttributeKey.getStrategyType();
					}).findFirst().isPresent();
					
					boolean existMergeAttribute = fullAccountObject.keySet().stream().filter(provisioningAttributeKey -> {
						return provisioningAttributeKey.getSchemaAttributeName().equals(schemaAttribute.getName()) 
								&& AttributeMappingStrategyType.MERGE == provisioningAttributeKey.getStrategyType();
					}).findFirst().isPresent();
					
					boolean existAuthMergeAttribute = fullAccountObject.keySet().stream().filter(provisioningAttributeKey -> {
						return provisioningAttributeKey.getSchemaAttributeName().equals(schemaAttribute.getName()) 
								&& AttributeMappingStrategyType.AUTHORITATIVE_MERGE == provisioningAttributeKey.getStrategyType();
					}).findFirst().isPresent();
					
					if(AttributeMappingStrategyType.CREATE == provisioningAttribute.getStrategyType()){
				
						if(existIfResourceNulltAttribute || existSetAttribute || existAuthMergeAttribute || existMergeAttribute){
							// Skip this attribute (with Create strategy), because exists same attribute with SET/WRITE_IF_NULL/MERGE/AUTH_MERGE strategy 
							// (this strategies has higher priority)							
							continue;
						}
					}
					if(AttributeMappingStrategyType.WRITE_IF_NULL == provisioningAttribute.getStrategyType()){
						
						if(existSetAttribute || existAuthMergeAttribute || existMergeAttribute){
							// Skip this attribute (with WRITE_IF_NULL strategy), because exists same attribute with SET/MERGE/AUTH_MERGE strategy
							// (this strategies has higher priority)							
							continue;
						}
					}
					
				}
				
				IcAttribute createdAttribute = createAttribute( 
						schemaAttribute,
						fullAccountObject.get(provisioningAttribute));
				if (createdAttribute != null) {
					connectorObject.getAttributes().add(createdAttribute);
				}
			}
		}
		provisioningOperation.setOperationType(ProvisioningEventType.CREATE);
	}
	
	@SuppressWarnings("unchecked")
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
			Map<ProvisioningAttributeDto, Object> fullAccountObject = provisioningOperationService.getFullAccountObject(provisioningOperation);
			updateConnectorObject = new IcConnectorObjectImpl(systemEntityUid, objectClass, null);
			
			SysSystemMapping mapping = getMapping(system, provisioningOperation.getEntityType());
			List<SysSchemaAttribute> schemaAttributes = findSchemaAttributes(system, mapping.getObjectClass());
			
			ProvisioningOperationFilter filter = new  ProvisioningOperationFilter();
			filter.setEntityIdentifier(provisioningOperation.getEntityIdentifier());
			filter.setEntityType(provisioningOperation.getEntityType());
			filter.setResultState(OperationState.EXECUTED);
			
			SysProvisioningArchive lastSuccessEntity = null;
			
			for (ProvisioningAttributeDto provisioningAttribute : fullAccountObject.keySet()) {
				
				Optional<SysSchemaAttribute> schemaAttributeOptional = schemaAttributes.stream().filter(schemaAttribute -> {
					return provisioningAttribute.getSchemaAttributeName().equals(schemaAttribute.getName());
				}).findFirst();
				
				if(!schemaAttributeOptional.isPresent()){
					throw new ProvisioningException(AccResultCode.PROVISIONING_SCHEMA_ATTRIBUTE_IS_FOUND, ImmutableMap.of("attribute", provisioningAttribute.getSchemaAttributeName()));
				}
				
				SysSchemaAttribute schemaAttribute = schemaAttributeOptional.get();
				if (schemaAttribute.isUpdateable()) {
					if (schemaAttribute.isReturnedByDefault()) {	
						Object idmValue = fullAccountObject.get(provisioningAttribute);
						IcAttribute attribute = existsConnectorObject.getAttributeByName(schemaAttribute.getName());
						Object connectorValue = attribute != null ? (attribute.isMultiValue() ? attribute.getValues() : attribute.getValue()) : null;
						Object resultValue = idmValue;
						
						
						if(AttributeMappingStrategyType.CREATE == provisioningAttribute.getStrategyType()){
							// We do update, attributes with create strategy will be skipped
							continue;
						}
						
						if(provisioningAttribute.isSendOnlyIfNotNull()){
							if(this.isValueEmpty(idmValue)){
								// Skip this attribute (marked with flag sendOnlyIfNotNull), because idm value is null							
								continue;
							}
						}
						
						if(AttributeMappingStrategyType.WRITE_IF_NULL == provisioningAttribute.getStrategyType()){
							
							boolean existSetAttribute = fullAccountObject.keySet().stream().filter(provisioningAttributeKey -> {
								return provisioningAttributeKey.getSchemaAttributeName().equals(schemaAttribute.getName()) 
										&& AttributeMappingStrategyType.SET == provisioningAttributeKey.getStrategyType();
							}).findFirst().isPresent();
							
							boolean existMergeAttribute = fullAccountObject.keySet().stream().filter(provisioningAttributeKey -> {
								return provisioningAttributeKey.getSchemaAttributeName().equals(schemaAttribute.getName()) 
										&& AttributeMappingStrategyType.MERGE == provisioningAttributeKey.getStrategyType();
							}).findFirst().isPresent();
							
							boolean existAuthMergeAttribute = fullAccountObject.keySet().stream().filter(provisioningAttributeKey -> {
								return provisioningAttributeKey.getSchemaAttributeName().equals(schemaAttribute.getName()) 
										&& AttributeMappingStrategyType.AUTHORITATIVE_MERGE == provisioningAttributeKey.getStrategyType();
							}).findFirst().isPresent();
							
							if(AttributeMappingStrategyType.WRITE_IF_NULL == provisioningAttribute.getStrategyType()){
								List<IcAttribute> icAttributes = existsConnectorObject.getAttributes();
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
								// We need do transform from resource first
								Object transformedConnectorValue = this.transformValueFromResource(provisioningAttribute.getTransformValueFromResourceScript()
										, schemaAttribute, icAttribute, icAttributes, system);
								
								if(transformedConnectorValue != null || existSetAttribute || existAuthMergeAttribute || existMergeAttribute){
									// Skip this attribute (with Write if null strategy), because connector value is not null
									// or exists same attribute with  SET/MERGE/AUTH_MERGE strategy (this strategies has higher priority)								
									continue;
								}
							}
							
						}
						
						if(AttributeMappingStrategyType.MERGE == provisioningAttribute.getStrategyType()){
							
							// Load last provisioning history
							if(lastSuccessEntity == null){
								List<SysProvisioningArchive> lastSuccessEntities = provisioningArchiveService.find(filter,
										new PageRequest(0, 1, new Sort(Direction.DESC, MODIFIED_FIELD_NAME))).getContent();
								if(!lastSuccessEntities.isEmpty()){
									lastSuccessEntity = lastSuccessEntities.get(0);
								}
							}
							
						 	// Merge IdM values with connector values
							if(connectorValue instanceof List){
								List<Object> connectorValues = new ArrayList<>((List<Object>)connectorValue);
								List<Object> idmValues = null;
								if(idmValue instanceof List){
									idmValues = (List<Object>) idmValue;
								}
								if(idmValues != null){
			
									idmValues.stream().forEach(value -> {
										if(!connectorValues.contains(value)){
											connectorValues.add(value);
										}
									});
								} 
	
								resultValue = connectorValues;
							}

							// Delete missing values by last provisioning history
							if( lastSuccessEntity != null && lastSuccessEntity.getProvisioningContext() != null 
									&& lastSuccessEntity.getProvisioningContext().getAccountObject() != null 
									&& lastSuccessEntity.getProvisioningContext().getAccountObject().containsKey(provisioningAttribute)){
								Object oldValue = lastSuccessEntity.getProvisioningContext().getAccountObject().get(provisioningAttribute);
								if(oldValue instanceof List){
									if(!oldValue.equals(idmValue)){
										// Search all deleted values (managed by IdM) by founded last provisioning values
										List<?> deletedValues = ((List<?>)oldValue).stream().filter(value -> {
											List<?> idmValues = null;
											if(idmValue instanceof List){
												idmValues = (List<?>) idmValue;
											}
											if(idmValues != null && idmValues.contains(value)){
												return false;
											} 
											return true;
										}).collect(Collectors.toList());
										if(resultValue instanceof List){
											List<?> resultValues = new ArrayList<>((List<Object>)resultValue);
											
											// Remove all deleted values (managed by IdM) 
											resultValues.removeAll(deletedValues);
											resultValue = resultValues;
										}
									}
								}
							}
						}
						
					 	// Update attribute on resource by given mapping
						// attribute and mapped value in entity
						IcAttribute updatedAttribute = updateAttribute(
								systemEntityUid, 
								resultValue, 
								schemaAttribute, 
								existsConnectorObject,
								system,
								provisioningAttribute);
						if (updatedAttribute != null) {
							updateConnectorObject.getAttributes().add(updatedAttribute);
						}
					}
				}
			}
		}
		//
		provisioningOperation.getProvisioningContext().setConnectorObject(updateConnectorObject);
		provisioningOperation.setOperationType(ProvisioningEventType.UPDATE);
	}
	
	private boolean isValueEmpty(Object idmValue){
		if(idmValue == null){
			return true;
		}
		
		if(idmValue instanceof String && Strings.isNullOrEmpty((String) idmValue)){
			return true;
		}
		
		return false;
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
	 * Find list of {@link SysSchemaAttribute} by system and objectClass
	 * 
	 * @param objectClass
	 * @param system
	 * @return
	 */
	private List<SysSchemaAttribute> findSchemaAttributes(SysSystem system, SysSchemaObjectClass objectClass) {
		
		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		schemaAttributeFilter.setObjectClassId(objectClass.getId());
		return schemaAttributeService.find(schemaAttributeFilter, null).getContent();
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
	private IcAttribute createAttribute(SysSchemaAttribute schemaAttribute, Object idmValue){
		if (!schemaAttribute.isCreateable()) {
			return null;
		}
		return attributeMappingService.createIcAttribute(schemaAttribute, idmValue);
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
	private IcAttribute updateAttribute(String uid, Object idmValue, SysSchemaAttribute schemaAttribute, IcConnectorObject existsConnectorObject, SysSystem system, ProvisioningAttributeDto provisioningAttributeDto) {
		List<IcAttribute> icAttributes = existsConnectorObject.getAttributes();
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
	
		return updateAttributeValue(uid, idmValue, schemaAttribute, icAttribute, icAttributes, system, provisioningAttributeDto.getTransformValueFromResourceScript(), provisioningAttributeDto.isSendAlways());
	}

	/**
	 * Check difference of attribute value on resource and in entity for given
	 * attribute. When is value changed, then add update of this attribute to
	 * map
	 * 
	 */
	private IcAttribute updateAttributeValue(String uid, Object idmValue, SysSchemaAttribute schemaAttribute, IcAttribute icAttribute, List<IcAttribute> icAttributes, SysSystem system, String transformValueFromResourceScript, boolean sendAlways){

		Object icValueTransformed = transformValueFromResource(transformValueFromResourceScript, schemaAttribute, icAttribute,
				icAttributes, system);
		if (sendAlways || (!Objects.equals(idmValue, icValueTransformed))) {
			// values is not equals
			// Or this attribute must be send every time (event if was not changed)
			return attributeMappingService.createIcAttribute(schemaAttribute, idmValue);
		}
		return null;
	}

	private Object transformValueFromResource(String transformValueFromResourceScript,
			SysSchemaAttribute schemaAttribute, IcAttribute icAttribute, List<IcAttribute> icAttributes,
			SysSystem system) {
		
		Object icValueTransformed = null;
		if (schemaAttribute.isMultivalued()) {
			// Multi value
			List<Object> icValues = icAttribute != null ? icAttribute.getValues() : null;
			icValueTransformed = attributeMappingService.transformValueFromResource(icValues, transformValueFromResourceScript,
					icAttributes, system);
		} else {
			// Single value
			Object icValue = icAttribute != null ? icAttribute.getValue() : null;
			icValueTransformed = attributeMappingService.transformValueFromResource(icValue, transformValueFromResourceScript,
					icAttributes, system);
		}
		return icValueTransformed;
	}
	
	@Override
	public int getOrder() {
		// before realization
		return -1000;
	}
}
