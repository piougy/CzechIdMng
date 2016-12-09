package eu.bcvsolutions.idm.acc.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountOperationType;
import eu.bcvsolutions.idm.acc.domain.MappingAttribute;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.MappingAttributeDto;
import eu.bcvsolutions.idm.acc.dto.RoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeHandlingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityHandlingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfConnectorObject;
import eu.bcvsolutions.idm.icf.api.IcfObjectClass;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.icf.impl.IcfAttributeImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorObjectImpl;
import eu.bcvsolutions.idm.icf.impl.IcfObjectClassImpl;
import eu.bcvsolutions.idm.icf.impl.IcfPasswordAttributeImpl;
import eu.bcvsolutions.idm.icf.impl.IcfUidAttributeImpl;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorFacade;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Service for do provisioning or synchronisation or reconciliation
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysProvisioningService implements SysProvisioningService {

	public static final String PASSWORD_IDM_PROPERTY_NAME = "password";
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSysProvisioningService.class);
	private final SysSystemEntityHandlingService entityHandlingService;
	private final SysSchemaAttributeHandlingService attributeHandlingService;
	private final IcfConnectorFacade connectorFacade;
	private final SysSystemService systemService;
	private AccIdentityAccountService identityAccountService;
	private final ConfidentialStorage confidentialStorage;
	private final FormService formService;
	private final SysRoleSystemService roleSystemService;
	private final AccAccountManagementService accountManagementService;
	private final SysRoleSystemAttributeService roleSystemAttributeService;
	private final SysSystemEntityService systemEntityService;
	private final AccAccountService accountService;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	public DefaultSysProvisioningService(SysSystemEntityHandlingService entityHandlingService,
			SysSchemaAttributeHandlingService attributeHandlingService, IcfConnectorFacade connectorFacade,
			SysSystemService systemService, ConfidentialStorage confidentialStorage, FormService formService,
			SysRoleSystemService roleSystemService, AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService) {

		Assert.notNull(entityHandlingService);
		Assert.notNull(attributeHandlingService);
		Assert.notNull(connectorFacade);
		Assert.notNull(systemService);
		Assert.notNull(confidentialStorage);
		Assert.notNull(formService);
		Assert.notNull(roleSystemService);
		Assert.notNull(accountManagementService);
		Assert.notNull(roleSystemAttributeService);
		Assert.notNull(systemEntityService);
		Assert.notNull(accountService);
		//
		this.entityHandlingService = entityHandlingService;
		this.attributeHandlingService = attributeHandlingService;
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.confidentialStorage = confidentialStorage;
		this.formService = formService;
		this.roleSystemService = roleSystemService;
		this.accountManagementService = accountManagementService;
		this.roleSystemAttributeService = roleSystemAttributeService;
		this.systemEntityService = systemEntityService;
		this.accountService = accountService;
	}

	@Override
	public void doProvisioning(IdmIdentity identity) {
		Assert.notNull(identity);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		Page<AccIdentityAccount> identityAccounts = getIdentityAccountService().find(filter, null);
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
		SysSystem system = account.getSystem();
		String uid = account.getUid();

		SysSystemEntity systemEntity = account.getSystemEntity();
		if (systemEntity != null && account.getUid() != null) {
			uid = systemEntity.getUid();
		}

		doOperation(uid, null, AccountOperationType.DELETE, SystemOperationType.PROVISIONING,
				SystemEntityType.IDENTITY, system, null);
	}

	@Override
	public void doProvisioning(AccAccount account) {
		Assert.notNull(account);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setAccountId(account.getId());
		Page<AccIdentityAccount> identityAccounts = getIdentityAccountService().find(filter, null);
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
		
		SysSystem system = account.getSystem();
		String uid = account.getUid();

		SysSystemEntity systemEntity = account.getSystemEntity();
		if (systemEntity != null && account.getUid() != null) {
			uid = systemEntity.getUid();
		}

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		filter.setSystemId(system.getId());
		filter.setOwnership(Boolean.TRUE);
		filter.setAccountId(account.getId());
		Page<AccIdentityAccount> identityAccounts = getIdentityAccountService().find(filter, null);
		List<AccIdentityAccount> idenityAccoutnList = identityAccounts.getContent();
		if (idenityAccoutnList == null) {
			return;
		}

		SystemOperationType operationType = SystemOperationType.PROVISIONING;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		// All identity account with flag ownership on true

		// All role system attributes (overloading) for this uid and same system
		List<SysRoleSystemAttribute> roleSystemAttributesAll = findOverloadingAttributes(uid, identity, system,
				idenityAccoutnList, operationType, entityType);

		// All default mapped attributes from system
		List<? extends MappingAttribute> defaultAttributes = findAttributesHandling(operationType, entityType, system);

		// Final list of attributes use for provisioning
		List<MappingAttribute> finalAttributes = compileAttributes(defaultAttributes, roleSystemAttributesAll);

		String uidFromOperation = doOperation(uid, identity, AccountOperationType.UPDATE, operationType, entityType, system,
				finalAttributes);
		

		if (systemEntity == null) {
			systemEntity = new SysSystemEntity();
			systemEntity.setEntityType(SystemEntityType.IDENTITY);
			systemEntity.setSystem(system);
		}
		account.setSystemEntity(systemEntity);
		systemEntity.setUid(uidFromOperation);
		systemEntityService.save(systemEntity);
		accountService.save(account);

	}

	/**
	 * Create final list of attributes for provisioning.
	 * 
	 * @param identityAccount
	 * @param defaultAttributes
	 * @param overloadingAttributes
	 * @return
	 */
	private List<MappingAttribute> compileAttributes(List<? extends MappingAttribute> defaultAttributes,
			List<SysRoleSystemAttribute> overloadingAttributes) {
		List<MappingAttribute> finalAttributes = new ArrayList<>();
		if (defaultAttributes == null) {
			return null;
		}
		defaultAttributes.stream().forEach(defaultAttribute -> {

			Optional<SysRoleSystemAttribute> overloadingAttributeOptional = overloadingAttributes.stream()
					.filter(roleSystemAttribute -> {
						// Search attribute override same schema attribute
						return roleSystemAttribute.getSchemaAttributeHandling().equals(defaultAttribute);
					}).sorted((att1, att2) -> {
						// Sort attributes by role name
						return att2.getRoleSystem().getRole().getName()
								.compareTo(att1.getRoleSystem().getRole().getName());
					}).findFirst();

			if (overloadingAttributeOptional.isPresent()) {
				SysRoleSystemAttribute overloadingAttribute = overloadingAttributeOptional.get();
				// Disabled attribute will be skipped
				if (!overloadingAttribute.isDisabledDefaultAttribute()) {
					// We can't use instance of SchemaAttributeHandling and set
					// up overloaded value (it is entity).
					// We have to create own dto and set up all values
					// (overloaded and default)
					MappingAttribute overloadedAttribute = new MappingAttributeDto();
					// Default values (values from schema attribute
					// handling)
					overloadedAttribute.setSchemaAttribute(defaultAttribute.getSchemaAttribute());
					overloadedAttribute
							.setTransformFromResourceScript(defaultAttribute.getTransformFromResourceScript());
					// Overloaded values
					fillOverloadedAttribute(overloadingAttribute, overloadedAttribute);
					// Add modified attribute to final list
					finalAttributes.add(overloadedAttribute);
				}
			} else {
				// We don't have overloading attribute, we will use default
				// If is default attribute disabled, then we don't use him
				if (!defaultAttribute.isDisabledAttribute()) {
					finalAttributes.add(defaultAttribute);
				}
			}

		});
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
			List<AccIdentityAccount> idenityAccoutnList, SystemOperationType operationType,
			SystemEntityType entityType) {

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

				SysSystemEntityHandling entityHandling = roleSystem.getSystemEntityHandling();
				return (operationType == entityHandling.getOperationType()
						&& entityType == entityHandling.getEntityType() && roleSystemUID.equals(uid));
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

	private void fillOverloadedAttribute(SysRoleSystemAttribute overloadingAttribute,
			MappingAttribute overloadedAttribute) {
		overloadedAttribute.setName(overloadingAttribute.getName());
		overloadedAttribute.setEntityAttribute(overloadingAttribute.isEntityAttribute());
		overloadedAttribute.setConfidentialAttribute(overloadingAttribute.isConfidentialAttribute());
		overloadedAttribute.setExtendedAttribute(overloadingAttribute.isExtendedAttribute());
		overloadedAttribute.setIdmPropertyName(overloadingAttribute.getIdmPropertyName());
		overloadedAttribute.setTransformToResourceScript(overloadingAttribute.getTransformScript());
		overloadedAttribute.setUid(overloadingAttribute.isUid());
		overloadedAttribute.setDisabledAttribute(overloadingAttribute.isDisabledDefaultAttribute());
	}

	@Override
	public void changePassword(IdmIdentity identity, PasswordChangeDto passwordChange) {
		Assert.notNull(identity);
		Assert.notNull(passwordChange);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		Page<AccIdentityAccount> identityAccounts = getIdentityAccountService().find(filter, null);
		List<AccIdentityAccount> identityAccountList = identityAccounts.getContent();
		if (identityAccountList == null) {
			return;
		}
		// TODO: ? add into IdentityAccountFilter: accountId IN (..., ...);
		identityAccountList.stream().filter(identityAccount -> {
			return passwordChange.getAccounts().contains(identityAccount.getId().toString());
		}).forEach(identityAccount -> {
			doProvisioningForAttribute(identityAccount.getAccount().getUid(), PASSWORD_IDM_PROPERTY_NAME,
					passwordChange.getNewPassword(), identityAccount.getAccount().getSystem(),
					AccountOperationType.UPDATE, SystemEntityType.IDENTITY, identity);
		});
	}

	@Override
	public void doProvisioningForAttribute(String uid, String idmPropertyName, Object value, SysSystem system,
			AccountOperationType operationType, SystemEntityType entityType, AbstractEntity entity) {

		Assert.notNull(uid);
		Assert.notNull(system);
		Assert.notNull(entityType);

		List<? extends MappingAttribute> attributes = findAttributesHandling(SystemOperationType.PROVISIONING,
				entityType, system);
		if (attributes == null || attributes.isEmpty()) {
			return;
		}

		// Find connector identification persisted in system
		IcfConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcfConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		IcfUidAttribute uidAttribute = new IcfUidAttributeImpl(null, uid, null);

		Optional<? extends MappingAttribute> attriubuteHandlingOptional = attributes.stream().filter((attribute) -> {
			return idmPropertyName.equals(attribute.getIdmPropertyName());
		}).findFirst();
		if (!attriubuteHandlingOptional.isPresent()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
					ImmutableMap.of("property", idmPropertyName, "uid", uid));
		}
		MappingAttribute attributeHandling = attriubuteHandlingOptional.get();
		if (!attributeHandling.getSchemaAttribute().isUpdateable()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_SCHEMA_ATTRIBUTE_IS_NOT_UPDATEABLE,
					ImmutableMap.of("property", idmPropertyName, "uid", uid));
		}

		String objectClassName = attributeHandling.getSchemaAttribute().getObjectClass().getObjectClassName();
		IcfAttribute icfAttributeForCreate = createIcfAttribute(attributeHandling, value, entity);
		IcfObjectClass icfObjectClass = new IcfObjectClassImpl(objectClassName);
		// Call icf modul for update single attribute
		connectorFacade.updateObject(connectorKey, connectorConfig, icfObjectClass, uidAttribute,
				ImmutableList.of(icfAttributeForCreate));

	}

	@Override
	public IcfUidAttribute authenticate(AccIdentityAccount identityAccount, SysSystem system) {
		GuardedString password = confidentialStorage.getGuardedString(identityAccount.getIdentity(),
				IdmIdentityService.PASSWORD_CONFIDENTIAL_PROPERTY);
		if (password == null) {
			password = new GuardedString(); // TODO: empty password or null?
		}
		return authenticate(identityAccount.getAccount().getUid(), password, system, SystemOperationType.PROVISIONING,
				SystemEntityType.IDENTITY);
	}

	@Override
	public IcfUidAttribute authenticate(String username, GuardedString password, SysSystem system,
			SystemOperationType operationType, SystemEntityType entityType) {

		Assert.notNull(username);
		Assert.notNull(system);
		Assert.notNull(entityType);
		Assert.notNull(operationType);

		List<? extends MappingAttribute> attributes = findAttributesHandling(operationType, entityType, system);
		if (attributes == null || attributes.isEmpty()) {
			return null;
		}

		// Find connector identification persisted in system
		IcfConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcfConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		// Find attribute handling mapped on schema password attribute
		Optional<? extends MappingAttribute> passwordAttributeHandlingOptional = attributes.stream()
				.filter((attribute) -> {
					return IcfConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equals(attribute.getSchemaAttribute().getName());
				}).findFirst();
		if (!passwordAttributeHandlingOptional.isPresent()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
					ImmutableMap.of("property", IcfConnectorFacade.PASSWORD_ATTRIBUTE_NAME, "uid", username));
		}

		MappingAttribute passwordAttributeHandling = passwordAttributeHandlingOptional.get();

		String objectClassName = passwordAttributeHandling.getSchemaAttribute().getObjectClass().getObjectClassName();
		IcfObjectClass icfObjectClass = new IcfObjectClassImpl(objectClassName);

		// Call ICF module for check authenticate
		return connectorFacade.authenticateObject(connectorKey, connectorConfig, icfObjectClass, username, password);
	}

	/**
	 * Do provisioning/synchronisation/reconciliation on given system for given
	 * entity
	 * 
	 * @param uid
	 * @param entity
	 * @param provisioningType
	 * @param entityType
	 * @param system
	 */
	public String doOperation(String uid, AbstractEntity entity, AccountOperationType operationType,
			SystemOperationType provisioningType, SystemEntityType entityType, SysSystem system,
			List<? extends MappingAttribute> attributes) {
		Assert.notNull(uid);
		Assert.notNull(provisioningType);
		Assert.notNull(system);
		Assert.notNull(entityType);

		// If are input attributes null, then we load default mapped attributes
		if (attributes == null) {
			attributes = findAttributesHandling(provisioningType, entityType, system);
		}
		if (attributes == null || attributes.isEmpty()) {
			return uid;
		}

		// Find connector identification persisted in system
		IcfConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcfConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		IcfUidAttribute uidAttribute = new IcfUidAttributeImpl(null, uid, null);

		Map<String, IcfConnectorObject> objectByClassMap = new HashMap<>();

		for (MappingAttribute ah : attributes) {
			String objectClassName = ah.getSchemaAttribute().getObjectClass().getObjectClassName();
			if (!objectByClassMap.containsKey(objectClassName)) {
				IcfObjectClass icfObjectClass = new IcfObjectClassImpl(objectClassName);
				IcfConnectorObject connectorObject = connectorFacade.readObject(connectorKey, connectorConfig,
						icfObjectClass, uidAttribute);
				objectByClassMap.put(objectClassName, connectorObject);
			}
		}

		if (SystemOperationType.PROVISIONING == provisioningType) {
			// Provisioning
			
			return doProvisioning(uid, entity, operationType, attributes, connectorKey, connectorConfig,
					objectByClassMap);

		} else {
			// TODO Synchronisation or reconciliation
		}
		return uid;

	}

	/**
	 * Find list of {@link SysSchemaAttributeHandling} by provisioning type and
	 * entity type on given system
	 * 
	 * @param provisioningType
	 * @param entityType
	 * @param system
	 * @return
	 */
	private List<? extends MappingAttribute> findAttributesHandling(SystemOperationType provisioningType,
			SystemEntityType entityType, SysSystem system) {
		List<SysSystemEntityHandling> entityHandlingList = entityHandlingService.findBySystem(system, provisioningType,
				entityType);
		if (entityHandlingList == null || entityHandlingList.size() != 1) {
			return null;
		}

		SysSystemEntityHandling entityHandling = entityHandlingList.get(0);
		List<SysSchemaAttributeHandling> attributes = attributeHandlingService.findByEntityHandling(entityHandling);

		return attributes;
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
	private String doProvisioning(String uid, AbstractEntity entity, AccountOperationType operationType,
			List<? extends MappingAttribute> attributes, IcfConnectorKey connectorKey,
			IcfConnectorConfiguration connectorConfig, Map<String, IcfConnectorObject> objectByClassMap) {

		Map<String, IcfConnectorObject> objectByClassMapForUpdate = new HashMap<>();
		Map<String, IcfConnectorObject> objectByClassMapForCreate = new HashMap<>();
		Map<String, IcfConnectorObject> objectByClassMapForDelete = new HashMap<>();

		IcfUidAttribute uidAttribute = new IcfUidAttributeImpl(null, uid, null);

		// One IDM account can be mapped to more then one connector object (on
		// more connector class).
		// We have to iterate via all mapped attribute and do operation for all
		// object class

		for (MappingAttribute attributeHandling : attributes) {
			SysSchemaAttribute schemaAttribute = attributeHandling.getSchemaAttribute();
			String objectClassName = schemaAttribute.getObjectClass().getObjectClassName();
			IcfConnectorObject connectorObject = objectByClassMap.get(objectClassName);
			if (AccountOperationType.UPDATE == operationType && connectorObject == null) {
				/**
				 * Create new connector object for this object class
				 */
				IcfConnectorObject connectorObjectForCreate = initConnectorObject(objectByClassMapForCreate,
						objectClassName);
				createAttribute(uid, entity, connectorObjectForCreate, attributeHandling, schemaAttribute,
						objectClassName);

			} else if (AccountOperationType.UPDATE == operationType && connectorObject != null) {
				/**
				 * Update connector object
				 */
				if (schemaAttribute.isUpdateable()) {
					if (!schemaAttribute.isReturnedByDefault()) {
						// TODO update for attributes not returned by default
						// (for example __PASSWORD__)
					} else {
						// Update attribute on resource by given handling
						// attribute and mapped value in entity
						updateAttribute(uid, entity, objectByClassMapForUpdate, attributeHandling, schemaAttribute,
								objectClassName, connectorObject);
					}
				}
			} else if (AccountOperationType.DELETE == operationType && connectorObject != null) {
				/**
				 * Delete connector object for this object class
				 */
				if (connectorObject != null && !objectByClassMapForDelete.containsKey(objectClassName)) {
					objectByClassMapForDelete.put(objectClassName, connectorObject);
				}
			}
		}
		final List<String> uids = new ArrayList<>();
		// call create on ICF module
		objectByClassMapForCreate.forEach((objectClassName, connectorObject) -> {
			log.debug("Provisioning - create object with uid " + uid + " and connector object "
					+ connectorObject.getObjectClass().getType());
			IcfUidAttribute icfUid = connectorFacade.createObject(connectorKey, connectorConfig,
					connectorObject.getObjectClass(), connectorObject.getAttributes());
			if (icfUid != null && icfUid.getUidValue() != null) {
				uids.add(icfUid.getUidValue());
			}
		});

		// call update on ICF module
		objectByClassMapForUpdate.forEach((objectClassName, connectorObject) -> {
			log.debug("Provisioning - update object with uid " + uid + " and connector object "
					+ connectorObject.getObjectClass().getType());
			IcfUidAttribute icfUid = connectorFacade.updateObject(connectorKey, connectorConfig,
					connectorObject.getObjectClass(), uidAttribute, connectorObject.getAttributes());
			if (icfUid != null && icfUid.getUidValue() != null) {
				uids.add(icfUid.getUidValue());
			}
		});
		// call delete on ICF module
		objectByClassMapForDelete.forEach((objectClassName, connectorObject) -> {
			log.debug("Provisioning - delete object with uid " + uid + " and connector object "
					+ connectorObject.getObjectClass().getType());
			connectorFacade.deleteObject(connectorKey, connectorConfig, connectorObject.getObjectClass(), uidAttribute);
		});

		// We have to validate returned uids form connector.
		// Different uids are inconsistent state, we will throw exception
		if (uids.isEmpty()) {
			return uid;
		}
		final String firstUidConnector = uids.get(0);
		boolean foundDiffUid = uids.stream().filter(uidConnector -> {
			return !uidConnector.equals(firstUidConnector);
		}).findFirst().isPresent();
		if (foundDiffUid) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_DIFFERENT_UIDS_FROM_CONNECTOR,
					ImmutableMap.of("uid", uid, "connectorUids", uids));
		}

		return firstUidConnector;

	}

	/**
	 * Return connector object from map by object class. If object for object
	 * class is missing, then will be create.
	 * 
	 * @param objectByClassMap
	 * @param objectClassName
	 * @return
	 */
	private IcfConnectorObject initConnectorObject(Map<String, IcfConnectorObject> objectByClassMap,
			String objectClassName) {
		IcfConnectorObject connectorObject = null;

		if (objectByClassMap != null) {
			connectorObject = objectByClassMap.get(objectClassName);
		}
		if (connectorObject == null) {
			IcfObjectClass ioc = new IcfObjectClassImpl(objectClassName);
			connectorObject = new IcfConnectorObjectImpl(ioc, null);
			if (objectByClassMap != null) {
				objectByClassMap.put(objectClassName, connectorObject);
			}
		}
		return connectorObject;
	}

	/**
	 * Create ICF attribute by schema attribute. ICF attribute will be set with
	 * value obtained form given entity. This value will be transformed to
	 * system value first.
	 * 
	 * @param uid
	 * @param entity
	 * @param connectorObjectForCreate
	 * @param attributeHandling
	 * @param schemaAttribute
	 * @param objectClassName
	 */
	private void createAttribute(String uid, AbstractEntity entity, IcfConnectorObject connectorObjectForCreate,
			MappingAttribute attributeHandling, SysSchemaAttribute schemaAttribute, String objectClassName) {
		if (schemaAttribute.isCreateable()) {
			try {
				Object idmValue = getAttributeValue(uid, entity, attributeHandling);
				IcfAttribute icfAttributeForCreate = createIcfAttribute(attributeHandling, idmValue, entity);
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
	private void updateAttribute(String uid, AbstractEntity entity,
			Map<String, IcfConnectorObject> objectByClassMapForUpdate, MappingAttribute attributeHandling,
			SysSchemaAttribute schemaAttribute, String objectClassName, IcfConnectorObject connectorObject) {
		List<IcfAttribute> icfAttributes = connectorObject.getAttributes();

		Optional<IcfAttribute> icfAttributeOptional = icfAttributes.stream().filter(icfa -> {
			return schemaAttribute.getName().equals(icfa.getName());
		}).findFirst();
		IcfAttribute icfAttribute = null;
		if (icfAttributeOptional.isPresent()) {
			icfAttribute = icfAttributeOptional.get();
		}
	
		updateAttributeValue(uid, entity, objectByClassMapForUpdate, attributeHandling, objectClassName,
				icfAttribute, icfAttributes);
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
	private void updateAttributeValue(String uid, AbstractEntity entity,
			Map<String, IcfConnectorObject> objectByClassMapForUpdate, MappingAttribute attributeHandling,
			String objectClassName, IcfAttribute icfAttribute, List<IcfAttribute> icfAttributes) {

		Object icfValueTransformed = null;
		if (attributeHandling.getSchemaAttribute().isMultivalued()) {
			// Multi value
			List<Object> icfValues = icfAttribute != null ? icfAttribute.getValues() : null;
			icfValueTransformed = attributeHandlingService.transformValueFromResource(icfValues, attributeHandling,
					icfAttributes);
		} else {
			// Single value
			Object icfValue = icfAttribute != null ? icfAttribute.getValue() : null;
			icfValueTransformed = attributeHandlingService.transformValueFromResource(icfValue, attributeHandling,
					icfAttributes);
		}

		try {
			Object idmValue = getAttributeValue(uid, entity, attributeHandling);

			if (!Objects.equals(idmValue, icfValueTransformed)) {
				// values is not equals
				IcfAttribute icfAttributeForUpdate = createIcfAttribute(attributeHandling, idmValue, entity);
				IcfConnectorObject connectorObjectForUpdate = initConnectorObject(objectByClassMapForUpdate,
						objectClassName);
				connectorObjectForUpdate.getAttributes().add(icfAttributeForUpdate);
			}

		} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
					ImmutableMap.of("uid", uid, "property", attributeHandling.getIdmPropertyName()), e);
		}
	}
	
	/**
	 * Find value for this mapped attribute by property name. Return value can be list of objects
	 * 
	 * @param uid
	 * @param entity
	 * @param attributeHandling
	 * @param idmValue
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private Object getAttributeValue(String uid, AbstractEntity entity, MappingAttribute attributeHandling)
			throws IntrospectionException, IllegalAccessException, InvocationTargetException {
		if (attributeHandling.isExtendedAttribute()) {
			IdmFormAttribute defAttribute = formService.getDefinition(((FormableEntity) entity).getClass())
					.getMappedAttributeByName(attributeHandling.getIdmPropertyName());
			List<AbstractFormValue<FormableEntity>> formValues = formService.getValues((FormableEntity) entity,
					defAttribute.getFormDefinition(), defAttribute.getName());
			if (formValues.isEmpty()) {
				return null;
			}
			if(defAttribute.isMultiple()){
				// Multivalue extended attribute
				List<Object> values = new ArrayList<>();
				formValues.stream().forEachOrdered(formValue -> {
					values.add(formValue.getValue());
				});
				return values;
			}else{
				// Singlevalue extended attribute
				AbstractFormValue<FormableEntity> formValue = formValues.get(0);
				if (formValue.isConfidential()) {
					return formService.getConfidentialPersistentValue(formValue);
				}
				return formValue.getValue();
			}
		}
		// Find value from entity
		if (attributeHandling.isEntityAttribute()) {
			if (attributeHandling.isConfidentialAttribute()) {
				// If is attribute isConfidential, then we will find value in
				// secured storage
				return confidentialStorage.getGuardedString(entity, attributeHandling.getIdmPropertyName());
			}
			// We will search value directly in entity by property name
			return getEntityValue(entity, attributeHandling.getIdmPropertyName());
		}

		// Attribute value is not in entity nor in extended attribute.
		// It means attribute is static ... we will call transformation to resource.
		return attributeHandlingService.transformValueToResource(null, attributeHandling, entity);
	}

	/**
	 * Create instance of ICF attribute for given name. Given idm value will be
	 * transformed to resource.
	 * 
	 * @param attributeHandling
	 * @param icfAttribute
	 * @param idmValue
	 * @param entity
	 * @return
	 */
	private IcfAttribute createIcfAttribute(MappingAttribute attributeHandling, Object idmValue,
			AbstractEntity entity) {
		Object idmValueTransformed = null;

		// If is attribute UID, then we don't want do transformation
		if (attributeHandling.isUid()) {
			idmValueTransformed = idmValue;
		}else if(!attributeHandling.isEntityAttribute() && !attributeHandling.isExtendedAttribute()){
			// If is attribute handling resolve as constant, then we don't want do transformation again (was did in getAttributeValue)
			idmValueTransformed = idmValue;
		} else {
			idmValueTransformed = attributeHandlingService.transformValueToResource(idmValue, attributeHandling,
					entity);
		}
		SysSchemaAttribute schemaAttribute = attributeHandling.getSchemaAttribute();
		// Check type of value
		try {
			Class<?> classType = Class.forName(schemaAttribute.getClassType());
			
			// If is multivalue and value is list, then we will iterate list and check every item on correct type
			if (schemaAttribute.isMultivalued() && idmValueTransformed instanceof List){
				((List<?>)idmValueTransformed).stream().forEachOrdered(value ->{
					if (value != null && !(classType.isAssignableFrom(value.getClass()))) {
						throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_VALUE_WRONG_TYPE,
								ImmutableMap.of("attribute", attributeHandling.getIdmPropertyName(), "schemaAttributeType",
										schemaAttribute.getClassType(), "valueType", value.getClass().getName()));
					}
				});
				
			// Check single value on correct type
			}else if (idmValueTransformed != null && !(classType.isAssignableFrom(idmValueTransformed.getClass()))) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_VALUE_WRONG_TYPE,
						ImmutableMap.of("attribute", attributeHandling.getIdmPropertyName(), "schemaAttributeType",
								schemaAttribute.getClassType(), "valueType", idmValueTransformed.getClass().getName()));
			}
		} catch (ClassNotFoundException e) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_TYPE_NOT_FOUND,
					ImmutableMap.of("attribute", attributeHandling.getIdmPropertyName(), "schemaAttributeType",
							schemaAttribute.getClassType()),
					e);
		}

		IcfAttribute icfAttributeForUpdate = null;
		if (IcfConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equals(schemaAttribute.getName())) {
			// Attribute is password type
			icfAttributeForUpdate = new IcfPasswordAttributeImpl((GuardedString) idmValueTransformed);

		} else {
			if(idmValueTransformed instanceof List){
				@SuppressWarnings("unchecked")
				List<Object> values = (List<Object>)idmValueTransformed;
				icfAttributeForUpdate = new IcfAttributeImpl(schemaAttribute.getName(), values, true);
			}else{
				icfAttributeForUpdate = new IcfAttributeImpl(schemaAttribute.getName(), idmValueTransformed);
			}
		}
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

	/**
	 * TODO: remove this lazy injection after provisioning event event will be
	 * done
	 * 
	 * @return
	 */
	private AccIdentityAccountService getIdentityAccountService() {
		if (identityAccountService == null) {
			this.identityAccountService = applicationContext.getBean(AccIdentityAccountService.class);
		}
		return identityAccountService;
	}
}
