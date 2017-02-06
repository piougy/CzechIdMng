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

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
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

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultLongRunningTaskManager.class);
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
			ResultModel resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_CANCELED_BY_RESTART, 
					ImmutableMap.of(
							"taskId", task.getId(), 
							"taskType", task.getTaskType(),
							"instanceId", task.getInstanceId())); 
			
			task.setResult(new OperationResult.Builder(OperationState.CANCELED).setModel(resultModel).build());
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
				throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_NOT_FOUND, ex);
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
				throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_RUNNING, ImmutableMap.of("taskId", task.getId()));
			}
			if (!OperationState.isRunnable(task.getResultState())) {
				throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_PROCESSED, ImmutableMap.of("taskId", task.getId()));
			}
			if (!task.getInstanceId().equals(configurationService.getInstanceId())) {
				throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_DIFFERENT_INSTANCE, 
						ImmutableMap.of("taskId", task.getId(), "taskInstanceId", task.getInstanceId(), "currentInstanceId", configurationService.getInstanceId()));
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
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_DIFFERENT_INSTANCE, 
					ImmutableMap.of("taskId", longRunningTaskId, "taskInstanceId", task.getInstanceId(), "currentInstanceId", instanceId));
		}
		if (!OperationState.RUNNING.equals(task.getResult().getState())) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_NOT_RUNNING, ImmutableMap.of("taskId", longRunningTaskId));
		}
		//
		// interrupt thread
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread thread : threadSet) {
			if (thread.getId() == task.getThreadId()) {
				ResultModel resultModel = null;
				Exception ex = null;
				try {
					thread.interrupt();
				} catch(Exception e) {
					resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_INTERRUPT, 
							ImmutableMap.of(
									"taskId", task.getId(), 
									"taskType", task.getTaskType(),
									"instanceId", task.getInstanceId()));
					ex = e;
					LOG.error(resultModel.toString(), e);
				}
				task.setRunning(false);
				if (resultModel == null) {
					task.setResult(new OperationResult.Builder(OperationState.CANCELED).build());
				} else {
					task.setResult(new OperationResult.Builder(OperationState.CANCELED).setModel(resultModel).setCause(ex).build());
				}				
				service.save(task);
				return;
			}
		}
	}
}
