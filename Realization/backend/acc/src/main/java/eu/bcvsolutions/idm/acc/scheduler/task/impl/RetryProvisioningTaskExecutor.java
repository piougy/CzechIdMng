package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.time.ZonedDateTime;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Retry provisioning executor.
 * 
 * @author Radek Tomiška
 *
 */
@Component(RetryProvisioningTaskExecutor.TASK_NAME)
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Description("Retry provisioning periodically.")
public class RetryProvisioningTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RetryProvisioningTaskExecutor.class);
	public static final String TASK_NAME = "acc-retry-provisioning-long-running-task";
	//
	@Autowired private ProvisioningExecutor provisioningExecutor;	
	@Autowired private SysProvisioningBatchService provisioningBatchService;
	//
	private ZonedDateTime start;	
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	protected boolean start() {
		start = ZonedDateTime.now();
		LOG.debug("Retry provisioning executor was inintialized for all next attmepts old than [{}]", start);
		//
		return super.start();
	}
	
	@Override
	public Boolean process() {
		LOG.info("Retry provisioning executor starts for all next attmepts old than [{}]", start);
		counter = 0L;
		boolean canContinue = true;
		while(canContinue) {
			// we process all batches
			Page<SysProvisioningBatchDto> batches = provisioningBatchService.findBatchesToRetry(start, PageRequest.of(0, 100));
			// init count
			if (count == null) {
				count = batches.getTotalElements();
			}
			//
			for(SysProvisioningBatchDto batch : batches) {
				try {
					this.logItemProcessed(batch, provisioningExecutor.execute(batch));
					counter++;
					canContinue = updateState();
				} catch (Exception ex) {
					// TODO: redesign Boolean result to OperationResult
					LOG.error("Batch [{}] execution failed", batch.getId(), ex);
				}
				if (!canContinue) {
					break;
				}
			}
			if (!batches.hasNext()) {
				break;
			}
		}
		LOG.info("Retry provisioning executor ended for all next attempts old than [{}]", start);
		return Boolean.TRUE;
	}
	
	@Override
    public boolean isRecoverable() {
    	return true;
    }
}
