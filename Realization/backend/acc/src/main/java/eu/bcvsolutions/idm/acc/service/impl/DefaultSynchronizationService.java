package eu.bcvsolutions.idm.acc.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.dto.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationConfig;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSynchronizationConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSynchronizationLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSyncDelta;
import eu.bcvsolutions.idm.ic.api.IcSyncResultsHandler;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.impl.IcSyncDeltaTypeEnum;
import eu.bcvsolutions.idm.ic.impl.IcSyncTokenImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

@Service
public class DefaultSynchronizationService implements SynchronizationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSynchronizationService.class);
	private final IcConnectorFacade connectorFacade;
	private final SysSystemService systemService;
	private final SysSystemAttributeMappingService attributeHandlingService;
	private final SysSynchronizationConfigService synchronizationConfigService;
	private final SysSynchronizationLogService synchronizationLogService;
	private final AccAccountService accountService;
	private final SysSystemEntityService systemEntityService;
	private final ConfidentialStorage confidentialStorage;
	private final FormService formService;
	private final IdmIdentityService identityService;
	private final AccIdentityAccountService identityAccoutnService;
	private final SysSyncItemLogService syncItemLogService;
	@Autowired(required = false)
	private ApplicationContext applicationContext;
	private SynchronizationService synchronizationService;

	@Autowired
	public DefaultSynchronizationService(IcConnectorFacade connectorFacade, SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSynchronizationConfigService synchronizationConfigService,
			SysSynchronizationLogService synchronizationLogService, AccAccountService accountService,
			SysSystemEntityService systemEntityService, ConfidentialStorage confidentialStorage,
			FormService formService, IdmIdentityService identityService,
			AccIdentityAccountService identityAccoutnService, SysSyncItemLogService syncItemLogService) {
		Assert.notNull(connectorFacade);
		Assert.notNull(systemService);
		Assert.notNull(attributeHandlingService);
		Assert.notNull(synchronizationConfigService);
		Assert.notNull(synchronizationLogService);
		Assert.notNull(accountService);
		Assert.notNull(systemEntityService);
		Assert.notNull(confidentialStorage);
		Assert.notNull(formService);
		Assert.notNull(identityService);
		Assert.notNull(identityAccoutnService);
		Assert.notNull(syncItemLogService);

		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.attributeHandlingService = attributeHandlingService;
		this.synchronizationConfigService = synchronizationConfigService;
		this.synchronizationLogService = synchronizationLogService;
		this.accountService = accountService;
		this.systemEntityService = systemEntityService;
		this.confidentialStorage = confidentialStorage;
		this.formService = formService;
		this.identityService = identityService;
		this.identityAccoutnService = identityAccoutnService;
		this.syncItemLogService = syncItemLogService;
	}

	@Override
	@Transactional
	public SysSynchronizationConfig synchronization(SysSynchronizationConfig config) {
		Assert.notNull(config);
		// Synchronization must be enabled
		if (!config.isEnabled()) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_NOT_ENABLED,
					ImmutableMap.of("name", config.getName()));
		}

		// Synchronization can not be running twice
		SynchronizationLogFilter logFilter = new SynchronizationLogFilter();
		logFilter.setSynchronizationConfigId(config.getId());
		logFilter.setRunning(Boolean.TRUE);
		if (!synchronizationLogService.find(logFilter, null).getContent().isEmpty()) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_RUNNING,
					ImmutableMap.of("name", config.getName()));
		}

		SysSystemMapping mapping = config.getSystemMapping();
		Assert.notNull(mapping);
		SysSystem system = mapping.getSystem();
		Assert.notNull(system);
		SystemEntityType entityType = mapping.getEntityType();

		SystemAttributeMappingFilter attributeHandlingFilter = new SystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMapping> mappedAttributes = attributeHandlingService.find(attributeHandlingFilter, null)
				.getContent();

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

		IcObjectClass objectClass = findObjectClass(mappedAttributes);

		Object lastToken = config.getToken();
		IcSyncToken lastIcToken = lastToken != null ? new IcSyncTokenImpl(lastToken) : null;

		SysSynchronizationLog log = new SysSynchronizationLog();
		log.setSynchronizationConfig(config);
		log.setStarted(LocalDateTime.now());
		log.setRunning(true);
		log.setToken(lastToken != null ? lastToken.toString() : null);

		try {
			synchronizationLogService.save(log);
			List<SysSyncActionLog> actionsLog = new ArrayList<>();
			IcSyncResultsHandler icSyncResultsHandler = new IcSyncResultsHandler() {

				@Override
				public boolean handle(IcSyncDelta delta) {
					SysSyncItemLog itemLog = new SysSyncItemLog();
					try {
						Assert.notNull(delta);
						Assert.notNull(delta.getUid());
						String uid = delta.getUid().getUidValue();
						itemLog.setIdentification(uid);
						itemLog.setDisplayName(uid);
						itemLog.setType(entityType.getEntityType().getSimpleName());

						boolean result = findSynchronizationService().doItemSynchronization(config, system, entityType,
								mappedAttributes, log, itemLog, actionsLog, delta);
						// Save token
						IcSyncToken token = delta.getToken();
						String tokenObject = token.getValue() != null ? token.getValue().toString() : null;
						log.setToken(tokenObject);
						config.setToken(tokenObject);

						return result;
					} catch (Exception ex) {
						LOG.error(MessageFormat.format("Synchronization - error for uid {0}",
								delta.getUid().getUidValue()), ex);
						return true;
					} finally {
						synchronizationConfigService.save(config);
						synchronizationLogService.save(log);
						if (itemLog.getSyncActionLog() == null) {
							// Default action log (for unexpected situation)
							String message = MessageFormat.format("Missing syncActionLog for delta {0}",
									delta.toString());
							LOG.warn(message);
							SysSyncActionLog actionLogDefault = new SysSyncActionLog();
							actionLogDefault.setSyncLog(log);
							actionLogDefault.setOperationCount(1);
							actionLogDefault.setOperationResult(OperationResultType.ERROR);
							actionLogDefault.setSyncAction(SynchronizationActionType.IGNORE);
							itemLog.addToLog(message);
							itemLog.setSyncActionLog(actionLogDefault);
						}
						syncItemLogService.save(itemLog);
					}
				}
			};

			IcSyncToken token = connectorFacade.synchronization(connectorKey, connectorConfig, objectClass, lastIcToken,
					icSyncResultsHandler);
			// config.setToken(token != null && token.getValue() != null ?
			// token.getValue().toString() : null);
			return synchronizationConfigService.save(config);

		} finally {
			log.setRunning(false);
			log.setEnded(LocalDateTime.now());
			synchronizationLogService.save(log);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean doItemSynchronization(SysSynchronizationConfig config, SysSystem system, SystemEntityType entityType,
			List<SysSystemAttributeMapping> mappedAttributes, SysSynchronizationLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs, IcSyncDelta delta) {

		IcSyncDeltaTypeEnum type = delta.getDeltaType();
		IcConnectorObject icObject = delta.getObject();

		if (IcSyncDeltaTypeEnum.CREATE == type || IcSyncDeltaTypeEnum.UPDATE == type
				|| IcSyncDeltaTypeEnum.CREATE_OR_UPDATE == type) {
			// Update or create
			IcUidAttribute uid = delta.getUid();
			Assert.notNull(icObject);
			List<IcAttribute> icAttributes = icObject.getAttributes();

			SystemEntityFilter systemEntityFilter = new SystemEntityFilter();
			systemEntityFilter.setEntityType(entityType);
			systemEntityFilter.setSystemId(system.getId());
			systemEntityFilter.setUidId(uid.getUidValue());
			List<SysSystemEntity> systemEntities = systemEntityService.find(systemEntityFilter, null).getContent();
			if (systemEntities.isEmpty()) {
				try {
					// Account not exist in IDM
					logItem.addToLog("Account not exist in IDM");

					// Create system entity instance
					SysSystemEntity systemEntity = new SysSystemEntity();
					systemEntity.setEntityType(entityType);
					systemEntity.setUid(uid.getUidValue());
					systemEntity.setSystem(system);
					systemEntityService.save(systemEntity);

					// Create idm account
					AccAccount account = new AccAccount();
					account.setSystem(system);
					account.setSystemEntity(systemEntity);
					account.setAccountType(AccountType.PERSONAL);
					account.setUid(uid.getUidValue());
					accountService.save(account);

					AbstractEntity entity = findEntityByCorrelationAttribute(config.getCorrelationAttribute(),
							entityType, icAttributes);
					if (entity != null) {
						// Account not exist but, entity by correlation was
						// found (ENTITY MATCHED)
						logItem.addToLog("Account not exist but, entity by correlation was found (entity unlinked).");
					} else if (SystemEntityType.IDENTITY == entityType) {
						// Account not exist and entity too (UNMATCHED)
						logItem.addToLog("Account not exist and entity too (missing entity).");
						switch (config.getMissingEntityAction()) {
						case IGNORE:
							// Ignore we will do nothing
							logItem.addToLog("Missing entity action is IGNORE, we will do nothing.");
							initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.SUCCESS, logItem,
									log, actionLogs);
							return true;
						case CREATE_ENTITY:
							if (true) {
								throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_MORE_UID,
										"ddddddddddddd");
							}
							// We will create new Identity
							logItem.addToLog("Missing entity action is CREATE_ENTITY, we will do create new identity.");
							IdmIdentity identity = new IdmIdentity();
							// Fill Identity by mapped attribute
							identity = (IdmIdentity) fillEntity(mappedAttributes, uid.getUidValue(), icAttributes,
									identity);
							// Create new Identity
							identityService.save(identity);

							// Create new Identity account relation
							AccIdentityAccount identityAccount = new AccIdentityAccount();
							identityAccount.setAccount(account);
							identityAccount.setIdentity(identity);
							identityAccoutnService.save(identityAccount);

							// Identity Created
							logItem.addToLog(
									MessageFormat.format("Identity with id {0} was created", identity.getId()));
							logItem.setDisplayName(identity.getUsername());
							initSyncActionLog(SynchronizationActionType.CREATE_ENTITY, OperationResultType.SUCCESS,
									logItem, log, actionLogs);
							return true;
						}
					}
				} catch (Exception e) {
					String message = MessageFormat.format(
							"Synchronization - exception during create new entity for UID {0}", uid.getUidValue());
					logItem.setMessage(message);
					logItem.addToLog(Throwables.getStackTraceAsString(e));
					initSyncActionLog(SynchronizationActionType.CREATE_ENTITY, OperationResultType.ERROR, logItem, log,
							actionLogs);
					LOG.error(message, e);
					throw e;
				}

			} else if (systemEntities.size() == 1) {
				try {
					SysSystemEntity systemEntity = systemEntities.get(0);
					// Account exist in IDM (LINKED)

					// Do update entity
					AccountFilter accountFilter = new AccountFilter();
					accountFilter.setSystemEntityId(systemEntity.getId());
					List<AccAccount> accounts = accountService.find(accountFilter, null).getContent();
					if (accounts.isEmpty()) {
						// TODO: Account not found
					} else if (accounts.size() > 1) {
						// TODO: Exception
					} else if (accounts.size() == 1) {
						AccAccount account = accounts.get(0);
						switch (config.getLinkedAction()) {
						case IGNORE:
							logItem.addToLog("Linked action is IGNORE. We will do nothing");
							initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.SUCCESS, logItem,
									log, actionLogs);
							return true;
						case UNLINK:
							logItem.addToLog("Linked action is UNLINK.");
							// TODO action

							initSyncActionLog(SynchronizationActionType.UNLINK, OperationResultType.SUCCESS, logItem,
									log, actionLogs);
							return true;
						case UPDATE_ENTITY:
							logItem.addToLog("Linked action is UPDATE_ENTITY.");
							if (SystemEntityType.IDENTITY == entityType) {
								IdentityAccountFilter identityAccountFilter = new IdentityAccountFilter();
								identityAccountFilter.setAccountId(account.getId());
								identityAccountFilter.setOwnership(Boolean.TRUE);
								List<AccIdentityAccount> identityAccounts = identityAccoutnService
										.find(identityAccountFilter, null).getContent();
								if (identityAccounts.isEmpty()) {
									// Entity missing TODO: Create?
								} else {
									// We assume that all identity accounts
									// (mark as
									// ownership) have same identity!
									IdmIdentity identity = identityAccounts.get(0).getIdentity();
									// Update identity
									identity = (IdmIdentity) fillEntity(mappedAttributes, uid.getUidValue(),
											icAttributes, identity);
									identityService.save(identity);

									// Identity Updated
									logItem.addToLog(
											MessageFormat.format("Identity with id {0} was updated", identity.getId()));
									logItem.setDisplayName(identity.getUsername());
									initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY,
											OperationResultType.SUCCESS, logItem, log, actionLogs);
									return true;

								}
							} else if (SystemEntityType.GROUP == entityType) {
								// TODO: for groups
							}
							return true;
						}
					}
				} catch (Exception e) {
					String message = MessageFormat.format(
							"Synchronization - exception during update new entity for UID {0}", uid.getUidValue());
					logItem.setMessage(message);
					logItem.addToLog(Throwables.getStackTraceAsString(e));
					initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.ERROR, logItem, log,
							actionLogs);
					LOG.error(message, e);
					throw e;
				}
			} else {
				// TODO: exception and log
			}

		} else if (IcSyncDeltaTypeEnum.DELETE == type) {
			// Delete
			delta.getObject();
		}
		return true;
	}

	private void initSyncActionLog(SynchronizationActionType actionType, OperationResultType resultType,
			SysSyncItemLog logItem, SysSynchronizationLog log, List<SysSyncActionLog> actionLogs) {

		SysSyncActionLog actionLog = null;
		Optional<SysSyncActionLog> optionalActionLog = actionLogs.stream().filter(al -> {
			return actionType == al.getSyncAction() && resultType == al.getOperationResult();
		}).findFirst();
		if (optionalActionLog.isPresent()) {
			actionLog = optionalActionLog.get();
		} else {
			actionLog = new SysSyncActionLog();
			actionLog.setOperationResult(resultType);
			actionLog.setSyncAction(actionType);
			actionLog.setSyncLog(log);
			actionLogs.add(actionLog);
		}
		logItem.setSyncActionLog(actionLog);
		actionLog.setOperationCount(actionLog.getOperationCount() + 1);
	}

	/**
	 * TODO: !!!We assume that all mapped attributes have same object class!!!
	 * Find objectClass from mapped attribute
	 * 
	 * @param mappedAttributes
	 * @return
	 */
	private IcObjectClass findObjectClass(List<SysSystemAttributeMapping> mappedAttributes) {
		Assert.notNull(mappedAttributes);
		return new IcObjectClassImpl(
				mappedAttributes.get(0).getSchemaAttribute().getObjectClass().getObjectClassName());
	}

	private SynchronizationService findSynchronizationService() {
		if (this.synchronizationService == null) {
			this.synchronizationService = applicationContext.getBean(SynchronizationService.class);
		}
		return this.synchronizationService;
	}

	private AbstractEntity findEntityByCorrelationAttribute(AttributeMapping attribute, SystemEntityType entityType,
			List<IcAttribute> icAttributes) {
		Assert.notNull(attribute);
		Assert.notNull(entityType);
		Assert.notNull(icAttributes);

		if (attribute.isEntityAttribute()) {
			if (SystemEntityType.IDENTITY == entityType) {
				Object value = getValueByMappedAttribute(attribute, icAttributes);
				if (value == null) {
					return null;
				}
				IdentityFilter identityFilter = new IdentityFilter();
				identityFilter.setProperty(attribute.getIdmPropertyName());
				identityFilter.setValue(value);
				List<IdmIdentity> identities = identityService.find(identityFilter, null).getContent();
				if (CollectionUtils.isEmpty(identities)) {
					return null;
				}
				if (identities.size() > 1) {
					throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_CORRELATION_TO_MANY_RESULTS,
							ImmutableMap.of("correlationAttribute", attribute.getName(), "value", value));
				}
				if (identities.size() == 1) {
					return identities.get(0);
				}
			}
		} else if (attribute.isExtendedAttribute()) {
			// TODO: not supported now
			return null;
		}
		return null;
	}

	private Object setEntityValue(AbstractEntity entity, String propertyName, Object value)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Optional<PropertyDescriptor> propertyDescriptionOptional = Arrays
				.asList(Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors()).stream()
				.filter(propertyDescriptor -> {
					return propertyName.equals(propertyDescriptor.getName());
				}).findFirst();
		if (!propertyDescriptionOptional.isPresent()) {
			throw new IllegalAccessException("Field " + propertyName + " not found!");
		}
		PropertyDescriptor propertyDescriptor = propertyDescriptionOptional.get();

		return propertyDescriptor.getWriteMethod().invoke(entity, value);
	}

	private AbstractEntity fillEntity(List<SysSystemAttributeMapping> mappedAttributes, String uid,
			List<IcAttribute> icAttributes, AbstractEntity entity) {
		mappedAttributes.stream().filter(attribute -> {
			// Skip disabled attributes
			return !attribute.isDisabledAttribute();

		}).forEach(attribute -> {
			String attributeProperty = attribute.getIdmPropertyName();
			Object transformedValue = getValueByMappedAttribute(attribute, icAttributes);
			if (attribute.isEntityAttribute()) {
				if (attribute.isConfidentialAttribute()) {
					// If is attribute confidential, then we will set
					// value to
					// secured storage
					if (!(transformedValue == null || transformedValue instanceof GuardedString)) {
						throw new ProvisioningException(AccResultCode.CONFIDENTIAL_VALUE_IS_NOT_GUARDED_STRING,
								ImmutableMap.of("property", attributeProperty, "class",
										transformedValue.getClass().getName()));
					}

					confidentialStorage.saveGuardedString(entity, attribute.getIdmPropertyName(),
							(GuardedString) transformedValue);

				} else {
					// Set transformed value from target system to identity
					try {
						setEntityValue(entity, attributeProperty, transformedValue);
					} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | ProvisioningException e) {
						throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
								ImmutableMap.of("property", attributeProperty, "uid", uid), e);
					}
				}
			} else if (attribute.isExtendedAttribute()) {
				// TODO: new method to form service to read concrete
				// attribute definition
				IdmFormAttribute defAttribute = formService.getDefinition(((FormableEntity) entity).getClass())
						.getMappedAttributeByName(attributeProperty);
				if (defAttribute == null) {
					// eav definition could be changed
					LOG.warn("Form attribute defininion [{}] was not found, skip", attributeProperty);
					// continue;
				}
				// List<AbstractFormValue<FormableEntity>> formValues =
				// formService.getValues((FormableEntity) entity,
				// defAttribute.getFormDefinition(),
				// defAttribute.getName());
				// if (formValues.isEmpty()) {
				// continue;
				// }
				//
				// if(defAttribute.isMultiple()){
				// // Multivalue extended attribute
				// List<Object> values = new ArrayList<>();
				// formValues.stream().forEachOrdered(formValue -> {
				// values.add(formValue.getValue());
				// });
				//
				// }else{
				// // Singlevalue extended attribute
				// AbstractFormValue<FormableEntity> formValue =
				// formValues.get(0);
				// if (formValue.isConfidential()) {
				// return
				// formService.getConfidentialPersistentValue(formValue);
				// }
				// return formValue.getValue();
				// }
			}
		});
		return entity;
	}

	private Object getValueByMappedAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes) {
		Optional<IcAttribute> optionalIcAttribute = icAttributes.stream().filter(icAttribute -> {
			return attribute.getSchemaAttribute().getName().equals(icAttribute.getName());
		}).findFirst();
		if (!optionalIcAttribute.isPresent()) {
			return null;
		}
		IcAttribute icAttribute = optionalIcAttribute.get();
		Object icValue = null;
		if (icAttribute.isMultiValue()) {
			icValue = icAttribute.getValues();
		} else {
			icValue = icAttribute.getValue();
		}

		Object transformedValue = attributeHandlingService.transformValueFromResource(icValue, attribute, icAttributes);
		return transformedValue;
	}

}
