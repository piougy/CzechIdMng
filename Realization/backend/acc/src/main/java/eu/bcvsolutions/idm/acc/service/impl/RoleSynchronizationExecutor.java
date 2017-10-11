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

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccRoleAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

@Component
public class RoleSynchronizationExecutor extends AbstractSynchronizationExecutor<IdmRoleDto>
		implements SynchronizationEntityExecutor {

	private final IdmRoleService roleService;
	private final AccRoleAccountService roleAccoutnService;
	public final static String ROLE_TYPE_FIELD = "roleType";

	@Autowired
	public RoleSynchronizationExecutor(
			IcConnectorFacade connectorFacade, 
			SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService, 
			SysSyncLogService synchronizationLogService,
			SysSyncActionLogService syncActionLogService, 
			AccAccountService accountService,
			SysSystemEntityService systemEntityService, 
			ConfidentialStorage confidentialStorage,
			FormService formService, 
			IdmRoleService roleService,
			AccRoleAccountService roleAccoutnService, 
			SysSyncItemLogService syncItemLogService,
			EntityEventManager entityEventManager,
			GroovyScriptService groovyScriptService, 
			WorkflowProcessInstanceService workflowProcessInstanceService,
			EntityManager entityManager,
			SysSystemMappingService systemMappingService,
			SysSchemaObjectClassService schemaObjectClassService,
			SysSchemaAttributeService schemaAttributeService) {
		super(connectorFacade, systemService, attributeHandlingService, synchronizationConfigService,
				synchronizationLogService, syncActionLogService, accountService, systemEntityService,
				confidentialStorage, formService, syncItemLogService, entityEventManager, groovyScriptService,
				workflowProcessInstanceService, entityManager, systemMappingService,
				schemaObjectClassService, schemaAttributeService);
		//
		Assert.notNull(roleService, "Identity service is mandatory!");
		Assert.notNull(roleAccoutnService, "Identity account service is mandatory!");
		//
		this.roleService = roleService;
		this.roleAccoutnService = roleAccoutnService;
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
	protected void doUpdateAccount(AccAccountDto account, SystemEntityType entityType, SysSyncLogDto log,
			SysSyncItemLogDto logItem, List<SysSyncActionLogDto> actionLogs) {
		UUID entityId = getEntityByAccount(account.getId());
		IdmRoleDto entity = null;
		if (entityId != null) {
			entity = roleService.get(entityId);
		}
		if (entity == null) {
			addToItemLog(logItem, "Entity account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		// Call provisioning for this entity
		callProvisioningForEntity(entity, entityType, logItem);
	}

	/**
	 * Call provisioning for given account
	 * 
	 * @param entity
	 * @param entityType
	 * @param logItem
	 */
	@Override
	protected void callProvisioningForEntity(IdmRoleDto entity, SystemEntityType entityType, SysSyncItemLogDto logItem) {
		addToItemLog(logItem,
				MessageFormat.format(
						"Call provisioning (process RoleEventType.SAVE) for role ({0}) with username ({1}).",
						entity.getId(), entity.getName()));
		entityEventManager.process(new RoleEvent(RoleEventType.UPDATE, entity)).getContent();
	}


	/**
	 * Operation remove RoleAccount relations and linked roles
	 * 
	 * @param account
	 * @param removeRoleRole
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUnlink(AccAccountDto account, boolean removeRoleRole, SysSyncLogDto log, SysSyncItemLogDto logItem,
			List<SysSyncActionLogDto> actionLogs) {

		EntityAccountFilter roleAccountFilter = new AccRoleAccountFilter();
		roleAccountFilter.setAccountId(account.getId());
		List<AccRoleAccountDto> roleAccounts = roleAccoutnService
				.find((AccRoleAccountFilter) roleAccountFilter, null).getContent();
		if (roleAccounts.isEmpty()) {
			addToItemLog(logItem, "Role account relation was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		addToItemLog(logItem, MessageFormat.format("Role-account relations to delete {0}", roleAccounts));

		roleAccounts.stream().forEach(roleAccount -> {
			// We will remove role account, but without delete connected
			// account
			roleAccoutnService.delete(roleAccount, false);
			addToItemLog(logItem,
					MessageFormat.format(
							"Role-account relation deleted (without call delete provisioning) (username: {0}, id: {1})",
							roleAccount.getRole(), roleAccount.getId()));

		});
		return;
	}
	
	@Override
	protected Object getValueByMappedAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes, SynchronizationContext context) {
		Object transformedValue =  super.getValueByMappedAttribute(attribute, icAttributes, context);
		// Transform role type enumeration from string
		if (transformedValue instanceof String && attribute.isEntityAttribute() && ROLE_TYPE_FIELD.equals(attribute.getIdmPropertyName())) {
			transformedValue = RoleType.valueOf((String) transformedValue);
		}
		return transformedValue;
	}
	
	/**
	 * Save entity
	 * @param entity
	 * @param skipProvisioning
	 * @return
	 */
	@Override
	protected IdmRoleDto save(IdmRoleDto entity, boolean skipProvisioning) {		
		// Content will be set in service (we need do transform entity to DTO). 
		// Here we set only dummy dto (null content is not allowed)
		EntityEvent<IdmRoleDto> event = new RoleEvent(
				roleService.isNew(entity) ? RoleEventType.CREATE : RoleEventType.UPDATE, 
				entity, 
				ImmutableMap.of(ProvisioningService.SKIP_PROVISIONING, skipProvisioning));
		
		return roleService.publish(event).getContent();
	}

	@Override
	protected EntityAccountFilter createEntityAccountFilter() {
		return new AccRoleAccountFilter();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityAccountService() {
		return roleAccoutnService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccRoleAccountDto();
	}

	@Override
	protected IdmRoleService getService() {
		return roleService;
	}

	@Override
	protected Class<? extends FormableEntity> getEntityClass() {
		return IdmRole.class;
	}

	@Override
	protected CorrelationFilter getEntityFilter() {
		return new IdmRoleFilter();
	}

	@Override
	protected IdmRoleDto findByAttribute(String idmAttributeName, String value) {
		CorrelationFilter filter = getEntityFilter();
		filter.setProperty(idmAttributeName);
		filter.setValue(value);
		
		List<IdmRoleDto> entities = roleService.find((IdmRoleFilter) filter, null).getContent();
		
		if (CollectionUtils.isEmpty(entities)) {
			return null;
		}
		if (entities.size() > 1) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_CORRELATION_TO_MANY_RESULTS,
					ImmutableMap.of("correlationAttribute", idmAttributeName, "value", value));
		}
		if (entities.size() == 1) {
			return entities.get(0);
		}
		return null;
	}

	@Override
	protected IdmRoleDto createEntityDto() {
		return new IdmRoleDto();
	}
}
