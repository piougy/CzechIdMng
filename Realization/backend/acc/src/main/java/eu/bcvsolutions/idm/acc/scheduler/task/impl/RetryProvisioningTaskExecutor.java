package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.util.Map;

import org.joda.time.DateTime;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

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
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		start = new DateTime();
		LOG.debug("Retry provisioning executor was inintialized for all next attmepts old than [{}]", start);
	}
	
	@Override
	public Boolean process() {
		LOG.info("Retry provisioning executor starts for all next attmepts old than [{}]", start);
		counter = 0L;
		boolean canContinue = true;
		while(canContinue) {
			// we process all batches
			Page<SysProvisioningBatch> batches = provisioningBatchService.findBatchesToRetry(start, new PageRequest(0, 100));
			// init count
			if (count == null) {
				count = batches.getTotalElements();
			}
			//
			for(SysProvisioningBatch batch : batches) {
				provisioningExecutor.execute(batch);
				counter++;
				canContinue = updateState();
				if (!canContinue) {
					break;
				}
			}
			if (!batches.hasNext()) {
				break;
			}
		}
		LOG.info("Retry provisioning executor ended for all next attmepts old than [{}]", start);
		return Boolean.TRUE;
	}
}
