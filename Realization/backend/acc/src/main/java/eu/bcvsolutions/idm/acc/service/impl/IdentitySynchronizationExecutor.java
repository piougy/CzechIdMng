package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Identity sync executor
 * @author svandav
 *
 */
@Component
public class IdentitySynchronizationExecutor extends AbstractSynchronizationExecutor<IdmIdentityDto>
		implements SynchronizationEntityExecutor {

	private final IdmIdentityService identityService;
	private final IdmIdentityRepository identityRepository;
	private final AccIdentityAccountService identityAccoutnService;
	private final IdmIdentityRoleService identityRoleService;
	private final FormService formService;

	@Autowired
	public IdentitySynchronizationExecutor(IcConnectorFacade connectorFacade, SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService, SysSyncLogService synchronizationLogService,
			SysSyncActionLogService syncActionLogService, AccAccountService accountService,
			SysSystemEntityService systemEntityService, ConfidentialStorage confidentialStorage,
			FormService formService, IdmIdentityService identityService, IdmIdentityRepository identityRepository,
			AccIdentityAccountService identityAccoutnService, SysSyncItemLogService syncItemLogService,
			IdmIdentityRoleService identityRoleService, EntityEventManager entityEventManager,
			GroovyScriptService groovyScriptService, WorkflowProcessInstanceService workflowProcessInstanceService,
			EntityManager entityManager) {
		super(connectorFacade, systemService, attributeHandlingService, synchronizationConfigService,
				synchronizationLogService, syncActionLogService, accountService, systemEntityService,
				confidentialStorage, formService, syncItemLogService, entityEventManager, groovyScriptService,
				workflowProcessInstanceService, entityManager);

		Assert.notNull(identityService, "Identity service is mandatory!");
		Assert.notNull(identityAccoutnService, "Identity account service is mandatory!");
		Assert.notNull(identityRoleService, "Identity role service is mandatory!");
		Assert.notNull(formService);

		this.identityService = identityService;
		this.identityAccoutnService = identityAccoutnService;
		this.identityRoleService = identityRoleService;
		this.identityRepository = identityRepository;
		this.formService = formService;
	}

	/**
	 * Delete entity linked with given account
	 * 
	 * @param account
	 * @param entityType
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doDeleteEntity(AccAccount account, SystemEntityType entityType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		UUID entityId = getEntityByAccount(account.getId());
		IdmIdentityDto identity = null;
		if (entityId != null) {
			identity = identityService.get(entityId);
		}
		if (identity == null) {
			addToItemLog(logItem, "Identity account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		// Delete identity
		identityService.delete(identity);
	}

	/**
	 * Call provisioning for given account
	 * 
	 * @param account
	 * @param entityType
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUpdateAccount(AccAccount account, SystemEntityType entityType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		UUID entityId = getEntityByAccount(account.getId());
		IdmIdentity identity = null;
		if (entityId != null) {
			identity = identityRepository.findOne(entityId);
		}
		if (identity == null) {
			addToItemLog(logItem, "Identity account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		// Call provisioning for this entity
		doUpdateAccountByEntity(identity, entityType, logItem);
	}

	/**
	 * Call provisioning for given account
	 * 
	 * @param entity
	 * @param entityType
	 * @param logItem
	 */
	protected void doUpdateAccountByEntity(AbstractEntity entity, SystemEntityType entityType, SysSyncItemLog logItem) {
		IdmIdentity identity = (IdmIdentity) entity;
		addToItemLog(logItem,
				MessageFormat.format(
						"Call provisioning (process IdentityEventType.SAVE) for identity ({0}) with username ({1}).",
						identity.getId(), identity.getUsername()));
		entityEventManager.process(new IdentityEvent(IdentityEventType.UPDATE, identityService.get(identity.getId()))).getContent();
	}

	/**
	 * Create and persist new entity by data from IC attributes
	 * 
	 * @param entityType
	 * @param mappedAttributes
	 * @param logItem
	 * @param uid
	 * @param icAttributes
	 * @param account
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doCreateEntity(SystemEntityType entityType, List<SysSystemAttributeMapping> mappedAttributes,
			SysSyncItemLog logItem, String uid, List<IcAttribute> icAttributes, AccAccount account) {
		// We will create new Identity
		addToItemLog(logItem, "Missing entity action is CREATE_ENTITY, we will do create new identity.");
		IdmIdentity identity = new IdmIdentity();
		// Fill Identity by mapped attribute
		identity = (IdmIdentity) fillEntity(mappedAttributes, uid, icAttributes, identity, true);
		// Create new Identity
		identity = identityService.saveIdentity(identity);
		// Update extended attribute (entity must be persisted first)
		updateExtendedAttributes(mappedAttributes, uid, icAttributes, identity, true);
		// Update confidential attribute (entity must be persisted first)
		updateConfidentialAttributes(mappedAttributes, uid, icAttributes, identity, true);

		// Create new Identity account relation
		AccIdentityAccountDto identityAccount = new AccIdentityAccountDto();
		identityAccount.setAccount(account.getId());
		identityAccount.setIdentity(identity.getId());
		identityAccount.setOwnership(true);
		identityAccoutnService.save(identityAccount);

		// Identity Created
		addToItemLog(logItem, MessageFormat.format("Identity with id {0} was created", identity.getId()));
		if (logItem != null) {
			logItem.setDisplayName(identity.getUsername());
		}
	}

	/**
	 * Fill data from IC attributes to entity (EAV and confidential storage too)
	 * 
	 * @param account
	 * @param entityType
	 * @param uid
	 * @param icAttributes
	 * @param mappedAttributes
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUpdateEntity(AccAccount account, SystemEntityType entityType, String uid,
			List<IcAttribute> icAttributes, List<SysSystemAttributeMapping> mappedAttributes, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		UUID entityId = getEntityByAccount(account.getId());
		IdmIdentity identity = null;
		if (entityId != null) {
			identity = identityRepository.findOne(entityId);
		}
		if (identity != null) {
			// Update identity
			identity = (IdmIdentity) fillEntity(mappedAttributes, uid, icAttributes, identity, false);
			identity = identityService.saveIdentity(identity);
			// Update extended attribute (entity must be persisted first)
			updateExtendedAttributes(mappedAttributes, uid, icAttributes, identity, false);
			// Update confidential attribute (entity must be persisted
			// first)
			updateConfidentialAttributes(mappedAttributes, uid, icAttributes, identity, false);

			// Identity Updated
			addToItemLog(logItem, MessageFormat.format("Identity with id {0} was updated", identity.getId()));
			if (logItem != null) {
				logItem.setDisplayName(identity.getUsername());
			}

			return;
		} else {
			addToItemLog(logItem, "Identity account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
	}

	/**
	 * Operation remove IdentityAccount relations and linked roles
	 * 
	 * @param account
	 * @param removeIdentityRole
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUnlink(AccAccount account, boolean removeIdentityRole, SysSyncLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs) {

		EntityAccountFilter identityAccountFilter = new IdentityAccountFilter();
		identityAccountFilter.setAccountId(account.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccoutnService
				.find((IdentityAccountFilter) identityAccountFilter, null).getContent();
		if (identityAccounts.isEmpty()) {
			addToItemLog(logItem, "Identity account relation was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		addToItemLog(logItem, MessageFormat.format("Identity-account relations to delete {0}", identityAccounts));

		identityAccounts.stream().forEach(identityAccount -> {
			// We will remove identity account, but without delete connected
			// account
			identityAccoutnService.delete(identityAccount, false);
			addToItemLog(logItem,
					MessageFormat.format(
							"Identity-account relation deleted (without call delete provisioning) (username: {0}, id: {1})",
							identityAccount.getIdentity(), identityAccount.getId()));
			UUID identityRole = identityAccount.getIdentityRole();

			if (removeIdentityRole && identityRole != null) {
				// We will remove connected identity role
				identityRoleService.deleteById(identityRole);
				addToItemLog(logItem, MessageFormat.format("Identity-role relation deleted (id: {0})", identityRole));
			}

		});
		return;
	}

	@Override
	public boolean supports(SystemEntityType delimiter) {
		return SystemEntityType.IDENTITY == delimiter;
	}

	@Override
	protected AbstractEntity findEntityById(UUID entityId, SystemEntityType entityType) {
		return identityRepository.findOne(entityId);
	}

	@Override
	protected EntityAccountFilter createEntityAccountFilter() {
		return new IdentityAccountFilter();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityAccountService() {
		return identityAccoutnService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccIdentityAccountDto();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityService() {
		return null; // We don't have DTO service for IdmIdentity now.
	}

	@Override
	protected List<? extends AbstractEntity> findAllEntity() {
		return Lists.newArrayList(identityRepository.findAll());
	}

	@Override
	protected Class<? extends FormableEntity> getEntityClass() {
		return IdmIdentity.class;
	}

	@Override
	protected BaseEntityRepository getRepository() {
		return this.identityRepository;
	}

	@Override
	protected CorrelationFilter getEntityFilter() {
		return new IdentityFilter();
	}

	@Override
	protected AbstractEntity findEntityByAttribute(String idmAttributeName, String value) {
		CorrelationFilter filter = getEntityFilter();
		filter.setProperty(idmAttributeName);
		filter.setValue(value);
		List<IdmIdentityDto> entities = identityService.find((IdentityFilter) filter, null).getContent();
		
		if (CollectionUtils.isEmpty(entities)) {
			return null;
		}
		if (entities.size() > 1) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_CORRELATION_TO_MANY_RESULTS,
					ImmutableMap.of("correlationAttribute", idmAttributeName, "value", value));
		}
		if (entities.size() == 1) {
			return identityRepository.findOne(entities.get(0).getId());
		}
		return null;
	}
}
