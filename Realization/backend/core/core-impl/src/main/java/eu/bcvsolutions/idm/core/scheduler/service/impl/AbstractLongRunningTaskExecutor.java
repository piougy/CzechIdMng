package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;

/**
 * Template for long running task executor. This template persists long running tasks.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractLongRunningTaskExecutor implements LongRunningTaskExecutor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractLongRunningTaskExecutor.class);
	@Autowired
	private IdmLongRunningTaskService service;
	@Autowired
	private EntityLookupService entityLookupService;
	//
	private ParameterConverter parameterConverter;	
	private UUID taskId;
	protected Long count = null;
	protected Long counter = null;
	
	/**
	 * Default implementation returns module by package conventions.
	 */
	@Override
	public String getModule() {
		return this.getClass().getCanonicalName().split("\\.")[3];
	}
	
	@Override
	public String getDescription() {
		return AutowireHelper.getBeanDescription(this.getClass());
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		// load properties from job
	}
	
	protected boolean start() {
		Assert.notNull(taskId);
		IdmLongRunningTask task = service.get(taskId);
		Assert.notNull(task, "Long running task has to be prepared before task is started");
		//
		if (task.isRunning()) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_RUNNING, ImmutableMap.of("taskId", task.getId()));
		}
		if (!OperationState.isRunnable(task.getResultState())) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_PROCESSED, ImmutableMap.of("taskId", task.getId()));
		}
		//
		Thread currentThread = Thread.currentThread();
		task.setThreadId(currentThread.getId());
		task.setThreadName(currentThread.getName());
		//
		setStateProperties(task);
		//
		task.setRunning(true);
		task.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		//
		service.save(task);
		return true;
	}
	
	
	@Override
	public void run() {
		try {
			if (!start()) {
				return;
			}
			process();
			//
			end(null);
		} catch (Exception ex) {
			end(ex);
		}
	}
	
	protected void end(Exception ex) {
		Assert.notNull(taskId);
		IdmLongRunningTask task = service.get(taskId);
		Assert.notNull(task, "Long running task has to be prepared before task is started");
		//
		setStateProperties(task);
		//
		if (ex != null) {
			ResultModel resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_FAILED, 
					ImmutableMap.of(
							"taskId", taskId, 
							"taskType", task.getTaskType(),
							"instanceId", task.getInstanceId()));	
			LOG.error(resultModel.toString(), ex);
			task.setResult(new OperationResult.Builder(OperationState.EXCEPTION).setModel(resultModel).setCause(ex).build());
		} else if(OperationState.isRunnable(task.getResultState())) { 
			// executed standardly
			task.setResult(new OperationResult.Builder(OperationState.EXECUTED).build());
		}
		task.setRunning(false);
		service.save(task);
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
	
	@Override
	public boolean updateState() {
		service.updateState(taskId, count, counter);
		//
		IdmLongRunningTask task = service.get(taskId);
		return task.isRunning() && OperationState.isRunnable(task.getResultState());
	}
	
	/**
	 * Return parameter converter helper
	 * 
	 * @return
	 */
	protected ParameterConverter getParameterConverter() {
		if (parameterConverter == null) {
			parameterConverter = new ParameterConverter(entityLookupService, null);
		}
		return parameterConverter;
	}
	
	/**
	 * Sets executor's state properties (count..) to long running task instance
	 * 
	 * @param longRunningTask
	 * @param taskExecutor
	 */
	private void setStateProperties(IdmLongRunningTask task) {
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
