package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.api.LongRunningTaskExecutor;

/**
 * Template for long running task executor. This template persists long running tasks.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractLongRunningTaskExecutor implements LongRunningTaskExecutor {

	@Autowired
	private IdmLongRunningTaskService longRunningTaskService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private EntityLookupService entityLookupService;
	@Autowired(required = false)
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	//
	private ParameterConverter parameterConverter;	
	private IdmLongRunningTask longRunningTask;	
	
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
	
	protected void start() {
		if (longRunningTask == null) {
			longRunningTask = new IdmLongRunningTask();
			longRunningTask.setTaskType(this.getClass().getCanonicalName());
			longRunningTask.setTaskDescription(getDescription());
		}
		longRunningTask.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		Thread currentThread = Thread.currentThread();
		longRunningTask.setInstanceId(getInstanceId());
		longRunningTask.setThreadId(currentThread.getId());
		longRunningTask.setThreadName(currentThread.getName());
		//
		setStateProperties();
		//
		longRunningTask.setRunning(true);
		longRunningTask = longRunningTaskService.save(longRunningTask);
	}
	
	
	@Override
	public void run() {
		start();
		//
		try {
			process();
			//
			end(null);
		} catch (Exception ex) {
			end(ex);
		}
	}
	
	protected void end(Exception ex) {
		setStateProperties();
		//
		if (ex != null) {
			longRunningTask.setResult(new OperationResult.Builder(OperationState.EXCEPTION).setCode("EX").setCause(ex).build()); // TODO: result code
		} else if(!longRunningTask.isRunning()) {
			longRunningTask.setResult(new OperationResult.Builder(OperationState.CANCELED).build()); // TODO: result state canceled standardly
		} else {
			// executed standardly
			longRunningTask.setResult(new OperationResult.Builder(OperationState.EXECUTED).build());
		}
		longRunningTask.setRunning(false);
		longRunningTask = longRunningTaskService.save(longRunningTask);
	}
	
	/**
	 * Override for count support
	 */
	@Override
	public Long getCount() {
		return null;
	}
	
	/**
	 * Override for counter support
	 */
	@Override
	public Long getCounter() {
		return null;
	}
	
	@Override
	public boolean updateState() {
		longRunningTask = longRunningTaskService.get(longRunningTask.getId());
		//
		setStateProperties();
		longRunningTask = longRunningTaskService.save(longRunningTask);
		return longRunningTask.isRunning();
	}
	
	/**
	 * Return parameter converter helper
	 * 
	 * @return
	 */
	protected ParameterConverter getParameterConverter() {
		if (parameterConverter == null) {
			parameterConverter = new ParameterConverter(entityLookupService, mapper);
		}
		return parameterConverter;
	}
	
	/**
	 * Sets executor's state properties (count..) to long running task instance
	 * 
	 * @param longRunningTask
	 * @param taskExecutor
	 */
	private void setStateProperties() {
		longRunningTask.setCount(getCount());
		longRunningTask.setCounter(getCounter());
		if (longRunningTask.getCount() != null && longRunningTask.getCounter() == null) {
			longRunningTask.setCounter(0L);
		}
	}
	
	@Override
	public IdmLongRunningTask getLongRunningTask() {
		return longRunningTask;
	}
	
	@Override
	public void setLongRunningTask(IdmLongRunningTask longRunningTask) {
		this.longRunningTask = longRunningTask;
	}
	
	/**
	 * Returns server instance id
	 * 
	 * @return
	 */
	protected String getInstanceId() {
		return configurationService.getValue(ConfigurationService.PROPERTY_APP_INSTANCE_ID, ConfigurationService.DEFAULT_PROPERTY_APP_INSTANCE_ID);
	}
}
