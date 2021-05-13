package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Abstract class for provisioning operation retry and cancel. These two operation has same options.
 *
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
public abstract class AbstractProvisioningOperationRetryCancelBulkAction extends AbstractBulkAction<SysProvisioningOperationDto, SysProvisioningOperationFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractProvisioningOperationRetryCancelBulkAction.class);

	public static final String RETRY_WHOLE_BATCH_CODE = "retryWholeBatch";

	@Autowired
	protected SysProvisioningOperationService service;
	@Autowired
	protected ProvisioningExecutor provisioningExecutor;
	@Autowired
	protected SysProvisioningBatchService provisioningOperationBatchService;

	@Override
	public SysProvisioningOperationService getService() {
		return service;
	}

	@Override
	protected OperationResult processEntities(Collection<UUID> entitiesId) {
		for (UUID entityId : entitiesId) {
			SysProvisioningOperationDto dto = getService().get(entityId);
			if (dto == null) {
				dto = new SysProvisioningOperationDto(entityId);
				boolean processed = false;
				//
				// try to find provisioning operation by id => NotFound(Ignore) annotation will be effective
				SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
				filter.setId(entityId);
				List<SysProvisioningOperationDto> operations = getService().find(filter, PageRequest.of(0, 1)).getContent();
				if (operations.size() == 1) {
					SysProvisioningOperationDto invalidOperation = operations.get(0);
					if (invalidOperation.getSystemEntity() == null) {
						// FIXME: how to delete invalid provisioning operation ... ?
						LOG.warn("System entity for provisioning operation [{}] was already deleted. "
								+ "Operation cannot be executed or canceled. Operation can be deleted from database only.", 
								entityId);
						processed = true;
						//
						this.logItemProcessed(
								invalidOperation,
								new OperationResult
									.Builder(OperationState.NOT_EXECUTED)
									.setModel(new DefaultResultModel(
											AccResultCode.SYSTEM_ENTITY_NOT_FOUND,
											ImmutableMap.of("system", invalidOperation.getSystem())))
									.build()
						);
					}
				}
				//
				// There must be log item and update state, some provisioning operation are processed by 
				// batch and it not exists.
				if (processed) {
					// item logged above already
				} else if (!isRetryWholeBatchAttribute()) {
					this.logItemProcessed(dto, new OperationResult.Builder(OperationState.NOT_EXECUTED).build());
				} else {
					LOG.warn("Entity with id [{}] not found. The Entity will be skipped by batch processing.", entityId);
					//
					this.logItemProcessed(
							dto, 
							new OperationResult
								.Builder(OperationState.EXECUTED)
								.setModel(new DefaultResultModel(
										AccResultCode.PROVISONING_OPERATION_RETRY_CANCEL_NOT_FOUND,
										ImmutableMap.of("provisioningOperation", entityId.toString())))
								.build()
					);
				}
				if (!updateState()) {
					return new OperationResult.Builder(OperationState.CANCELED).build();
				}
				continue;
			}
			try {
				if (checkPermissionForEntity(dto)) {
					OperationResult result = processDto(dto);
					this.logItemProcessed(dto, result);
				} else {
					// check permission failed
					createPermissionFailedLog(dto);
				}
				//
				//
				if (!updateState()) {
					return new OperationResult.Builder(OperationState.CANCELED).build();
				}
			} catch (Exception ex) {
				// log failed result and continue
				LOG.error("Processing of entity [{}] failed.", entityId, ex);
				this.logItemProcessed(dto, new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build());
				if (!updateState()) {
					return new OperationResult.Builder(OperationState.CANCELED).setCause(ex).build();
				}
			}
		}
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(getRetryWholeBatchAttribute());
		return formAttributes;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.PROVISIONING_OPERATION_UPDATE);
	}

	/**
	 * Return {@link IdmFormAttributeDto} for checkbox retry/cancel whole queue
	 *
	 * @return
	 */
	private IdmFormAttributeDto getRetryWholeBatchAttribute() {
		IdmFormAttributeDto attribute = new IdmFormAttributeDto(
				RETRY_WHOLE_BATCH_CODE, 
				RETRY_WHOLE_BATCH_CODE, 
				PersistentType.BOOLEAN);
		attribute.setDefaultValue(Boolean.TRUE.toString());
		return attribute;
	}

	/**
	 * Return if bulk operation must process only selected items or whole batch
	 *
	 * @return
	 */
	protected boolean isRetryWholeBatchAttribute() {
		Boolean approve = this.getParameterConverter().toBoolean(getProperties(), RETRY_WHOLE_BATCH_CODE);
		return approve != null ? approve.booleanValue() : true;
	}
}
