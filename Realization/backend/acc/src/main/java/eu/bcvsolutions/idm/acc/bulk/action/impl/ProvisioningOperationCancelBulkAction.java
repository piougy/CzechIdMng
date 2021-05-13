package eu.bcvsolutions.idm.acc.bulk.action.impl;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation_;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Bulk operation to cancel given provisioning operation.
 *
 * @author Ondrej Kopr
 *
 */
@Enabled(AccModuleDescriptor.MODULE_ID)
@Component("provisioningOperationCancelBulkAction")
@Description("Bulk operation to cancel given provisioning operation.")
public class ProvisioningOperationCancelBulkAction extends AbstractProvisioningOperationRetryCancelBulkAction {

	public static final String NAME = "provisioning-operation-cancel-bulk-action";

	@Override
	protected OperationResult processDto(SysProvisioningOperationDto dto) {
		if (isRetryWholeBatchAttribute()) {
			SysProvisioningBatchDto batch = DtoUtils.getEmbedded(dto, SysProvisioningOperation_.batch, SysProvisioningBatchDto.class, null);
			if (batch == null) {
				batch = provisioningOperationBatchService.get(dto.getBatch());
			}

			provisioningExecutor.cancel(batch);
		} else {
			provisioningExecutor.cancel(dto);
		}
		return new OperationResult(OperationState.EXECUTED);
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getOrder() {
		return 100;
	}
	
	@Override
	public NotificationLevel getLevel() {
		return NotificationLevel.WARNING;
	}
}
