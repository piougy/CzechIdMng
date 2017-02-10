package eu.bcvsolutions.idm.acc.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.MappingAttributeDto;
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
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
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
	private final AccIdentityAccountService identityAccountService;
	private final ConfidentialStorage confidentialStorage;
	private final FormService formService;
	private final SysRoleSystemService roleSystemService;
	private final AccAccountManagementService accountManagementService;
	private final SysRoleSystemAttributeService roleSystemAttributeService;
	private final SysSystemEntityService systemEntityService;
	private final AccAccountService accountService;
	private final ProvisioningExecutor provisioningExecutor;

	@Autowired
	public DefaultProvisioningService(SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, IcConnectorFacade connectorFacade,
			SysSystemService systemService, ConfidentialStorage confidentialStorage, FormService formService,
			SysRoleSystemService roleSystemService, AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService, AccIdentityAccountService identityAccountService,
			ProvisioningExecutor provisioningExecutor) {

		Assert.notNull(systemMappingService);
		Assert.notNull(attributeMappingService);
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
		Assert.notNull(provisioningExecutor);
		//
		this.systemMappingService = systemMappingService;
		this.attributeMappingService = attributeMappingService;
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
		this.provisioningExecutor = provisioningExecutor;
	}

	@Override
	public void doProvisioning(IdmIdentity identity) {
		Assert.notNull(identity);
		//
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
		Map<String, Object> accountAttributes = new HashMap<>();
		if (ProvisioningOperationType.DELETE != operationType) { // delete - account attributes is not needed
			for (AttributeMapping attributeHandling : attributes) {
				if (attributeHandling.isDisabledAttribute()) {
					continue;
				}
				accountAttributes.put(attributeHandling.getSchemaAttribute().getName(), getAttributeValue(entity, attributeHandling));
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
		IcAttribute icAttributeForCreate = attributeMappingService.createIcAttribute(attributeMapping, valueTransformed);
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

		List<? extends AttributeMapping> attributes = findAttributeMappings(system, entityType);
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
		Optional<? extends AttributeMapping> passwordAttributeHandlingOptional = attributes.stream()
				.filter((attribute) -> {
					return IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equals(attribute.getSchemaAttribute().getName());
				}).findFirst();
		if (!passwordAttributeHandlingOptional.isPresent()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
					ImmutableMap.of("property", IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME, "uid", username));
		}

		AttributeMapping passwordAttributeHandling = passwordAttributeHandlingOptional.get();

		String objectClassName = passwordAttributeHandling.getSchemaAttribute().getObjectClass().getObjectClassName();
		IcObjectClass icObjectClass = new IcObjectClassImpl(objectClassName);

		// Call IC module for check authenticate
		return connectorFacade.authenticateObject(system.getConnectorInstance(), connectorConfig, icObjectClass, username, password);
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
		Page<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null);
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
	private List<AttributeMapping> compileAttributes(List<? extends AttributeMapping> defaultAttributes,
			List<SysRoleSystemAttribute> overloadingAttributes) {
		List<AttributeMapping> finalAttributes = new ArrayList<>();
		if (defaultAttributes == null) {
			return null;
		}
		defaultAttributes.stream().forEach(defaultAttribute -> {

			Optional<SysRoleSystemAttribute> overloadingAttributeOptional = overloadingAttributes.stream()
					.filter(roleSystemAttribute -> {
						// Search attribute override same schema attribute
						return roleSystemAttribute.getSystemAttributeMapping().equals(defaultAttribute);
					}).sorted((att1, att2) -> {
						// Sort attributes by role name
						return att2.getRoleSystem().getRole().getName()
								.compareTo(att1.getRoleSystem().getRole().getName());
					}).findFirst();

			if (overloadingAttributeOptional.isPresent()) {
				SysRoleSystemAttribute overloadingAttribute = overloadingAttributeOptional.get();
				// Disabled attribute will be skipped
				if (!overloadingAttribute.isDisabledDefaultAttribute()) {
					// We can't use instance of SysSysteAttributeMapping and set
					// up overloaded value (it is entity).
					// We have to create own dto and set up all values
					// (overloaded and default)
					AttributeMapping overloadedAttribute = new MappingAttributeDto();
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
	
	/**
	 * Find value for this mapped attribute by property name. Return value can be list of objects. Returns transformed value.
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
	private Object getAttributeValue(AbstractEntity entity, AttributeMapping attributeHandling) {
		Object idmValue = null;
		//
		if (attributeHandling.isExtendedAttribute()) {
			List<AbstractFormValue<FormableEntity>> formValues = formService.getValues((FormableEntity) entity, attributeHandling.getIdmPropertyName());
			if (formValues.isEmpty()) {
				idmValue = null;
			} else if(attributeHandling.getSchemaAttribute().isMultivalued()){
				// Multiple value extended attribute
				List<Object> values = new ArrayList<>();
				formValues.stream().forEachOrdered(formValue -> {
					values.add(formValue.getValue());
				});
				idmValue = values;
			} else {
				// Single value extended attribute
				AbstractFormValue<FormableEntity> formValue = formValues.get(0);
				if (formValue.isConfidential()) {
					idmValue = formService.getConfidentialPersistentValue(formValue);
				} else {
					idmValue = formValue.getValue();
				}
			}
		}
		// Find value from entity
		else if (attributeHandling.isEntityAttribute()) {
			if (attributeHandling.isConfidentialAttribute()) {
				// If is attribute isConfidential, then we will find value in
				// secured storage
				idmValue = confidentialStorage.getGuardedString(entity, attributeHandling.getIdmPropertyName());
			} else {
				try {
					// We will search value directly in entity by property name
					idmValue = getEntityValue(entity, attributeHandling.getIdmPropertyName());
				} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | ProvisioningException o_O) {
					throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
							ImmutableMap.of("property", attributeHandling.getIdmPropertyName(), "entityType", entity.getClass()), o_O);
				}
			}
		} else {
			// If Attribute value is not in entity nor in extended attribute, then idmValue is null.
			// It means attribute is static ... we will call transformation to resource.
		}
		return attributeMappingService.transformValueToResource(idmValue, attributeHandling, entity);
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
}
