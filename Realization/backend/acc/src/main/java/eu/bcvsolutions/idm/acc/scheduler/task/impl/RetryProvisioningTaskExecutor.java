package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import org.joda.time.DateTime;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Retry provisioning executor
 * 
 * @author Radek Tomiška
 *
 */
@Service
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Description("Retry provisioning periodically")
public class RetryProvisioningTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RetryProvisioningTaskExecutor.class);
	@Autowired private ProvisioningExecutor provisioningExecutor;	
	@Autowired private SysProvisioningBatchService provisioningBatchService;
	//
	private DateTime start;	
	
	@Override
	protected boolean start() {
		start = new DateTime();
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
			Page<SysProvisioningBatchDto> batches = provisioningBatchService.findBatchesToRetry(start, new PageRequest(0, 100));
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
}
