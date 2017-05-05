package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.MappingAttributeDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Abstract service for do provisioning
 * 
 * @author svandav
 *
 */
public abstract class AbstractProvisioningExecutor<ENTITY extends AbstractEntity> implements ProvisioningEntityExecutor<ENTITY>{

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractProvisioningExecutor.class);
	protected final SysSystemMappingService systemMappingService;
	protected final SysSystemAttributeMappingService attributeMappingService;
	private final IcConnectorFacade connectorFacade;
	private final SysSystemService systemService;
	protected final SysRoleSystemAttributeService roleSystemAttributeService;
	private final SysSystemEntityService systemEntityService;
	protected final AccAccountService accountService;
	private final ProvisioningExecutor provisioningExecutor;

	@Autowired
	public AbstractProvisioningExecutor(SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			SysRoleSystemService roleSystemService, AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService,
			ProvisioningExecutor provisioningExecutor) {

		Assert.notNull(systemMappingService);
		Assert.notNull(attributeMappingService);
		Assert.notNull(connectorFacade);
		Assert.notNull(systemService);
		Assert.notNull(roleSystemService);
		Assert.notNull(accountManagementService);
		Assert.notNull(roleSystemAttributeService);
		Assert.notNull(systemEntityService);
		Assert.notNull(accountService);
		Assert.notNull(provisioningExecutor);
		//
		this.systemMappingService = systemMappingService;
		this.attributeMappingService = attributeMappingService;
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.roleSystemAttributeService = roleSystemAttributeService;
		this.systemEntityService = systemEntityService;
		this.accountService = accountService;
		this.provisioningExecutor = provisioningExecutor;
	}

	@Override
	public abstract void doProvisioning(AccAccount account);
	
	@Override
	public void doProvisioning(ENTITY entity) {
		Assert.notNull(entity);
		//
		EntityAccountFilter filter = createEntityAccountFilter();
		filter.setEntityId(entity.getId());
		filter.setOwnership(true);
		@SuppressWarnings("unchecked")
		List<EntityAccountDto> entityAccoutnList =  this.getEntityAccountService().findDto((BaseFilter) filter, null).getContent();

		List<UUID> accounts = new ArrayList<>();
		entityAccoutnList.stream().forEach((entityAccount) -> {
			if (!accounts.contains(entityAccount.getAccount())) {
				accounts.add(entityAccount.getAccount());
			}
		});

		accounts.stream().forEach(account -> {
			this.doProvisioning(accountService.get(account), entity);
		});
	}

	@Override
	public void doProvisioning(AccAccount account, ENTITY entity) {
		Assert.notNull(account);
		Assert.notNull(entity);
		//
		ProvisioningOperationType operationType;
		SysSystem system = account.getSystem();
		SysSystemEntity systemEntity = getSystemEntity(account);
		if (systemEntity == null) {
			// prepare system entity - uid could be changed by provisioning, but we need to link her with account
			// First we try find system entity with same uid. 
			systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.getByClass(entity.getClass()), account.getUid());
			if (systemEntity == null) {
				systemEntity = new SysSystemEntity();
				systemEntity.setEntityType(SystemEntityType.getByClass(entity.getClass()));
				systemEntity.setSystem(system);
				systemEntity.setUid(account.getUid());
				systemEntity.setWish(true);
				systemEntity = systemEntityService.save(systemEntity);
			}
			account.setSystemEntity(systemEntity);
			account = accountService.save(account);
			operationType = ProvisioningOperationType.CREATE; // we wont create account, but after target system call can be switched to UPDATE
		} else {
			operationType = ProvisioningOperationType.UPDATE; // we wont update account, but after target system call can be switched to CREATE
		}
		
		List<AttributeMapping> finalAttributes = resolveMappedAttributes(account.getUid(), account, entity, system, systemEntity.getEntityType());
		if(CollectionUtils.isEmpty(finalAttributes)){
			// nothing to do - mapping is empty
			return;
		}
		
		doProvisioning(systemEntity, entity, operationType, finalAttributes);		
	}
	
	@Override
	public void doDeleteProvisioning(AccAccount account) {
		Assert.notNull(account);
		SysSystemEntity systemEntity = getSystemEntity(account);
		//
		if (systemEntity != null){
			doProvisioning(systemEntity, null, ProvisioningOperationType.DELETE, null);
		}
	}
	
	@Override
	public void changePassword(ENTITY entity, PasswordChangeDto passwordChange) {
		Assert.notNull(entity);
		Assert.notNull(passwordChange);

		EntityAccountFilter filter = this.createEntityAccountFilter();
		filter.setEntityId(entity.getId());
		@SuppressWarnings("unchecked")
		List<? extends EntityAccountDto> entityAccountList = getEntityAccountService().findDto((BaseFilter)filter, null).getContent();
		if (entityAccountList == null) {
			return;
		}
		
		// Distinct by accounts
		List<AccAccount> accounts = new ArrayList<>();
		entityAccountList.stream().filter(entityAccount -> {
			return entityAccount.isOwnership() && (passwordChange.isAll()
					|| passwordChange.getAccounts().contains(entityAccount.getId().toString()));
		}).forEach(entityAccount -> {
			if (!accounts.contains(entityAccount.getAccount())) {
				accounts.add(accountService.get(entityAccount.getAccount()));
			}
		});

		accounts.forEach(account -> {
			// find uid from system entity or from account
			String uid = account.getUid();
			SysSystem system = account.getSystem();
			SysSystemEntity systemEntity = account.getSystemEntity();
			//
			// Find mapped attributes (include overloaded attributes)
			List<AttributeMapping> finalAttributes = resolveMappedAttributes(uid, account, entity, system, systemEntity.getEntityType());
			if (CollectionUtils.isEmpty(finalAttributes)) {
				return;
			}
			
			// We try find __PASSWORD__ attribute in mapped attributes
			Optional<? extends AttributeMapping> attriubuteHandlingOptional = finalAttributes.stream()
					.filter((attribute) -> {
						return ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME.equals(attribute.getSchemaAttribute().getName());
					}).findFirst();
			if (!attriubuteHandlingOptional.isPresent()) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_PASSWORD_FIELD_NOT_FOUND,
						ImmutableMap.of("uid", uid));
			}
			AttributeMapping mappedAttribute = attriubuteHandlingOptional.get();

			doProvisioningForAttribute(systemEntity, mappedAttribute, passwordChange.getNewPassword(),
					ProvisioningOperationType.UPDATE, entity);
		});
	}
	
	@Override
	public void createAccountsForAllSystems(ENTITY entity){
		SystemEntityType entityType = SystemEntityType.getByClass(entity.getClass());
		List<SysSystemMapping> systemMappings = findSystemMappingsForEntityType(entity, entityType);
		systemMappings.forEach(mapping -> {
			UUID accountId = this.getAccountByEntity(entity.getId(), mapping.getSystem().getId());
			if(accountId != null){
				// We already have account for this system -> next
				return;
			}
			List<SysSystemAttributeMapping> mappedAttributes = attributeMappingService.findBySystemMapping(mapping);
			SysSystemAttributeMapping uidAttribute = attributeMappingService.getUidAttribute(mappedAttributes, mapping.getSystem());
			String uid = attributeMappingService.generateUid(entity, uidAttribute);
			
			// Create AccAccount and relation between account and entity
			createEntityAccount(uid, entity.getId(), mapping.getSystem().getId());
		});
	}
	
	/**
	 * Returns system entity associated to given account 
	 * 
	 * @param account
	 * @return
	 */
	private SysSystemEntity getSystemEntity(AccAccount account) {
		//
		// TODO: we can find sysstem entity on target system, if no one existst etc.
		//
		return account.getSystemEntity();
	}

	/**
	 * Validate attributes on incompatible strategies
	 * @param finalAttributes
	 */
	protected void validateAttributesStrategy(List<AttributeMapping> finalAttributes) {
		if(finalAttributes == null){
			return;
		}
		finalAttributes.forEach(parentAttribute -> {
			if(AttributeMappingStrategyType.MERGE == parentAttribute.getStrategyType() || AttributeMappingStrategyType.AUTHORITATIVE_MERGE == parentAttribute.getStrategyType()){
				Optional<AttributeMapping> conflictAttributeOptional = finalAttributes.stream().filter(att -> {
					return att.getSchemaAttribute().equals(parentAttribute.getSchemaAttribute()) 
							&& !(att.getStrategyType() == parentAttribute.getStrategyType() 
							|| att.getStrategyType() == AttributeMappingStrategyType.CREATE 
							|| att.getStrategyType() == AttributeMappingStrategyType.WRITE_IF_NULL);
				}).findFirst();
				if(conflictAttributeOptional.isPresent()){
					throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_STRATEGY_CONFLICT,
							ImmutableMap.of("strategyParent", parentAttribute.getStrategyType(), "strategyConflict", conflictAttributeOptional.get().getStrategyType(), "attribute", conflictAttributeOptional.get().getName()));
				}
			}
		});
	}
	
	/**
	 * Do provisioning on given system for given entity
	 * 
	 * @param systemEntityU
	 * @param entity
	 * @param provisioningType
	 * @param attributes
	 */
	private void doProvisioning(SysSystemEntity systemEntity, ENTITY entity, ProvisioningOperationType operationType, 
			List<? extends AttributeMapping> attributes) {
		Assert.notNull(systemEntity);
		Assert.notNull(systemEntity.getUid());
		Assert.notNull(systemEntity.getEntityType());
		SysSystem system = systemEntity.getSystem();
		Assert.notNull(system);
		//
		// If are input attributes null, then we load default mapped attributes
		if (attributes == null) {
			attributes = findAttributeMappings(system, systemEntity.getEntityType());
		}
		if (attributes == null || attributes.isEmpty()) {
			return;
		}

		// Find connector identification persisted in system
		IcConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		// One IDM object can be mapped to one connector object (= one connector class).
		SysSystemMapping mapping = getMapping(system, systemEntity.getEntityType());
		if (mapping == null) {
			// mapping not found - nothing to do
			// TODO: delete operation 
			return;
		}
		//
		Map<ProvisioningAttributeDto, Object> accountAttributes = preapareMappedAttributesValues(entity, operationType,
				systemEntity, attributes);
		// public provisioning event 
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(systemEntity.getUid(), new IcObjectClassImpl(mapping.getObjectClass().getObjectClassName()), null);
		SysProvisioningOperation.Builder operationBuilder = new SysProvisioningOperation.Builder()
				.setOperationType(operationType)
				.setSystemEntity(systemEntity)
				.setEntityIdentifier(entity == null ? null : entity.getId())
				.setProvisioningContext(new ProvisioningContext(accountAttributes, connectorObject));
		provisioningExecutor.execute(operationBuilder.build());
	}

	/**
	 * Prepare all mapped attribute values (= account)
	 * @param entity
	 * @param operationType
	 * @param systemEntity
	 * @param attributes
	 * @return
	 */
	protected Map<ProvisioningAttributeDto, Object> preapareMappedAttributesValues(ENTITY entity,
			ProvisioningOperationType operationType, SysSystemEntity systemEntity,
			List<? extends AttributeMapping> attributes) {
		Map<ProvisioningAttributeDto, Object> accountAttributes = new HashMap<>();
		if (ProvisioningOperationType.DELETE != operationType) { // delete - account attributes is not needed
			
			// First we will resolve attribute without MERGE strategy
			attributes.stream().filter(attribute -> {
				return !attribute.isDisabledAttribute() 
						&& AttributeMappingStrategyType.AUTHORITATIVE_MERGE != attribute.getStrategyType() 
						&& AttributeMappingStrategyType.MERGE != attribute.getStrategyType() ;
			}).forEach(attribute -> {
				if(attribute.isUid()){
					// For UID attribute, we will set as value always UID form account
					// TODO: now we set UID from SystemEntity, may be UID from AccAccount will be more correct
					accountAttributes.put(ProvisioningAttributeDto.createProvisioningAttributeKey(attribute), systemEntity.getUid());
				}else {
					accountAttributes.put(ProvisioningAttributeDto.createProvisioningAttributeKey(attribute), getAttributeValue(entity, attribute));
				}
			});
			
			// Second we will resolve MERGE attributes
			List<? extends AttributeMapping> attributesMerge = attributes.stream().filter(attribute -> {
				return !attribute.isDisabledAttribute() 
						&& (AttributeMappingStrategyType.AUTHORITATIVE_MERGE == attribute.getStrategyType() 
						|| AttributeMappingStrategyType.MERGE == attribute.getStrategyType());
				
			}).collect(Collectors.toList());
			
			for(AttributeMapping attributeParent : attributesMerge){
				ProvisioningAttributeDto attributeParentKey = ProvisioningAttributeDto.createProvisioningAttributeKey(attributeParent);
			
				if(!attributeParent.getSchemaAttribute().isMultivalued()){
					throw new ProvisioningException(AccResultCode.PROVISIONING_MERGE_ATTRIBUTE_IS_NOT_MULTIVALUE,
							ImmutableMap.of("object", systemEntity.getUid(), "attribute", attributeParent.getSchemaAttribute().getName()));
				}
				
				List<Object> mergedValues = new ArrayList<>();
				attributes.stream().filter(attribute -> {
					return !accountAttributes.containsKey(attributeParentKey)
							&& attributeParent.getSchemaAttribute().equals(attribute.getSchemaAttribute()) 
							&& attributeParent.getStrategyType() == attribute.getStrategyType();
				}).forEach(attribute -> {
					Object value = getAttributeValue(entity, attribute);
					// We don`t want null item in list (problem with provisioning in IC)
					if(value != null){
						// If is value collection, then we add all its items to main list!
						if(value instanceof Collection){
							Collection<?> collectionNotNull = ((Collection<?>)value).stream().filter(item -> {
								return item != null;
							}).collect(Collectors.toList());
							mergedValues.addAll(collectionNotNull);
						}else {
							mergedValues.add(value);
						}
					}
				});
				if(!accountAttributes.containsKey(attributeParentKey)){
					accountAttributes.put(attributeParentKey, mergedValues);
				}
			}
		}
		return accountAttributes;
	}

	protected Object getAttributeValue(ENTITY entity, AttributeMapping attribute){
		return attributeMappingService.getAttributeValue(entity, attribute);
	}

	@Override
	public void doProvisioningForAttribute(SysSystemEntity systemEntity, AttributeMapping attributeMapping, Object value,
			ProvisioningOperationType operationType, ENTITY entity) {

		Assert.notNull(systemEntity);
		Assert.notNull(systemEntity.getSystem());
		Assert.notNull(systemEntity.getEntityType());
		Assert.notNull(systemEntity.getUid());
		Assert.notNull(attributeMapping);

		if (!attributeMapping.getSchemaAttribute().isUpdateable()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_SCHEMA_ATTRIBUTE_IS_NOT_UPDATEABLE,
					ImmutableMap.of("property", attributeMapping.getIdmPropertyName(), "uid", systemEntity.getUid()));
		}
		
		String objectClassName = attributeMapping.getSchemaAttribute().getObjectClass().getObjectClassName();
		// We do transformation to system if is attribute only constant
		Object valueTransformed = value;
		if (!attributeMapping.isEntityAttribute() && !attributeMapping.isExtendedAttribute()){
			// If is attribute handling resolve as constant, then we don't want do transformation again (was did in getAttributeValue)
		} else {
			valueTransformed = attributeMappingService.transformValueToResource(value, attributeMapping, entity);
		}
		IcAttribute icAttributeForCreate = attributeMappingService.createIcAttribute(attributeMapping.getSchemaAttribute(), valueTransformed);
		//
		// Call ic modul for update single attribute
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(systemEntity.getUid(), new IcObjectClassImpl(objectClassName), ImmutableList.of(icAttributeForCreate));		
		SysProvisioningOperation.Builder operationBuilder = new SysProvisioningOperation.Builder()
				.setOperationType(ProvisioningEventType.UPDATE)
				.setSystemEntity(systemEntity)
				.setEntityIdentifier(entity == null ? null : entity.getId())
				.setProvisioningContext(new ProvisioningContext(connectorObject));						
		provisioningExecutor.execute(operationBuilder.build());
	}

	@Override
	public IcUidAttribute authenticate(String username, GuardedString password, SysSystem system, SystemEntityType entityType) {

		Assert.notNull(username);
		Assert.notNull(system);
		Assert.notNull(entityType);

		// Find connector configuration persisted in system
		IcConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Call IC module for check authenticate
		return connectorFacade.authenticateObject(system.getConnectorInstance(), connectorConfig, null, username, password);
	}

	/**
	 * Return all mapped attributes for this account (include overloaded attributes)
	 * 
	 * @param uid
	 * @param account
	 * @param entity
	 * @param system
	 * @param entityType
	 * @return
	 */
	@Override
	public List<AttributeMapping> resolveMappedAttributes(String uid, AccAccount account, ENTITY entity, SysSystem system, SystemEntityType entityType) {
		EntityAccountFilter filter = this.createEntityAccountFilter();
		filter.setEntityId(entity.getId());
		filter.setSystemId(system.getId());
		filter.setOwnership(Boolean.TRUE);
		filter.setAccountId(account.getId());
		
		@SuppressWarnings("unchecked")
		List<? extends EntityAccountDto> entityAccoutnList = this.getEntityAccountService().findDto((BaseFilter)filter, null).getContent();
		if (entityAccoutnList == null) {
			return null;
		}
		// All identity account with flag ownership on true

		// All role system attributes (overloading) for this uid and same system
		List<SysRoleSystemAttribute> roleSystemAttributesAll = findOverloadingAttributes(uid, entity, system, entityAccoutnList, entityType);

		// All default mapped attributes from system
		List<? extends AttributeMapping> defaultAttributes = findAttributeMappings(system, entityType);

		// Final list of attributes use for provisioning
		return compileAttributes(defaultAttributes, roleSystemAttributesAll, entityType);
	}
	/**
	 * Create final list of attributes for provisioning.
	 * 
	 * @param identityAccount
	 * @param defaultAttributes
	 * @param overloadingAttributes
	 * @return
	 */
	@Override
	public List<AttributeMapping> compileAttributes(List<? extends AttributeMapping> defaultAttributes,
			List<SysRoleSystemAttribute> overloadingAttributes, SystemEntityType entityType) {
		Assert.notNull(overloadingAttributes, "List of overloading attributes cannot be null!");
		
		List<AttributeMapping> finalAttributes = new ArrayList<>();
		if (defaultAttributes == null) {
			return null;
		}
		defaultAttributes.stream().forEach(defaultAttribute -> {
			for(AttributeMappingStrategyType strategy : AttributeMappingStrategyType.values()){
				finalAttributes.addAll(compileAtributeForStrategy(strategy, defaultAttribute, overloadingAttributes));
			}
		});
		
		// Validate attributes on incompatible strategies
		validateAttributesStrategy(finalAttributes);
		
		return finalAttributes;
	}

	/**
	 * Compile given attribute for strategy
	 * @param strategy
	 * @param defaultAttribute
	 * @param overloadingAttributes
	 * @return
	 */
	protected List<AttributeMapping> compileAtributeForStrategy(AttributeMappingStrategyType strategy,
			AttributeMapping defaultAttribute, List<SysRoleSystemAttribute> overloadingAttributes) {

		List<AttributeMapping> finalAttributes = new ArrayList<>();

		List<SysRoleSystemAttribute> attributesOrdered = overloadingAttributes.stream().filter(roleSystemAttribute -> {
			// Search attribute override same schema attribute
			SysSystemAttributeMapping attributeMapping = roleSystemAttribute.getSystemAttributeMapping();
			return attributeMapping.equals(defaultAttribute);
		}).sorted((att1, att2) -> {
			// Sort attributes by role priority
			return Integer.valueOf(att2.getRoleSystem().getRole().getPriority())
					.compareTo(new Integer(att1.getRoleSystem().getRole().getPriority()));
		}).collect(Collectors.toList());

		// We have some overloaded attributes
		if (!attributesOrdered.isEmpty()) {
			List<SysRoleSystemAttribute> attributesOrderedGivenStrategy = attributesOrdered.stream()
					.filter(attribute -> {
						return strategy == attribute.getStrategyType();
					}).collect(Collectors.toList());

			// We do not have overloaded attributes for given strategy
			if (attributesOrderedGivenStrategy.isEmpty()) {
				return finalAttributes;
			}

			// First element have role with max priority
			int maxPriority = attributesOrderedGivenStrategy.get(0).getRoleSystem().getRole().getPriority();

			// We will search for attribute with highest priority (and role name)
			Optional<SysRoleSystemAttribute> highestPriorityAttributeOptional = attributesOrderedGivenStrategy.stream()
					.filter(attribute -> {
						// Filter attributes by max priority
						return maxPriority == attribute.getRoleSystem().getRole().getPriority();
					}).sorted((att1, att2) -> {
						// Second filtering, if we have same priority, then
						// we
						// will sort by role name
						return att2.getRoleSystem().getRole().getName()
								.compareTo(att1.getRoleSystem().getRole().getName());
					}).findFirst();

			if (highestPriorityAttributeOptional.isPresent()) {
				SysRoleSystemAttribute highestPriorityAttribute = highestPriorityAttributeOptional.get();

				// For merge strategies, will be add to final list all
				// overloaded attributes
				if (strategy == AttributeMappingStrategyType.AUTHORITATIVE_MERGE
						|| strategy == AttributeMappingStrategyType.MERGE) {
					attributesOrderedGivenStrategy.forEach(attribute -> {
						// Disabled attribute will be skipped
						if (!attribute.isDisabledDefaultAttribute()) {
							// We can't use instance of SysSysteAttributeMapping and set
							// up overloaded value (it is entity).
							// We have to create own dto and set up all values
							// (overloaded and default)
							AttributeMapping overloadedAttribute = new MappingAttributeDto();
							// Default values (values from schema attribute handling)
							overloadedAttribute.setSchemaAttribute(defaultAttribute.getSchemaAttribute());
							overloadedAttribute
									.setTransformFromResourceScript(defaultAttribute.getTransformFromResourceScript());
							// Overloaded values
							roleSystemAttributeService.fillOverloadedAttribute(attribute, overloadedAttribute);

							// Common properties (for MERGE strategy) will be
							// set from MERGE attribute with highest priority
							overloadedAttribute.setSendAlways(highestPriorityAttribute.isSendAlways());
							overloadedAttribute.setSendOnlyIfNotNull(highestPriorityAttribute.isSendOnlyIfNotNull());

							// Add modified attribute to final list
							finalAttributes.add(overloadedAttribute);
						}
					});
					return finalAttributes;
				}

				// We will search for disabled overloaded attribute
				Optional<SysRoleSystemAttribute> disabledOverloadedAttOptional = attributesOrderedGivenStrategy.stream()
						.filter(attribute -> {
							// Filter attributes by max priority
							return maxPriority == attribute.getRoleSystem().getRole().getPriority();
						}).filter(attribute -> {
							// Second filtering, we will search for disabled
							// overloaded attribute
							return attribute.isDisabledDefaultAttribute();
						}).findFirst();
				if (disabledOverloadedAttOptional.isPresent()) {
					// We found disabled overloaded attribute with highest priority
					return finalAttributes;
				}

				// None overloaded attribute are disabled, we will search for
				// attribute with highest priority (and role name)
				// Disabled attribute will be skipped
				if (!highestPriorityAttribute.isDisabledDefaultAttribute()) {
					// We can't use instance of SysSysteAttributeMapping and set
					// up overloaded value (it is entity).
					// We have to create own dto and set up all values
					// (overloaded and default)
					AttributeMapping overloadedAttribute = new MappingAttributeDto();
					// Default values (values from schema attribute handling)
					overloadedAttribute.setSchemaAttribute(defaultAttribute.getSchemaAttribute());
					overloadedAttribute
							.setTransformFromResourceScript(defaultAttribute.getTransformFromResourceScript());
					// Overloaded values
					roleSystemAttributeService.fillOverloadedAttribute(highestPriorityAttribute, overloadedAttribute);
					// Add modified attribute to final list
					finalAttributes.add(overloadedAttribute);
					return finalAttributes;
				}
			}
		}
		// We don't have overloading attribute, we will use default
		// if has given strategy
		// If is default attribute disabled, then we don't use him

		if (!defaultAttribute.isDisabledAttribute() && strategy == defaultAttribute.getStrategyType()) {
			finalAttributes.add(defaultAttribute);
		}

		return finalAttributes;
	}

	/**
	 * Return list of all overloading attributes for given identity, system and
	 * uid
	 * 
	 * @param identityAccount
	 * @param uid
	 * @param idenityAccoutnList
	 * @param operationType
	 * @param entityType
	 * @return
	 */
	protected abstract List<SysRoleSystemAttribute> findOverloadingAttributes(String uid, ENTITY entity, SysSystem system,
			List<? extends EntityAccountDto> idenityAccoutnList, SystemEntityType entityType);
	
	
	private SysSystemMapping getMapping(SysSystem system, SystemEntityType entityType) {
		List<SysSystemMapping> systemMappings = systemMappingService.findBySystem(system, SystemOperationType.PROVISIONING, entityType);
		if (systemMappings == null || systemMappings.isEmpty()) {
			LOG.info(MessageFormat.format("System [{0}] does not have mapping, provisioning will not be executed. Add some mapping for entity type [{1}]", system.getName(), entityType));
			return null;
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
	protected List<? extends AttributeMapping> findAttributeMappings(SysSystem system, SystemEntityType entityType) {
		SysSystemMapping mapping = getMapping(system, entityType);
		if (mapping == null) {
			return null;
		}
		return attributeMappingService.findBySystemMapping(mapping);
	}

	protected List<SysSystemMapping> findSystemMappingsForEntityType(ENTITY entity, SystemEntityType entityType) {
		SystemMappingFilter mappingFilter = new SystemMappingFilter();
		mappingFilter.setEntityType(entityType);
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		List<SysSystemMapping> systemMappings = systemMappingService.find(mappingFilter, null).getContent();
		return systemMappings;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Create AccAccount and relation between account and entity
	 * @param uid
	 * @param entityId
	 * @param systemId
	 * @return Id of new EntityAccount
	 */
	protected UUID createEntityAccount(String uid, UUID entityId, UUID systemId){
		AccAccount account = new AccAccount();
		account.setSystem(systemService.get(systemId));
		account.setAccountType(AccountType.PERSONAL);
		account.setUid(uid);
		accountService.save(account);
		// Create new entity account relation
		EntityAccountDto entityAccount = this.createEntityAccountDto();
		entityAccount.setAccount(account.getId());
		entityAccount.setEntity(entityId);
		entityAccount.setOwnership(true);
		entityAccount = (EntityAccountDto) getEntityAccountService().save(entityAccount);
		return (UUID) entityAccount.getId();
	}
	
	protected UUID getAccountByEntity(UUID entityId, UUID systemId) {
		EntityAccountFilter entityAccountFilter = createEntityAccountFilter();
		entityAccountFilter.setEntityId(entityId);
		entityAccountFilter.setSystemId(systemId);
		entityAccountFilter.setOwnership(Boolean.TRUE);
		@SuppressWarnings("unchecked")
		List<EntityAccountDto> entityAccounts = this.getEntityAccountService()
				.findDto((BaseFilter) entityAccountFilter, null).getContent();
		if (entityAccounts.isEmpty()) {
			return null;
		} else {
			// We assume that all entity accounts
			// (mark as
			// ownership) have same account!
			return entityAccounts.get(0).getEntity();
		}
	}
	
	protected abstract EntityAccountFilter createEntityAccountFilter();

	protected abstract EntityAccountDto createEntityAccountDto();

	@SuppressWarnings("rawtypes")
	protected abstract ReadWriteDtoService getEntityAccountService();

	@SuppressWarnings("rawtypes")
	protected abstract ReadWriteDtoService getEntityService();
}
