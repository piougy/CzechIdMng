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
import eu.bcvsolutions.idm.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.impl.IcPasswordAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Service for do provisioning or synchronisation or reconciliation
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysProvisioningService implements SysProvisioningService {

	public static final String PASSWORD_SCHEMA_PROPERTY_NAME = "__PASSWORD__";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSysProvisioningService.class);
	private final SysSystemEntityHandlingService entityHandlingService;
	private final SysSchemaAttributeHandlingService attributeHandlingService;
	private final IcConnectorFacade connectorFacade;
	private final SysSystemService systemService;
	private final AccIdentityAccountService identityAccountService;
	private final ConfidentialStorage confidentialStorage;
	private final FormService formService;
	private final SysRoleSystemService roleSystemService;
	private final AccAccountManagementService accountManagementService;
	private final SysRoleSystemAttributeService roleSystemAttributeService;
	private final SysSystemEntityService systemEntityService;
	private final AccAccountService accountService;

	@Autowired
	public DefaultSysProvisioningService(SysSystemEntityHandlingService entityHandlingService,
			SysSchemaAttributeHandlingService attributeHandlingService, IcConnectorFacade connectorFacade,
			SysSystemService systemService, ConfidentialStorage confidentialStorage, FormService formService,
			SysRoleSystemService roleSystemService, AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService, AccIdentityAccountService identityAccountService) {

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
		Assert.notNull(identityAccountService);
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
		this.identityAccountService = identityAccountService;
	}

	@Override
	public void doProvisioning(IdmIdentity identity) {
		Assert.notNull(identity);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null);
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
		
		// We successfully deleted account on target system.
		// If SysSystemEntity exist, we delete him.
		if(systemEntity != null){
			systemEntityService.delete(systemEntity);
		}
	}

	@Override
	public void doProvisioning(AccAccount account) {
		Assert.notNull(account);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setAccountId(account.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null);
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
		SysSystem system = account.getSystem();
		SysSystemEntity systemEntity = account.getSystemEntity();
		// find uid from system entity or form account
		String uid = account.getUid();
		String systemUid = getRealUid(account);
		SystemOperationType operationType = SystemOperationType.PROVISIONING;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		
		List<MappingAttribute> finalAttributes = resolveMappedAttributes(account, identity, system, uid, operationType, entityType);
		if(CollectionUtils.isEmpty(finalAttributes)){
			return;
		}

		String uidFromOperation = doOperation(systemUid, identity, AccountOperationType.UPDATE, operationType, entityType, system,
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

	
	
	@Override
	public void fillOverloadedAttribute(SysRoleSystemAttribute overloadingAttribute,
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
		Page<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null);
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
			String systemUid = getRealUid(account);
			SysSystem system = account.getSystem();
			SystemOperationType operationType = SystemOperationType.PROVISIONING;
			SystemEntityType entityType = SystemEntityType.IDENTITY;

			// Find mapped attributes (include overloaded attributes)
			List<MappingAttribute> finalAttributes = resolveMappedAttributes(account, identity, system, uid,
					operationType, entityType);
			if (CollectionUtils.isEmpty(finalAttributes)) {
				return;
			}

			// We try find __PASSWORD__ attribute in mapped attributes
			Optional<? extends MappingAttribute> attriubuteHandlingOptional = finalAttributes.stream()
					.filter((attribute) -> {
						return PASSWORD_SCHEMA_PROPERTY_NAME.equals(attribute.getSchemaAttribute().getName());
					}).findFirst();
			if (!attriubuteHandlingOptional.isPresent()) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_PASSWORD_FIELD_NOT_FOUND,
						ImmutableMap.of("uid", uid));
			}
			MappingAttribute mappedAttribute = attriubuteHandlingOptional.get();

			doProvisioningForAttribute(systemUid, mappedAttribute, passwordChange.getNewPassword(), account.getSystem(),
					AccountOperationType.UPDATE, SystemEntityType.IDENTITY, identity);
		});
	}

	@Override
	public void doProvisioningForAttribute(String uid, MappingAttribute mappedAttribute, Object value, SysSystem system,
			AccountOperationType operationType, SystemEntityType entityType, AbstractEntity entity) {

		Assert.notNull(uid);
		Assert.notNull(system);
		Assert.notNull(entityType);
		Assert.notNull(mappedAttribute);


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
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, uid, null);


		if (!mappedAttribute.getSchemaAttribute().isUpdateable()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_SCHEMA_ATTRIBUTE_IS_NOT_UPDATEABLE,
					ImmutableMap.of("property", mappedAttribute.getIdmPropertyName(), "uid", uid));
		}

		String objectClassName = mappedAttribute.getSchemaAttribute().getObjectClass().getObjectClassName();
		// We do transformation to system if is attribute only constant
		Object valueTransformed = null;
		if(!mappedAttribute.isEntityAttribute() && !mappedAttribute.isExtendedAttribute()){
			// If is attribute handling resolve as constant, then we don't want do transformation again (was did in getAttributeValue)
			valueTransformed = value;
		} else {
			valueTransformed = attributeHandlingService.transformValueToResource(value, mappedAttribute,
					entity);
		}
		IcAttribute icAttributeForCreate = createIcAttribute(mappedAttribute, valueTransformed);
		IcObjectClass icObjectClass = new IcObjectClassImpl(objectClassName);
		// Call ic modul for update single attribute
		connectorFacade.updateObject(connectorKey, connectorConfig, icObjectClass, uidAttribute,
				ImmutableList.of(icAttributeForCreate));

	}

	@Override
	public IcUidAttribute authenticate(AccIdentityAccount identityAccount, SysSystem system) {
		GuardedString password = confidentialStorage.getGuardedString(identityAccount.getIdentity(),
				IdmIdentityService.CONFIDENTIAL_PROPERTY_PASSWORD);
		if (password == null) {
			password = new GuardedString(); // TODO: empty password or null?
		}
		return authenticate(identityAccount.getAccount().getUid(), password, system, SystemOperationType.PROVISIONING,
				SystemEntityType.IDENTITY);
	}

	@Override
	public IcUidAttribute authenticate(String username, GuardedString password, SysSystem system,
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
		// Find attribute handling mapped on schema password attribute
		Optional<? extends MappingAttribute> passwordAttributeHandlingOptional = attributes.stream()
				.filter((attribute) -> {
					return IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equals(attribute.getSchemaAttribute().getName());
				}).findFirst();
		if (!passwordAttributeHandlingOptional.isPresent()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
					ImmutableMap.of("property", IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME, "uid", username));
		}

		MappingAttribute passwordAttributeHandling = passwordAttributeHandlingOptional.get();

		String objectClassName = passwordAttributeHandling.getSchemaAttribute().getObjectClass().getObjectClassName();
		IcObjectClass icObjectClass = new IcObjectClassImpl(objectClassName);

		// Call IC module for check authenticate
		return connectorFacade.authenticateObject(connectorKey, connectorConfig, icObjectClass, username, password);
	}

	/**
	 * Return all mapped attributes for this account (include overloaded attributes)
	 * @param account
	 * @param identity
	 * @param system
	 * @param uid
	 * @param operationType
	 * @param entityType
	 * @return
	 */
	@Override
	public List<MappingAttribute> resolveMappedAttributes(AccAccount account, IdmIdentity identity, SysSystem system,
			String uid, SystemOperationType operationType, SystemEntityType entityType) {
		
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		filter.setSystemId(system.getId());
		filter.setOwnership(Boolean.TRUE);
		filter.setAccountId(account.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null);
		List<AccIdentityAccount> idenityAccoutnList = identityAccounts.getContent();
		if (idenityAccoutnList == null) {
			return null;
		}
		// All identity account with flag ownership on true

		// All role system attributes (overloading) for this uid and same system
		List<SysRoleSystemAttribute> roleSystemAttributesAll = findOverloadingAttributes(uid, identity, system,
				idenityAccoutnList, operationType, entityType);

		// All default mapped attributes from system
		List<? extends MappingAttribute> defaultAttributes = findAttributesHandling(operationType, entityType, system);

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
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, uid, null);

		Map<String, IcConnectorObject> objectByClassMap = new HashMap<>();

		for (MappingAttribute ah : attributes) {
			String objectClassName = ah.getSchemaAttribute().getObjectClass().getObjectClassName();
			if (!objectByClassMap.containsKey(objectClassName)) {
				IcObjectClass icObjectClass = new IcObjectClassImpl(objectClassName);
				IcConnectorObject connectorObject = connectorFacade.readObject(connectorKey, connectorConfig,
						icObjectClass, uidAttribute);
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

		return attributeHandlingService.findByEntityHandling(entityHandling);
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
			List<? extends MappingAttribute> attributes, IcConnectorKey connectorKey,
			IcConnectorConfiguration connectorConfig, Map<String, IcConnectorObject> objectByClassMap) {

		Map<String, IcConnectorObject> objectByClassMapForUpdate = new HashMap<>();
		Map<String, IcConnectorObject> objectByClassMapForCreate = new HashMap<>();
		Map<String, IcConnectorObject> objectByClassMapForDelete = new HashMap<>();

		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, uid, null);

		// One IDM account can be mapped to more then one connector object (on
		// more connector class).
		// We have to iterate via all mapped attribute and do operation for all
		// object class

		for (MappingAttribute attributeHandling : attributes) {
			SysSchemaAttribute schemaAttribute = attributeHandling.getSchemaAttribute();
			String objectClassName = schemaAttribute.getObjectClass().getObjectClassName();
			IcConnectorObject connectorObject = objectByClassMap.get(objectClassName);
			if (AccountOperationType.UPDATE == operationType && connectorObject == null) {
				/**
				 * Create new connector object for this object class
				 */
				IcConnectorObject connectorObjectForCreate = initConnectorObject(objectByClassMapForCreate,
						objectClassName);
				createAttribute(uid, entity, connectorObjectForCreate, attributeHandling, schemaAttribute);

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
			} else if (AccountOperationType.DELETE == operationType) {
				/**
				 * Delete connector object for this object class
				 */
				if (connectorObject != null && !objectByClassMapForDelete.containsKey(objectClassName)) {
					objectByClassMapForDelete.put(objectClassName, connectorObject);
				}
			}
		}
		final List<String> uids = new ArrayList<>();
		// call create on IC module
		objectByClassMapForCreate.forEach((objectClassName, connectorObject) -> {
			LOG.debug("Provisioning - create object with uid {} and connector object {}", uid,
					connectorObject.getObjectClass().getType());
			IcUidAttribute icUid = connectorFacade.createObject(connectorKey, connectorConfig,
					connectorObject.getObjectClass(), connectorObject.getAttributes());
			if (icUid != null && icUid.getUidValue() != null) {
				uids.add(icUid.getUidValue());
			}
		});

		// call update on IC module
		objectByClassMapForUpdate.forEach((objectClassName, connectorObject) -> {
			LOG.debug("Provisioning - update object with uid {} and connector object {}", uid,
					connectorObject.getObjectClass().getType());
			IcUidAttribute icUid = connectorFacade.updateObject(connectorKey, connectorConfig,
					connectorObject.getObjectClass(), uidAttribute, connectorObject.getAttributes());
			if (icUid != null && icUid.getUidValue() != null) {
				uids.add(icUid.getUidValue());
			}
		});
		// call delete on IC module
		objectByClassMapForDelete.forEach((objectClassName, connectorObject) -> {
			LOG.debug("Provisioning - delete object with uid {} and connector object {}", uid,
					connectorObject.getObjectClass().getType());
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
	private IcConnectorObject initConnectorObject(Map<String, IcConnectorObject> objectByClassMap,
			String objectClassName) {
		IcConnectorObject connectorObject = null;

		if (objectByClassMap != null) {
			connectorObject = objectByClassMap.get(objectClassName);
		}
		if (connectorObject == null) {
			IcObjectClass ioc = new IcObjectClassImpl(objectClassName);
			connectorObject = new IcConnectorObjectImpl(ioc, null);
			if (objectByClassMap != null) {
				objectByClassMap.put(objectClassName, connectorObject);
			}
		}
		return connectorObject;
	}

	/**
	 * Create IC attribute by schema attribute. IC attribute will be set with
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
	private void createAttribute(String uid, AbstractEntity entity, IcConnectorObject connectorObjectForCreate,
			MappingAttribute attributeHandling, SysSchemaAttribute schemaAttribute){
		if (schemaAttribute.isCreateable()) {
			try {
				Object idmValue = getAttributeValue(entity, attributeHandling);
				Object idmValueTransformed = null;
				if(!attributeHandling.isEntityAttribute() && !attributeHandling.isExtendedAttribute()){
					// If is attribute handling resolve as constant, then we don't want do transformation again (was did in getAttributeValue)
					idmValueTransformed = idmValue;
				} else {
					idmValueTransformed = attributeHandlingService.transformValueToResource(idmValue, attributeHandling,
							entity);
				}
				IcAttribute icAttributeForCreate = createIcAttribute(attributeHandling, idmValueTransformed);
				connectorObjectForCreate.getAttributes().add(icAttributeForCreate);

			} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | ProvisioningException e) {
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
			Map<String, IcConnectorObject> objectByClassMapForUpdate, MappingAttribute attributeHandling,
			SysSchemaAttribute schemaAttribute, String objectClassName, IcConnectorObject connectorObject) {
		List<IcAttribute> icAttributes = connectorObject.getAttributes();

		Optional<IcAttribute> icAttributeOptional = icAttributes.stream().filter(ica -> {
			return schemaAttribute.getName().equals(ica.getName());
		}).findFirst();
		IcAttribute icAttribute = null;
		if (icAttributeOptional.isPresent()) {
			icAttribute = icAttributeOptional.get();
		}
	
		updateAttributeValue(uid, entity, objectByClassMapForUpdate, attributeHandling, objectClassName,
				icAttribute, icAttributes);
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
	private void updateAttributeValue(String uid, AbstractEntity entity,
			Map<String, IcConnectorObject> objectByClassMapForUpdate, MappingAttribute attributeHandling,
			String objectClassName, IcAttribute icAttribute, List<IcAttribute> icAttributes){

		Object icValueTransformed = null;
		if (attributeHandling.getSchemaAttribute().isMultivalued()) {
			// Multi value
			List<Object> icValues = icAttribute != null ? icAttribute.getValues() : null;
			icValueTransformed = attributeHandlingService.transformValueFromResource(icValues, attributeHandling,
					icAttributes);
		} else {
			// Single value
			Object icValue = icAttribute != null ? icAttribute.getValue() : null;
			icValueTransformed = attributeHandlingService.transformValueFromResource(icValue, attributeHandling,
					icAttributes);
		}

		try {
			Object idmValue = getAttributeValue(entity, attributeHandling);
			Object idmValueTransformed = null;
			if(!attributeHandling.isEntityAttribute() && !attributeHandling.isExtendedAttribute()){
				// If is attribute handling resolve as constant, then we don't want do transformation again (was did in getAttributeValue)
				idmValueTransformed = idmValue;
			} else {
				idmValueTransformed = attributeHandlingService.transformValueToResource(idmValue, attributeHandling,
						entity);
			}

			if (!Objects.equals(idmValueTransformed, icValueTransformed)) {
				// values is not equals
				IcAttribute icAttributeForUpdate = createIcAttribute(attributeHandling, idmValueTransformed);
				IcConnectorObject connectorObjectForUpdate = initConnectorObject(objectByClassMapForUpdate,
						objectClassName);
				connectorObjectForUpdate.getAttributes().add(icAttributeForUpdate);
			}

		} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | ProvisioningException e) {
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
	private Object getAttributeValue(AbstractEntity entity, MappingAttribute attributeHandling)
			throws IntrospectionException, IllegalAccessException, InvocationTargetException {
		if (attributeHandling.isExtendedAttribute()) {
			// TODO: new method to form service to read concrete attribute definition
			IdmFormAttribute defAttribute = formService.getDefinition(((FormableEntity) entity).getClass())
					.getMappedAttributeByName(attributeHandling.getIdmPropertyName());
			if (defAttribute == null) {
				// eav definition could be changed
				LOG.warn("Form attribute defininion [{}] was not found, returning null", attributeHandling.getIdmPropertyName());
				return null;
			}
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
	 * Create instance of IC attribute for given name. Given idm value will be
	 * transformed to resource.
	 * 
	 * @param attributeHandling
	 * @param icAttribute
	 * @param idmValue
	 * @return
	 */
	private IcAttribute createIcAttribute(MappingAttribute attributeHandling, Object idmValue){
		SysSchemaAttribute schemaAttribute = attributeHandling.getSchemaAttribute();
		// Check type of value
		try {
			Class<?> classType = Class.forName(schemaAttribute.getClassType());
			
			// If is multivalue and value is list, then we will iterate list and check every item on correct type
			if (schemaAttribute.isMultivalued() && idmValue instanceof List){
				((List<?>)idmValue).stream().forEachOrdered(value ->{
					if (value != null && !(classType.isAssignableFrom(value.getClass()))) {
						throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_VALUE_WRONG_TYPE,
								ImmutableMap.of("attribute", attributeHandling.getIdmPropertyName(), "schemaAttributeType",
										schemaAttribute.getClassType(), "valueType", value.getClass().getName()));
					}
				});
				
			// Check single value on correct type
			}else if (idmValue != null && !(classType.isAssignableFrom(idmValue.getClass()))) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_VALUE_WRONG_TYPE,
						ImmutableMap.of("attribute", attributeHandling.getName(), "schemaAttributeType",
								schemaAttribute.getClassType(), "valueType", idmValue.getClass().getName()));
			}
		} catch (ClassNotFoundException | ProvisioningException e) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_TYPE_NOT_FOUND,
					ImmutableMap.of("attribute", attributeHandling.getName(), "schemaAttributeType",
							schemaAttribute.getClassType()),
					e);
		}

		IcAttribute icAttributeForUpdate = null;
		if (IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equals(schemaAttribute.getName())) {
			// Attribute is password type
			icAttributeForUpdate = new IcPasswordAttributeImpl((GuardedString) idmValue);

		} else {
			if(idmValue instanceof List){
				@SuppressWarnings("unchecked")
				List<Object> values = (List<Object>)idmValue;
				icAttributeForUpdate = new IcAttributeImpl(schemaAttribute.getName(), values, true);
			}else{
				icAttributeForUpdate = new IcAttributeImpl(schemaAttribute.getName(), idmValue);
			}
		}
		return icAttributeForUpdate;
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
	private Object getEntityValue(AbstractEntity entity, String propertyName) throws 
	IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
			 {
		Optional<PropertyDescriptor> propertyDescriptionOptional = Arrays
				.asList(Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors())
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
	 * Return real uid from system entity.
	 * If system entity do not exist, then return uid from account.
	 * @param account
	 * @return
	 */
	private String getRealUid(AccAccount account){
		Assert.notNull(account);
		String uid = account.getUid();

		SysSystemEntity systemEntity = account.getSystemEntity();
		if (systemEntity != null && account.getUid() != null) {
			uid = systemEntity.getUid();
		}
		return uid;
	}
}
