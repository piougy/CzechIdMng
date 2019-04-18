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
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Bulk action to retry given provisioning operation
 *
 * @author Ondrej Kopr
 *
 */

@Enabled(AccModuleDescriptor.MODULE_ID)
@Component("provisioningOperationRetryBulkAction")
@Description("Bulk operation to retry given provisioning operation.")
public class ProvisioningOperationRetryBulkAction extends AbstractProvisioningOperationRetryCancelBulkAction {

	public static final String NAME = "provisioning-operation-retry-bulk-action";

	@Override
	protected OperationResult processDto(SysProvisioningOperationDto dto) {
		if (isRetryWholeBatchAttribute()) {
			SysProvisioningBatchDto batch = DtoUtils.getEmbedded(dto, SysProvisioningOperation_.batch, SysProvisioningBatchDto.class, null);
			if (batch == null) {
				batch = provisioningOperationBatchService.get(dto.getBatch());
			}

			provisioningExecutor.execute(batch);
		} else {
			provisioningExecutor.execute(dto);
		}
		return new OperationResult(OperationState.EXECUTED);
	}
	

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
