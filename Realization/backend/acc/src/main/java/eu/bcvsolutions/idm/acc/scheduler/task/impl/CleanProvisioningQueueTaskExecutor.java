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
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * LRT will clean queue for provisioning
 * 
 * @author Patrik Stloukal
 *
 */
@Component
@Description("Clean provisioning queue for given filter.")
public class CleanProvisioningQueueTaskExecutor
		extends AbstractSchedulableStatefulExecutor<SysProvisioningOperationDto> {

	@Autowired
	private SysProvisioningBatchService provisioningBatchService;
	@Autowired
	private SysProvisioningOperationService provisioningOperationService;

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
			SysProvisioningOperationFilter filterOperation = new SysProvisioningOperationFilter();
			filterOperation.setBatchId(batch.getId());
			Page<SysProvisioningOperationDto> page = provisioningOperationService.find(filterOperation, null);
			for (SysProvisioningOperationDto operation : page) {
				provisioningOperationService.delete(operation);
			}
		}
		//
		return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
	}
}
