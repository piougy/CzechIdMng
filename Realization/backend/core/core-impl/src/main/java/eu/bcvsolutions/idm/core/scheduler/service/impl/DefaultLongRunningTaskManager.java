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
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;

/**
 * Default implementation {@link LongRunningTaskManager}
 * 
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultLongRunningTaskManager implements LongRunningTaskManager {

	private final IdmLongRunningTaskService service;
	private final Executor executor;
	private final ConfigurationService configurationService;
	
	@Autowired
	public DefaultLongRunningTaskManager(
			IdmLongRunningTaskService service,
			Executor executor,
			ConfigurationService configurationService) {
		Assert.notNull(service);
		Assert.notNull(executor);
		Assert.notNull(configurationService);
		//
		this.service = service;
		this.executor = executor;
		this.configurationService = configurationService;
	}
	
	/**
	 * cancel all previously runned tasks
	 */
	@Transactional
	@PostConstruct
	public void init() {
		service.getTasks(configurationService.getInstanceId(), OperationState.RUNNING).forEach(task -> {
			task.setRunning(false);
			task.setResult(new OperationResult.Builder(OperationState.CANCELED).build()); // TODO: result state canceled by restart
			service.save(task);
		});
	}
	
	/**
	 * Executes long running task on this instance
	 */
	@Override
	@Scheduled(fixedDelay = 10000)
	@SuppressWarnings("unchecked")
	public void processCreated() {
		service.getTasks(configurationService.getInstanceId(), OperationState.CREATED).forEach(task -> {
			LongRunningTaskExecutor taskExecutor;
			try {
				taskExecutor = (LongRunningTaskExecutor) Class.forName(task.getTaskType()).newInstance();
				AutowireHelper.autowire(taskExecutor);
				taskExecutor.setLongRunningTaskId(task.getId());
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
		// prepare task
		if (taskExecutor.getLongRunningTaskId() == null) {
			IdmLongRunningTask task = new IdmLongRunningTask();
			task.setTaskType(taskExecutor.getClass().getCanonicalName());
			task.setTaskDescription(taskExecutor.getDescription());	
			task.setInstanceId(configurationService.getInstanceId());
			task.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
			taskExecutor.setLongRunningTaskId(service.save(task).getId());
		} else {
			IdmLongRunningTask task = service.get(taskExecutor.getLongRunningTaskId());
			Assert.notNull(task);
			if (task.isRunning()) {
				// TODO: result code exception
				return;
			}
			if (!OperationState.isRunnable(task.getResultState())) {
				// TODO: result code exception
				return;
			}
			if (!task.getInstanceId().equals(configurationService.getInstanceId())) {
				// TODO: result code exception
				return;
			}
		}		
		// execute
		executor.execute(taskExecutor);
	}

	@Override
	@Transactional
	public void cancel(UUID longRunningTaskId) {
		Assert.notNull(longRunningTaskId);
		IdmLongRunningTask task = service.get(longRunningTaskId);
		Assert.notNull(longRunningTaskId);
		//
		task.setResult(new OperationResult.Builder(OperationState.CANCELED).build());
		// running to false will be setted by task himself
		service.save(task);
	}
	
	@Override
	@Transactional
	public void interrupt(UUID longRunningTaskId) {
		Assert.notNull(longRunningTaskId);
		IdmLongRunningTask task = service.get(longRunningTaskId);
		Assert.notNull(longRunningTaskId);
		String instanceId = configurationService.getInstanceId();
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
				service.save(task);
				return;
			}
		}
	}
}
