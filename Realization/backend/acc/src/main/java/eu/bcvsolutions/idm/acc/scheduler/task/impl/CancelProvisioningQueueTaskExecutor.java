package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * LRT will cancel queue for provisioning
 * 
 * @author Patrik Stloukal
 * @author Radek Tomiška
 *
 */
@Component
@Description("Cancel operations in provisioning queue for given filter.")
public class CancelProvisioningQueueTaskExecutor
		extends AbstractSchedulableStatefulExecutor<SysProvisioningOperationDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CancelProvisioningQueueTaskExecutor.class);
	//
	@Autowired private SysProvisioningBatchService provisioningBatchService;
	@Autowired private SysProvisioningOperationService provisioningOperationService;
	@Autowired private ProvisioningExecutor provisioningExecutor;
	//
	private SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();

	public SysProvisioningOperationFilter getFilter() {
		return filter;
	}

	public void setFilter(SysProvisioningOperationFilter filter) {
		this.filter = filter;
	}

	@Override
	public Page<SysProvisioningOperationDto> getItemsToProcess(Pageable pageable) {
		return provisioningOperationService.find(filter, null);
	}

	@Override
	public Optional<OperationResult> processItem(SysProvisioningOperationDto dto) {
		//
		SysProvisioningBatchDto batch = provisioningBatchService.get(dto.getBatch());
		//
		if (batch != null) {
			provisioningExecutor.cancel(batch);
			//
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		}
		//
		LOG.debug("Batch [{}] not found, cannot be cancelled twice.", dto.getBatch());
		// TODO: not executed with appropriate code
		return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
	}
}
