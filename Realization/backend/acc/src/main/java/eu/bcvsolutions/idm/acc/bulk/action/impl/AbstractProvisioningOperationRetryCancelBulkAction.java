package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Abstract class for provisioning operation retry and cancel. These two operation has same options.
 *
 * @author Ondrej Kopr
 *
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
				LOG.warn("Entity with id [{}] not found. The Entity will be skipped.", entityId);
				dto = new SysProvisioningOperationDto();
				dto.setId(entityId);

				// There must be log item and update state, some provisioning operation are processed by 
				// batch and it not exists
				this.logItemProcessed(dto, new OperationResult.Builder(OperationState.NOT_EXECUTED).build());
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
