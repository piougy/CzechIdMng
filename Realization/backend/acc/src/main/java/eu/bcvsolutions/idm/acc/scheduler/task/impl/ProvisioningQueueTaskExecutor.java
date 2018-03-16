package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Process provisioning operations in queue periodically
 * 
 * @author Radek Tomiška
 *
 */
@Service
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Description("Process provisioning operations in queue periodically")
public class ProvisioningQueueTaskExecutor extends AbstractSchedulableStatefulExecutor<SysProvisioningBatchDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningQueueTaskExecutor.class);
	private static final String PARAMETER_VIRTUAL = "virtualSystem";
	//
	@Autowired private ProvisioningExecutor provisioningExecutor;	
	@Autowired private SysProvisioningBatchService provisioningBatchService;
	//
	private Boolean virtualSystem; // configured virtual system
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		virtualSystem = getParameterConverter().toBoolean(properties, PARAMETER_VIRTUAL);
	}
	
	@Override
	protected boolean start() {
		LOG.debug("Start processing created provisioning operation in queue.");
		//
		return super.start();
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		result = super.end(result, ex);
		LOG.debug("End processing created provisioning operation in queue.");
		return result;
	}

	@Override
	public Page<SysProvisioningBatchDto> getItemsToProcess(Pageable pageable) {
		// TODO: we want to execute provisioning in more threads - we aren't using pagination here to prevent collisions
		// TODO: we can add Sort by some priority (CREATE ... etc.)
		// TODO: we can add algorithm to reduce / merge provisioning operations by system entity
		return provisioningBatchService.findBatchesToProcess(virtualSystem, null);
	}

	@Override
	public Optional<OperationResult> processItem(SysProvisioningBatchDto dto) {
		LOG.debug("Start processinig created batch [{}] from queue.",  dto.getId());
		try {
			// TODO: multi thread
//			FutureTask<OperationResult> futureTask = new FutureTask<>(new Callable<OperationResult>() {
//				@Override
//				public OperationResult call() throws Exception {
//					LOG.error(".... +: " + dto.getId());
//					return provisioningExecutor.execute(dto);
//				}				
//			});
//			executor.execute(futureTask);
//			return Optional.of(new OperationResult.Builder(OperationState.RUNNING).build());
			return Optional.of(provisioningExecutor.execute(dto));		
		} catch (Exception ex) {
			// just for sure - execute should return appropriate result always
			LOG.error("Process [{}] batch from queue failed", dto.getId(), ex);
			return Optional.of(new OperationResult.Builder(OperationState.EXCEPTION)
					.setCause(ex)
					.build());
		} finally {
			LOG.debug("Created batch [{}] from queue was processed",  dto.getId());
		}
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_VIRTUAL);
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_VIRTUAL, virtualSystem);
		return properties;
	}
	
	public void setVirtual(boolean virtual) {
		this.virtualSystem = virtual;
	}
	
	public Boolean getVirtual() {
		return virtualSystem;
	}
}
