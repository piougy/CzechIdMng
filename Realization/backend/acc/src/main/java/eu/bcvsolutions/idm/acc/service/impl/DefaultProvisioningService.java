package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.MappingAttributeDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
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
 * Service for do provisioning or synchronisation or reconciliation
 * 
 * @author svandav
 *
 */
@Service
public class DefaultProvisioningService implements ProvisioningService {

	public static final String PASSWORD_SCHEMA_PROPERTY_NAME = "__PASSWORD__";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultProvisioningService.class);
	private final SysSystemMappingService systemMappingService;
	private final SysSystemAttributeMappingService attributeMappingService;
	private final IcConnectorFacade connectorFacade;
	private final SysSystemService systemService;
	private final AccIdentityAccountRepository identityAccountRepository;
	private final SysRoleSystemService roleSystemService;
	private final AccAccountManagementService accountManagementService;
	private final SysRoleSystemAttributeService roleSystemAttributeService;
	private final SysSystemEntityService systemEntityService;
	private final AccAccountService accountService;
	private final ProvisioningExecutor provisioningExecutor;

	@Autowired
	public DefaultProvisioningService(SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			SysRoleSystemService roleSystemService, AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService, AccIdentityAccountRepository identityAccountRepository,
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
		Assert.notNull(identityAccountRepository);
		Assert.notNull(provisioningExecutor);
		//
		this.systemMappingService = systemMappingService;
		this.attributeMappingService = attributeMappingService;
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.roleSystemService = roleSystemService;
		this.accountManagementService = accountManagementService;
		this.roleSystemAttributeService = roleSystemAttributeService;
		this.systemEntityService = systemEntityService;
		this.accountService = accountService;
		this.identityAccountRepository = identityAccountRepository;
		this.provisioningExecutor = provisioningExecutor;
	}

	@Override
	public void doProvisioning(IdmIdentity identity) {
		Assert.notNull(identity);
		//
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountRepository.find(filter, null);
		List<AccIdentityAccount> idenityAccoutnList = identityAccounts.getContent();
		if (idenityAccoutnList == null) {
			return;
		}

		List<AccAccount> accounts = new ArrayList<>();
		idenityAccoutnList.stream().filter(ia -> {
			return ia.isOwnership();
		}).forEach((identityAccount) -> {
			if (!accounts.contains(identityAccount.getAccount())) {
				accounts.add(identityAccount.getAccount());
			}
		});

		accounts.stream().forEach(account -> {
			this.doProvisioning(account, identity);
		});
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

	@Override
	public void doProvisioning(AccAccount account) {
		Assert.notNull(account);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setAccountId(account.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountRepository.find(filter, null);
		List<AccIdentityAccount> idenittyAccoutnList = identityAccounts.getContent();
		if (idenittyAccoutnList == null) {
			return;
		}
		idenittyAccoutnList.stream().filter(identityAccount -> {
			return identityAccount.isOwnership();
		}).forEach((identityAccount) -> {
			doProvisioning(account, identityAccount.getIdentity());
		});
	}

	@Override
	public void doProvisioning(AccAccount account, IdmIdentity identity) {
		Assert.notNull(account);
		Assert.notNull(identity);
		//
		ProvisioningOperationType operationType;
		SysSystem system = account.getSystem();
		SysSystemEntity systemEntity = getSystemEntity(account);
		if (systemEntity == null) {
			// prepare system entity - uid could be changed by provisioning, but we need to link her with account
			// First we try find system entity with same uid. 
			systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, account.getUid());
			if (systemEntity == null) {
				systemEntity = new SysSystemEntity();
				systemEntity.setEntityType(SystemEntityType.IDENTITY);
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
		
		List<AttributeMapping> finalAttributes = resolveMappedAttributes(account.getUid(), account, identity, system, systemEntity.getEntityType());
		if(CollectionUtils.isEmpty(finalAttributes)){
			// nothing to do - mapping is empty
			return;
		}
		
		doProvisioning(systemEntity, identity, operationType, finalAttributes);		
	}

	/**
	 * Validate attributes on incompatible strategies
	 * @param finalAttributes
	 */
	private void validateAttributesStrategy(List<AttributeMapping> finalAttributes) {
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
	private void doProvisioning(SysSystemEntity systemEntity, AbstractEntity entity, ProvisioningOperationType operationType, 
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
		// prepare all mapped attribute values (= account)
		Map<ProvisioningAttributeDto, Object> accountAttributes = new HashMap<>();
		if (ProvisioningOperationType.DELETE != operationType) { // delete - account attributes is not needed
			
			// First we will resolve attribute without MERGE strategy
			attributes.stream().filter(attribute -> {
				return !attribute.isDisabledAttribute() 
						&& AttributeMappingStrategyType.AUTHORITATIVE_MERGE != attribute.getStrategyType() 
						&& AttributeMappingStrategyType.MERGE != attribute.getStrategyType() ;
			}).forEach(attribute -> {
				accountAttributes.put(ProvisioningAttributeDto.createProvisioningAttributeKey(attribute), attributeMappingService.getAttributeValue(entity, attribute));
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
					Object value = attributeMappingService.getAttributeValue(entity, attribute);
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
		// public provisioning event 
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(systemEntity.getUid(), new IcObjectClassImpl(mapping.getObjectClass().getObjectClassName()), null);
		SysProvisioningOperation.Builder operationBuilder = new SysProvisioningOperation.Builder()
				.setOperationType(operationType)
				.setSystemEntity(systemEntity)
				.setEntityIdentifier(entity == null ? null : entity.getId())
				.setProvisioningContext(new ProvisioningContext(accountAttributes, connectorObject));
		provisioningExecutor.execute(operationBuilder.build());
	}
	
	@Override
	public void fillOverloadedAttribute(SysRoleSystemAttribute overloadingAttribute,
			AttributeMapping overloadedAttribute) {
		overloadedAttribute.setName(overloadingAttribute.getName());
		overloadedAttribute.setEntityAttribute(overloadingAttribute.isEntityAttribute());
		overloadedAttribute.setConfidentialAttribute(overloadingAttribute.isConfidentialAttribute());
		overloadedAttribute.setExtendedAttribute(overloadingAttribute.isExtendedAttribute());
		overloadedAttribute.setIdmPropertyName(overloadingAttribute.getIdmPropertyName());
		overloadedAttribute.setTransformToResourceScript(overloadingAttribute.getTransformScript());
		overloadedAttribute.setUid(overloadingAttribute.isUid());
		overloadedAttribute.setDisabledAttribute(overloadingAttribute.isDisabledDefaultAttribute());
		overloadedAttribute.setStrategyType(overloadingAttribute.getStrategyType());
		overloadedAttribute.setSendAlways(overloadingAttribute.isSendAlways());
		overloadedAttribute.setSendOnlyIfNotNull(overloadingAttribute.isSendOnlyIfNotNull());
	}

	@Override
	public void changePassword(IdmIdentity identity, PasswordChangeDto passwordChange) {
		Assert.notNull(identity);
		Assert.notNull(passwordChange);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountRepository.find(filter, null);
		List<AccIdentityAccount> identityAccountList = identityAccounts.getContent();
		if (identityAccountList == null) {
			return;
		}
		
		// Distinct by accounts
		List<AccAccount> accounts = new ArrayList<>();
		identityAccountList.stream().filter(identityAccount -> {
			return identityAccount.isOwnership() && (passwordChange.isAll()
					|| passwordChange.getAccounts().contains(identityAccount.getId().toString()));
		}).forEach(identityAccount -> {
			if (!accounts.contains(identityAccount.getAccount())) {
				accounts.add(identityAccount.getAccount());
			}
		});

		accounts.forEach(account -> {
			// find uid from system entity or from account
			String uid = account.getUid();
			SysSystem system = account.getSystem();
			SysSystemEntity systemEntity = account.getSystemEntity();
			//
			// Find mapped attributes (include overloaded attributes)
			List<AttributeMapping> finalAttributes = resolveMappedAttributes(uid, account, identity, system, systemEntity.getEntityType());
			if (CollectionUtils.isEmpty(finalAttributes)) {
				return;
			}
			
			// We try find __PASSWORD__ attribute in mapped attributes
			Optional<? extends AttributeMapping> attriubuteHandlingOptional = finalAttributes.stream()
					.filter((attribute) -> {
						return PASSWORD_SCHEMA_PROPERTY_NAME.equals(attribute.getSchemaAttribute().getName());
					}).findFirst();
			if (!attriubuteHandlingOptional.isPresent()) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_PASSWORD_FIELD_NOT_FOUND,
						ImmutableMap.of("uid", uid));
			}
			AttributeMapping mappedAttribute = attriubuteHandlingOptional.get();

			doProvisioningForAttribute(systemEntity, mappedAttribute, passwordChange.getNewPassword(),
					ProvisioningOperationType.UPDATE, identity);
		});
	}

	@Override
	public void doProvisioningForAttribute(SysSystemEntity systemEntity, AttributeMapping attributeMapping, Object value,
			ProvisioningOperationType operationType, AbstractEntity entity) {

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
	 * @param identity
	 * @param system
	 * @param entityType
	 * @return
	 */
	@Override
	public List<AttributeMapping> resolveMappedAttributes(String uid, AccAccount account, IdmIdentity identity, SysSystem system, SystemEntityType entityType) {
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		filter.setSystemId(system.getId());
		filter.setOwnership(Boolean.TRUE);
		filter.setAccountId(account.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountRepository.find(filter, null);
		List<AccIdentityAccount> idenityAccoutnList = identityAccounts.getContent();
		if (idenityAccoutnList == null) {
			return null;
		}
		// All identity account with flag ownership on true

		// All role system attributes (overloading) for this uid and same system
		List<SysRoleSystemAttribute> roleSystemAttributesAll = findOverloadingAttributes(uid, identity, system, idenityAccoutnList, entityType);

		// All default mapped attributes from system
		List<? extends AttributeMapping> defaultAttributes = findAttributeMappings(system, entityType);

		// Final list of attributes use for provisioning
		return compileAttributes(defaultAttributes, roleSystemAttributesAll);
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
			List<SysRoleSystemAttribute> overloadingAttributes) {
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
	private List<AttributeMapping> compileAtributeForStrategy(AttributeMappingStrategyType strategy,
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
							fillOverloadedAttribute(attribute, overloadedAttribute);

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
					fillOverloadedAttribute(highestPriorityAttribute, overloadedAttribute);
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
	private List<SysRoleSystemAttribute> findOverloadingAttributes(String uid, IdmIdentity identity, SysSystem system,
			List<AccIdentityAccount> idenityAccoutnList, SystemEntityType entityType) {

		List<SysRoleSystemAttribute> roleSystemAttributesAll = new ArrayList<>();

		idenityAccoutnList.stream().filter(ia -> {
			return ia.getIdentityRole() != null && ia.getAccount().getSystem() != null
					&& ia.getAccount().getSystem().equals(system) 
					&& ia.isOwnership();
		}).forEach((identityAccountInner) -> {
			// All identity account with same system and with filled
			// identityRole

			IdmIdentityRole identityRole = identityAccountInner.getIdentityRole();
			RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
			roleSystemFilter.setRoleId(identityRole.getRole().getId());
			roleSystemFilter.setSystemId(identityAccountInner.getAccount().getSystem().getId());
			List<SysRoleSystem> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();

			List<SysRoleSystem> roleSystemsForSameAccount = roleSystems.stream().filter(roleSystem -> {
				String roleSystemUID = accountManagementService.generateUID(identity, roleSystem);

				SysSystemMapping systemMapping = roleSystem.getSystemMapping();
				return (SystemOperationType.PROVISIONING == systemMapping.getOperationType()
						&& entityType == systemMapping.getEntityType() && roleSystemUID.equals(uid));
			}).collect(Collectors.toList());

			if (roleSystemsForSameAccount.size() > 1) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_DUPLICATE_ROLE_MAPPING,
						ImmutableMap.of("role", roleSystemsForSameAccount.get(0).getRole().getName(), "system",
								roleSystemsForSameAccount.get(0).getSystem().getName(), "entityType", entityType));
			}
			if (!roleSystemsForSameAccount.isEmpty()) {
				RoleSystemAttributeFilter roleSystemAttributeFilter = new RoleSystemAttributeFilter();
				roleSystemAttributeFilter.setRoleSystemId(roleSystemsForSameAccount.get(0).getId());
				List<SysRoleSystemAttribute> roleAttributes = roleSystemAttributeService
						.find(roleSystemAttributeFilter, null).getContent();

				if (!CollectionUtils.isEmpty(roleAttributes)) {
					roleSystemAttributesAll.addAll(roleAttributes);
				}
			}

		});

		return roleSystemAttributesAll;
	}
	
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
	private List<? extends AttributeMapping> findAttributeMappings(SysSystem system, SystemEntityType entityType) {
		SysSystemMapping mapping = getMapping(system, entityType);
		if (mapping == null) {
			return null;
		}
		return attributeMappingService.findBySystemMapping(mapping);
	}
}
