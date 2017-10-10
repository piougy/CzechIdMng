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
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccContractAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccContractAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccContractAccountService;
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
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

@Component
public class ContractSynchronizationExecutor extends AbstractSynchronizationExecutor<IdmIdentityContractDto>
		implements SynchronizationEntityExecutor {

	private final IdmIdentityContractService contractService;
	private final AccContractAccountService contractAccoutnService;
	public final static String ROLE_TYPE_FIELD = "roleType";

	@Autowired
	public ContractSynchronizationExecutor(IcConnectorFacade connectorFacade, SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService, SysSyncLogService synchronizationLogService,
			SysSyncActionLogService syncActionLogService, AccAccountService accountService,
			SysSystemEntityService systemEntityService, ConfidentialStorage confidentialStorage,
			FormService formService, IdmIdentityContractService contractService,
			AccContractAccountService contractAccoutnService, SysSyncItemLogService syncItemLogService,
			EntityEventManager entityEventManager, GroovyScriptService groovyScriptService,
			WorkflowProcessInstanceService workflowProcessInstanceService, EntityManager entityManager,
			SysSystemMappingService systemMappingService, SysSchemaObjectClassService schemaObjectClassService,
			SysSchemaAttributeService schemaAttributeService) {
		super(connectorFacade, systemService, attributeHandlingService, synchronizationConfigService,
				synchronizationLogService, syncActionLogService, accountService, systemEntityService,
				confidentialStorage, formService, syncItemLogService, entityEventManager, groovyScriptService,
				workflowProcessInstanceService, entityManager, systemMappingService, schemaObjectClassService,
				schemaAttributeService);
		//
		Assert.notNull(contractService, "Contract service is mandatory!");
		Assert.notNull(contractAccoutnService, "Contract-account service is mandatory!");
		//
		this.contractService = contractService;
		this.contractAccoutnService = contractAccoutnService;
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
		IdmIdentityContractDto entity = null;
		if (entityId != null) {
			entity = contractService.get(entityId);
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
	protected void callProvisioningForEntity(IdmIdentityContractDto entity, SystemEntityType entityType,
			SysSyncItemLogDto logItem) {
		addToItemLog(logItem,
				MessageFormat.format(
						"Call provisioning (process IdentityContractEvent.UPDATE) for contract ({0}) with position ({1}).",
						entity.getId(), entity.getPosition()));
		entityEventManager.process(new IdentityContractEvent(IdentityContractEventType.UPDATE, entity)).getContent();
	}

	/**
	 * Operation remove IdentityContractAccount relations and linked roles
	 * 
	 * @param account
	 * @param removeIdentityContractIdentityContract
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUnlink(AccAccountDto account, boolean removeIdentityContractIdentityContract, SysSyncLogDto log,
			SysSyncItemLogDto logItem, List<SysSyncActionLogDto> actionLogs) {

		EntityAccountFilter entityAccountFilter = new AccContractAccountFilter();
		entityAccountFilter.setAccountId(account.getId());
		List<AccContractAccountDto> entityAccounts = contractAccoutnService
				.find((AccContractAccountFilter) entityAccountFilter, null).getContent();
		if (entityAccounts.isEmpty()) {
			addToItemLog(logItem, "Contract-account relation was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		addToItemLog(logItem, MessageFormat.format("Contract-account relations to delete {0}", entityAccounts));

		entityAccounts.stream().forEach(entityAccount -> {
			// We will remove contract account, but without delete connected
			// account
			contractAccoutnService.delete(entityAccount, false);
			addToItemLog(logItem,
					MessageFormat.format(
							"Contract-account relation deleted (without call delete provisioning) (contract id: {0}, contract-account id: {1})",
							entityAccount.getContract(), entityAccount.getId()));

		});
		return;
	}

	@Override
	protected Object getValueByMappedAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes) {
		Object transformedValue = super.getValueByMappedAttribute(attribute, icAttributes);
		// Transform role type enumeration from string
		// if (transformedValue instanceof String &&
		// attribute.isEntityAttribute()
		// && ROLE_TYPE_FIELD.equals(attribute.getIdmPropertyName())) {
		// transformedValue = IdentityContractType.valueOf((String)
		// transformedValue);
		// }
		return transformedValue;
	}

	/**
	 * Save entity
	 * 
	 * @param entity
	 * @param skipProvisioning
	 * @return
	 */
	@Override
	protected IdmIdentityContractDto save(IdmIdentityContractDto entity, boolean skipProvisioning) {
		// Content will be set in service (we need do transform entity to DTO).
		// Here we set only dummy dto (null content is not allowed)
		EntityEvent<IdmIdentityContractDto> event = new IdentityContractEvent(
				contractService.isNew(entity) ? IdentityContractEventType.CREATE : IdentityContractEventType.UPDATE,
				entity, ImmutableMap.of(ProvisioningService.SKIP_PROVISIONING, skipProvisioning));

		return contractService.publish(event).getContent();
	}

	@Override
	protected EntityAccountFilter createEntityAccountFilter() {
		return new AccContractAccountFilter();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityAccountService() {
		return contractAccoutnService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccContractAccountDto();
	}

	@Override
	protected IdmIdentityContractDto createEntityDto() {
		return new IdmIdentityContractDto();
	}

	@Override
	protected IdmIdentityContractService getService() {
		return contractService;
	}

	@Override
	protected Class<? extends FormableEntity> getEntityClass() {
		return IdmIdentityContract.class;
	}

	@Override
	protected CorrelationFilter getEntityFilter() {
		return new IdmIdentityContractFilter();
	}

	@Override
	protected IdmIdentityContractDto findByAttribute(String idmAttributeName, String value) {
		CorrelationFilter filter = getEntityFilter();
		filter.setProperty(idmAttributeName);
		filter.setValue(value);

		List<IdmIdentityContractDto> entities = contractService.find((IdmIdentityContractFilter) filter, null)
				.getContent();

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
}
