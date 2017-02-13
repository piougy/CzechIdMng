package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

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
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Default implementation {@link LongRunningTaskManager}
 * 
 * TODO: long running task interface only + AOP wrapper for long running task executor
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
	private final SecurityService securityService;
	
	@Autowired
	public DefaultLongRunningTaskManager(
			IdmLongRunningTaskService service,
			Executor executor,
			ConfigurationService configurationService,
			SecurityService securityService) {
		Assert.notNull(service);
		Assert.notNull(executor);
		Assert.notNull(configurationService);
		Assert.notNull(securityService);
		//
		this.service = service;
		this.executor = executor;
		this.configurationService = configurationService;
		this.securityService = securityService;
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
	
	@Override
	@Scheduled(fixedDelayString = "${scheduler.task.queue.process:60000}")
	public void scheduleProcessCreated() {
		processCreated();
	}
	
	/**
	 * Executes long running task on this instance
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<LongRunningFutureTask<?>> processCreated() {
		// run as system - called from scheduler internally
		securityService.setSystemAuthentication();
		//
		List<LongRunningFutureTask<?>> taskList = new ArrayList<LongRunningFutureTask<?>>();
		service.getTasks(configurationService.getInstanceId(), OperationState.CREATED).forEach(task -> {
			LongRunningTaskExecutor<?> taskExecutor = null;
			ResultModel resultModel = null;
			Exception ex = null;
			//
			try {
				taskExecutor = (LongRunningTaskExecutor<?>) AutowireHelper.createBean(Class.forName(task.getTaskType()));
				taskExecutor.setLongRunningTaskId(task.getId());
				taskExecutor.init((Map<String, Object>) task.getTaskProperties());			
			} catch (ClassNotFoundException e) {
				ex = e;
				resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_NOT_FOUND, 
							ImmutableMap.of(
									"taskId", task.getId(), 
									"taskType", task.getTaskType(),
									"instanceId", task.getInstanceId()));
				
			} catch (Exception e) {
				ex = e;
				resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_INIT_FAILED, 
							ImmutableMap.of(
									"taskId", task.getId(), 
									"taskType", task.getTaskType(),
									"instanceId", task.getInstanceId()));
			}
			if (ex != null) {
				LOG.error(resultModel.toString(), ex);
				task.setResult(new OperationResult.Builder(OperationState.EXCEPTION).setModel(resultModel).setCause(ex).build());
				service.save(task);
			} else {
				taskList.add(execute(taskExecutor));
			}			
		});
		return taskList;
	}
	
	@Override
	public <V> LongRunningFutureTask<V> execute(LongRunningTaskExecutor<V> taskExecutor) {
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
		FutureTask<V> futureTask = new FutureTask<>(taskExecutor);
		executor.execute(futureTask);
		return new LongRunningFutureTask<>(taskExecutor, futureTask);
	}

	@Override
	@Transactional
	public void cancel(UUID longRunningTaskId) {
		Assert.notNull(longRunningTaskId);
		IdmLongRunningTask task = service.get(longRunningTaskId);
		Assert.notNull(longRunningTaskId);
		//
		if (OperationState.RUNNING != task.getResult().getState()) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_NOT_RUNNING, 
					ImmutableMap.of("taskId", longRunningTaskId, "taskType", task.getTaskType(), "instanceId", task.getInstanceId()));
		}
		//
		task.setResult(new OperationResult.Builder(OperationState.CANCELED).build());
		// running to false will be setted by task himself
		service.save(task);
	}
	
	@Override
	@Transactional
	public boolean interrupt(UUID longRunningTaskId) {
		Assert.notNull(longRunningTaskId);
		IdmLongRunningTask task = service.get(longRunningTaskId);
		Assert.notNull(longRunningTaskId);
		String instanceId = configurationService.getInstanceId();
		if (!task.getInstanceId().equals(instanceId)) {			
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_DIFFERENT_INSTANCE, 
					ImmutableMap.of("taskId", longRunningTaskId, "taskInstanceId", task.getInstanceId(), "currentInstanceId", instanceId));
		}
		if (OperationState.RUNNING != task.getResult().getState()) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_NOT_RUNNING, ImmutableMap.of("taskId", longRunningTaskId));
		}
		//
		// interrupt thread
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread thread : threadSet) {
			if (thread.getId() == task.getThreadId()) {
				ResultModel resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_INTERRUPT, 
						ImmutableMap.of(
								"taskId", task.getId(), 
								"taskType", task.getTaskType(),
								"instanceId", task.getInstanceId()));
				Exception ex = null;
				try {
					thread.interrupt();
				} catch(Exception e) {
					ex = e;
					LOG.error(resultModel.toString(), e);
				}
				task.setRunning(false);
				//
				if (ex == null) {
					task.setResult(new OperationResult.Builder(OperationState.CANCELED).setModel(resultModel).build());
				} else {
					task.setResult(new OperationResult.Builder(OperationState.EXCEPTION).setModel(resultModel).setCause(ex).build());
				}				
				service.save(task);
				return true;
			}
		}
		LOG.warn("Long ruuning task with id");
		return false;
	}
}
