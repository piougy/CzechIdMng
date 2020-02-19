package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.util.Optional;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation_;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * LRT will cancel queue for provisioning.
 * 
 * @author Patrik Stloukal
 * @author Radek Tomiška
 * @deprecated @since 10.2.0 - not used from devstack. use {@link ProvisioningOperationCancelBulkAction}.
 */
@Deprecated
@Component
@DisallowConcurrentExecution
@Description("Cancel all operations in provisioning queue.")
public class CancelProvisioningQueueTaskExecutor
		extends AbstractSchedulableStatefulExecutor<SysProvisioningOperationDto> {

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
		return provisioningOperationService.find(filter, PageRequest.of(0, pageable.getPageSize())); // new pageable is given => records are deleted and we need the first page all time
	}

	@Override
	public Optional<OperationResult> processItem(SysProvisioningOperationDto dto) {
		//
		if (dto.getBatch() == null) {
			// cancel single operation - batch was deleted for some reason (previously saved operations without batch, truncate in database ...)
			provisioningExecutor.cancel(dto);
			//
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		}
		SysProvisioningBatchDto batch = DtoUtils.getEmbedded(dto, SysProvisioningOperation_.batch, (SysProvisioningBatchDto) null);
	    if (batch == null) {
	    	batch = provisioningBatchService.get(dto.getBatch());
	    }
		//
		provisioningExecutor.cancel(batch);
		//
		return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
	}
}
