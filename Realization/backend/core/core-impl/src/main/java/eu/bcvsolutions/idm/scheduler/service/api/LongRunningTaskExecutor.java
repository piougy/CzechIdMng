package eu.bcvsolutions.idm.scheduler.service.api;

import java.util.Map;

import eu.bcvsolutions.idm.scheduler.entity.IdmLongRunningTask;

/**
 * Long running task executor
 * 
 * @author Radek Tomiška
 *
 */
public interface LongRunningTaskExecutor extends Runnable {

	/**
	 * Module identifier
	 * 
	 * @return
	 */
	String getModule();
	
	/**
	 * Initialize task executor before task is processed
	 * 
	 * @param context
	 */
	void init(Map<String, Object> properties);
	
	/**
	 * Main execution method
	 */
	void process();
	
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
	 * Updates persisted task state (count, counter, etc.)
	 * 
	 * @param context
	 * @return Returns false, when long running task is canceled.
	 */
	boolean updateState();
	
	/**
	 * Gets long running task log
	 * 
	 * @return
	 */
	IdmLongRunningTask getLongRunningTask();
	/**
	 * Sets long running task log
	 * 
	 * @param longRunningTask
	 */
	void setLongRunningTask(IdmLongRunningTask longRunningTask);
}
