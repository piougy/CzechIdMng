package eu.bcvsolutions.idm.core.scheduler.api.config;

/**
 * Configuration for scheduler and asynchronous processing.
 * 
 * @author Radek Tomiška
 *
 */
public interface SchedulerConfiguration {
	
	/**
	 * Common thread pool, used mainly for asynchronous long running tasks
	 */
	String TASK_EXECUTOR_NAME = "taskExecutor";
	
	/**
	 * Used with event processing.
	 */
	String EVENT_EXECUTOR_NAME = "eventExecutor";
	
	/**
	 * Enable / disable scheduler
	 */
	String PROPERTY_SCHEDULER_ENABLED = "scheduler.enabled";
	boolean DEFAULT_SCHEDULER_ENABLED = true;
	
	/**
	 * Asynchronous task execution is enabled. Asynchronous task execution can be disabled for testing or debugging purposes.
	 */
	String PROPERTY_TASK_ASYNCHRONOUS_ENABLED = "scheduler.task.asynchronous.enabled";
	boolean DEFAULT_TASK_ASYNCHRONOUS_ENABLED = true;
	
	/**
	 * Task queue processing period (ms)
	 */
	String PROPERTY_TASK_QUEUE_PROCESS = "scheduler.task.queue.process";
	int DEFAULT_TASK_QUEUE_PROCESS = 1000;

	/**
	 * Scheduler (Quartz) property file location
	 */
	String PROPERTY_PROPERETIES_LOCATION = "scheduler.properties.location";
	String DEFAULT_PROPERETIES_LOCATION = "/quartz.properties";
	
	/**
	 * Event queue processing period (ms)
	 */
	String PROPERTY_EVENT_QUEUE_PROCESS = "scheduler.event.queue.process";
	int DEFAULT_EVENT_QUEUE_PROCESS = 1000;
	
	/**
	 * Task executor core pool size. Uses CPU count as default.
	 */
	String PROPERTY_TASK_EXECUTOR_CORE_POOL_SIZE = "scheduler.task.executor.corePoolSize";
	
	/**
	 * Task executor max pool size. Uses CPU corePoolSize * 2 as default. 
	 * maxPoolSize has to be higher than corePoolSize (IllegalArgumentException is thrown otherwise).
	 * When queueCapacity is full, then new threads are created from corePoolSize to maxPoolSize.
	 */
	String PROPERTY_TASK_EXECUTOR_MAX_POOL_SIZE = "scheduler.task.executor.maxPoolSize";
	
	/**
	 * Waiting tasks to be processed. Uses {@code Integer.MAX_VALUE} as default.
	 * {@link AbotrPolicy} is set for rejected tasks.	
	 */
	String PROPERTY_TASK_EXECUTOR_QUEUE_CAPACITY = "scheduler.task.executor.queueCapacity";
	int DEFAULT_TASK_EXECUTOR_QUEUE_CAPACITY = Integer.MAX_VALUE;
	
	/**
	 * Thread priority for threads in event executor pool - 5 by default (normal).
	 */
	String PROPERTY_TASK_EXECUTOR_THREAD_PRIORITY = "scheduler.task.executor.threadPriority";
	int DEFAULT_TASK_EXECUTOR_THREAD_PRIORITY = 5;
	
	/**
	 * Event executor core pool size. Uses CPU count * 2 as default.
	 */
	String PROPERTY_EVENT_EXECUTOR_CORE_POOL_SIZE = "scheduler.event.executor.corePoolSize";
	
	/**
	 * Event executor max pool size. Uses CPU corePoolSize * 2 as default. 
	 * maxPoolSize has to be higher than corePoolSize (IllegalArgumentException is thrown otherwise).
	 * When queueCapacity is full, then new threads are created from corePoolSize to maxPoolSize.
	 */
	String PROPERTY_EVENT_EXECUTOR_MAX_POOL_SIZE = "scheduler.event.executor.maxPoolSize";
	
	/**
	 * Waiting events to be processed. Uses corePoolSize * 2 as default.
	 * {@link AbotrPolicy} is set for rejected tasks.
	 */
	String PROPERTY_EVENT_EXECUTOR_QUEUE_CAPACITY = "scheduler.event.executor.queueCapacity";
	
	/**
	 * Thread priority for threads in event executor pool - 6 by default (a little higher priority than normal 5).
	 */
	String PROPERTY_EVENT_EXECUTOR_THREAD_PRIORITY = "scheduler.event.executor.threadPriority";
	int DEFAULT_EVENT_EXECUTOR_THREAD_PRIORITY = 6;
}
