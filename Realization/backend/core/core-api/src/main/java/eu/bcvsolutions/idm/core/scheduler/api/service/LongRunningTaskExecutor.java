package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Long running task executor
 * 
 * @author Radek Tomiška
 */
public interface LongRunningTaskExecutor<V> extends Callable<V> {
	
	static final String PARAMETER_INSTANCE_ID = "core:instanceId"; // server instance id

	/**
	 * Returns task name (task class name by default)
	 * @return
	 */
	String getName();
	
	/**
	 * Module identifier
	 * 
	 * @return
	 */
	String getModule();
	
	/**
	 * Returns configurable properties names for this task
	 * 
	 * @return
	 */
	List<String> getPropertyNames();
	
	/**
	 * Initialize task executor before task is processed
	 * 
	 * @param context
	 */
	void init(Map<String, Object> properties);
	
	/**
	 * Returns persistent task parameter values. Don't forget to override this method additively.
	 * init(properties) => getProperties() should return the at least the same values as init method.
	 */
	Map<String, Object> getProperties();
	
	/**
	 * Main execution method
	 */
	V process();
	
	/**
	 * Executors description
	 * 
	 * @return
	 */
	String getDescription();
	
	/**
	 * Returns total item count
	 * 
	 * @return
	 */
	Long getCount();
	
	/**
	 * Returns processed items count
	 * 
	 * @return
	 */
	Long getCounter();
	
	/**
	 * Sets total item count
	 * 
	 */
	void setCount(Long count);

	/**
	 * Sets processed items count
	 * 
	 */
	void setCounter(Long counter);
	
	/**
	 * Increase counter by 1 and return it.
	 * @return
	 */
	Long increaseCounter();

	
	/**
	 * Updates persisted task state (count, counter, etc.)
	 * 
	 * @param context
	 * @return Returns false, when long running task is canceled.
	 */
	boolean updateState();
	
	/**
	 * Returns true, when task updates its state when runs => can be canceled, progress can be shown
	 * 
	 * @return
	 */
	boolean isStateful();
	
	/**
	 * Gets long running task log id
	 * 
	 * @return
	 */
	UUID getLongRunningTaskId();
	
	/**
	 * Sets long running task log id
	 * 
	 * @param longRunningTask
	 */
	void setLongRunningTaskId(UUID taskId);
}
