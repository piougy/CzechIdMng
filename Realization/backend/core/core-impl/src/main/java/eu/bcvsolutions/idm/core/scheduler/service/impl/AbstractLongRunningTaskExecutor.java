package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;

/**
 * Template for long running task executor. This template persists long running tasks.
 * 
 * TODO: interface only + AOP executor
 * TODO: refactor autowired fields
 * TODO: Configurable API?
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractLongRunningTaskExecutor<V> implements LongRunningTaskExecutor<V> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractLongRunningTaskExecutor.class);
	@Autowired protected IdmLongRunningTaskService service;
	@Autowired private LookupService entityLookupService;
	//
	private ParameterConverter parameterConverter;	
	private UUID taskId;
	protected Long count = null;
	protected Long counter = null;
	
	@Override
	public String getName() {
		return this.getClass().getCanonicalName();
	}
	
	/**
	 * Default implementation returns module by package conventions.
	 */
	@Override
	public String getModule() {
		return EntityUtils.getModule(this.getClass());
	}
	
	@Override
	public String getDescription() {
		return AutowireHelper.getBeanDescription(this.getClass());
	}
	
	/**
	 * Returns configurable task parameters. Don't forget to override this method additively.
	 */
	@Override
	public List<String> getPropertyNames() {
		// any parameter for now
		return new ArrayList<>();
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		count = null;
		counter = null;
	}
	
	/**
	 * Returns persistent task parameter values. Don't forget to override this method additively.
	 */
	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}
	
	/**
	 * Starts given task
	 * - persists task properties
	 * 
	 * @return
	 */
	protected boolean start() {
		Assert.notNull(taskId);
		IdmLongRunningTaskDto task = service.get(taskId);
		//
		validate(task);
		//
		Thread currentThread = Thread.currentThread();
		task.setThreadId(currentThread.getId());
		task.setThreadName(currentThread.getName());
		//
		setStateProperties(task);
		//
		task.setRunning(true);
		task.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		task.setStateful(isStateful());
		Map<String, Object> taskProperties = task.getTaskProperties();
		taskProperties.put(LongRunningTaskExecutor.PARAMETER_INSTANCE_ID, task.getInstanceId());
		taskProperties.putAll(getProperties());
		task.setTaskProperties(taskProperties);
		task.setStateful(isStateful());
		//
		service.save(task);
		return true;
	}
	
	/**
	 * Validates task before start e.q. if task already running or to prevent run task concurrently.
	 * 
	 * @param task persisted task to validate
	 */
	@Override
	public void validate(IdmLongRunningTaskDto task) {
		Assert.notNull(task, "Long running task has to be prepared before task is started");
		//
		if (task.isRunning()) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_RUNNING, ImmutableMap.of("taskId", task.getId()));
		}
		if (!OperationState.isRunnable(task.getResultState())) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_PROCESSED, ImmutableMap.of("taskId", task.getId()));
		}
		// 
		if (this.getClass().isAnnotationPresent(DisallowConcurrentExecution.class)) {
			IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
			filter.setTaskType(getName());
			filter.setRunning(Boolean.TRUE);
			if (service.find(filter, null).getTotalElements() > 0) {
				throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_RUNNING, ImmutableMap.of("taskId", getName()));
			}
		}
	}
	
	
	@Override
	public V call() {
		try {
			if (!start()) {
				return null;
			}
			V result = process();
			//
			return end(result, null);
		} catch (Exception ex) {
			return end(null, ex);
		}
	}
	
	/**
	 * TODO: save result into long running task - blob, text?
	 * 
	 * @param result
	 * @param ex
	 * @return
	 */
	protected V end(V result, Exception ex) {
		Assert.notNull(taskId);
		IdmLongRunningTaskDto task = service.get(taskId);
		Assert.notNull(task, "Long running task has to be prepared before task is ended");
		LOG.debug("Long running task ends [{}] with result [{}].", taskId, result);
		//
		setStateProperties(task);
		//
		if (ex != null) {
			ResultModel resultModel;
			if (ex instanceof ResultCodeException) {
				resultModel = ((ResultCodeException) ex).getError().getError();
			} else {
				resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_FAILED, 
					ImmutableMap.of(
							"taskId", taskId, 
							"taskType", task.getTaskType(),
							"instanceId", task.getInstanceId()));
			}
			LOG.error(resultModel.toString(), ex);
			task.setResult(new OperationResult.Builder(OperationState.EXCEPTION).setModel(resultModel).setCause(ex).build());
		} else if(OperationState.isRunnable(task.getResultState())) { 
			// executed standardly
			LOG.debug("Long running task ended [{}] standardly, previous state [{}], result [{}].", taskId, task.getResultState(), result);
			task.setResult(new OperationResult.Builder(OperationState.EXECUTED).build());
		}
		task.setRunning(false);
		service.save(task);
		//
		return result;
	}
	
	/**
	 * Override for count support
	 */
	@Override
	public Long getCount() {
		return count;
	}
	
	/**
	 * Override for counter support
	 */
	@Override
	public Long getCounter() {
		return counter;
	}
	
	/**
	 * Override for counter support
	 */
	@Override
	public void setCount(Long count) {
		this.count = count;
	}

	/**
	 * Override for counter support
	 */
	@Override
	public void setCounter(Long counter) {
		this.counter = counter;
	}
	
	@Override
	public Long increaseCounter(){
		return this.counter++;
	}

	@Override
	public boolean updateState() {
		// TODO: interface only + AOP => task can be ran directly without executor
		if (service == null || taskId == null) {
			return true;
		}
		service.updateState(taskId, count, counter);
		//
		IdmLongRunningTaskDto task = service.get(taskId);
		if (task == null) {
			return true;
		}
		return task.isRunning() && OperationState.isRunnable(task.getResultState());
	}
	
	@Override
	public boolean isStateful() {
		return true;
	}
	
	/**
	 * Return parameter converter helper
	 * 
	 * @return
	 */
	protected ParameterConverter getParameterConverter() {
		if (parameterConverter == null) {
			parameterConverter = new ParameterConverter(entityLookupService);
		}
		return parameterConverter;
	}
	
	/**
	 * Sets executor's state properties (count..) to long running task instance
	 * 
	 * @param longRunningTask
	 * @param taskExecutor
	 */
	private void setStateProperties(IdmLongRunningTaskDto task) {
		task.setCount(getCount());
		task.setCounter(getCounter());
		if (task.getCount() != null && task.getCounter() == null) {
			task.setCounter(0L);
		}
	}
	
	@Override
	public UUID getLongRunningTaskId() {
		return taskId;
	}
	
	@Override
	public void setLongRunningTaskId(UUID taskId) {
		this.taskId = taskId;
	}
}
