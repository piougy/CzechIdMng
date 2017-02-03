package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmLongRunningTaskRepository;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.api.LongRunningTaskExecutor;

/**
 * Persists long running tasks
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmLongRunningTaskService extends AbstractReadWriteEntityService<IdmLongRunningTask, LongRunningTaskFilter> implements IdmLongRunningTaskService {
	
	private final IdmLongRunningTaskRepository repository;
	private final Executor executor;
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	public DefaultIdmLongRunningTaskService(
			IdmLongRunningTaskRepository repository,
			Executor executor) {
		super(repository);
		//
		Assert.notNull(executor);
		//
		this.repository = repository;
		this.executor = executor;
	}
	
	/**
	 * cancel all previously runned tasks
	 */
	@Transactional
	@PostConstruct
	public void init() {
		repository.findAllByInstanceIdAndResult_State(getInstanceId(), OperationState.RUNNING).forEach(task -> {
			task.setRunning(false);
			task.setResult(new OperationResult.Builder(OperationState.CANCELED).build()); // TODO: result state canceled by restart
			save(task);
		});
	}
	
	/**
	 * Executes long running task on this instance
	 */
	@Override
	@Scheduled(fixedDelay = 60000)
	@SuppressWarnings("unchecked")
	public void processCreated() {
		repository.findAllByInstanceIdAndResult_State(getInstanceId(), OperationState.CREATED).forEach(task -> {
			LongRunningTaskExecutor taskExecutor;
			try {
				taskExecutor = (LongRunningTaskExecutor) Class.forName(task.getTaskType()).newInstance();
				AutowireHelper.autowire(taskExecutor);
				taskExecutor.setLongRunningTask(task);
				taskExecutor.init((Map<String, Object>) task.getTaskProperties());						
			} catch (ClassNotFoundException | InstantiationException  | IllegalAccessException ex) {
				throw new CoreException(ex); // TODO: result code exception
			}
			execute(taskExecutor);
		});
	}
	
	@Override
	public void execute(LongRunningTaskExecutor taskExecutor) {
		// autowire task properties
		AutowireHelper.autowire(taskExecutor);
		// execute
		executor.execute(taskExecutor);
	}

	@Override
	@Transactional
	public void cancel(UUID longRunningTaskId) {
		Assert.notNull(longRunningTaskId);
		IdmLongRunningTask task = get(longRunningTaskId);
		Assert.notNull(longRunningTaskId);
		//
		task.setRunning(false);
		// state will be set by task himself on cancel
		save(task);
	}
	
	@Override
	@Transactional
	public void interrupt(UUID longRunningTaskId) {
		Assert.notNull(longRunningTaskId);
		IdmLongRunningTask task = get(longRunningTaskId);
		Assert.notNull(longRunningTaskId);
		String instanceId = getInstanceId();
		if (!task.getInstanceId().equals(instanceId)) {
			throw new CoreException("Thread for task [" + longRunningTaskId +"] running on differrent instance [" + task.getInstanceId() + "], "
					+ "can not be interrupted from this instance [" + instanceId + "]"); 
			// TODO: result code ex - thread could running on different machine
		}
		if (!OperationState.RUNNING.equals(task.getResult().getState())) {
			throw new CoreException("Task not running"); // TODO: resultCode
		}
		//
		// interrupt thread
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread thread : threadSet) {
			if (thread.getId() == task.getThreadId()) {
				thread.interrupt();
				task.setRunning(false);
				task.setResult(new OperationResult.Builder(OperationState.CANCELED).build()); // TODO: result state canceled standardly
				save(task);
				return;
			}
		}
	}
	
	/**
	 * Returns server instance id
	 * 
	 * @return
	 */
	private String getInstanceId() {
		return configurationService.getValue(ConfigurationService.PROPERTY_APP_INSTANCE_ID, ConfigurationService.DEFAULT_PROPERTY_APP_INSTANCE_ID);
	}
}
