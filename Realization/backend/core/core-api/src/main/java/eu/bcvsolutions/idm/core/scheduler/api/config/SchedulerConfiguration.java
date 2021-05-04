package eu.bcvsolutions.idm.core.scheduler.api.config;

import java.util.concurrent.LinkedBlockingQueue;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;

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
	String TASK_EXECUTOR_NAME = "idmTaskExecutor";
	
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
	 * Asynchronous task processing is stopped.
	 * Asynchronous task processing is stopped, when instance for processing is switched => prevent to process asynchronous task in the meantime.
	 * Asynchronous task processing can be stopped for testing or debugging purposes.
	 * Asynchronous task are still created in queue, but they are not processed.
	 * 
	 * Lookout: under idm private prefix => can be changed on fly.
	 * 
	 * @since 11.1.0
	 */
	String PROPERTY_TASK_ASYNCHRONOUS_STOP_PROCESSING = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.scheduler.task.asynchronous.stopProcessing";
	boolean DEFAULT_TASK_ASYNCHRONOUS_STOP_PROCESSING = false;
	
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
	int DEFAULT_EVENT_QUEUE_PROCESS = 500;
	
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
	 * Waiting tasks to be processed. Uses 20 as default. 
	 * {@link LinkedBlockingQueue} is used for queue => capacity is initialized dynamically.
	 * {@link AbotrPolicy} is set for rejected tasks - reject exception has to be processed by a caller ({@link LongRunningTaskManager}).
	 */
	String PROPERTY_TASK_EXECUTOR_QUEUE_CAPACITY = "scheduler.task.executor.queueCapacity";
	int DEFAULT_TASK_EXECUTOR_QUEUE_CAPACITY = 20;
	
	/**
	 * Thread priority for threads in event executor pool - 5 by default (normal).
	 */
	String PROPERTY_TASK_EXECUTOR_THREAD_PRIORITY = "scheduler.task.executor.threadPriority";
	int DEFAULT_TASK_EXECUTOR_THREAD_PRIORITY = 5;
	
	/**
	 * Event executor core pool size. Uses CPU count +1 as default.
	 */
	String PROPERTY_EVENT_EXECUTOR_CORE_POOL_SIZE = "scheduler.event.executor.corePoolSize";
	
	/**
	 * Event executor max pool size. Uses CPU corePoolSize * 2 as default. 
	 * maxPoolSize has to be higher than corePoolSize (IllegalArgumentException is thrown otherwise).
	 * When queueCapacity is full, then new threads are created from corePoolSize to maxPoolSize.
	 */
	String PROPERTY_EVENT_EXECUTOR_MAX_POOL_SIZE = "scheduler.event.executor.maxPoolSize";
	
	/**
	 * Waiting events to be processed. Uses 1000 as default - prevent to prepare events repetitively and use additional threads till maxPoolSize.
	 * {@link LinkedBlockingQueue} is used for queue => capacity is initialized dynamically.
	 * {@link AbotrPolicy} is set for rejected tasks - reject exception has to be processed by a caller ({@link EntityEventManager}).
	 */
	String PROPERTY_EVENT_EXECUTOR_QUEUE_CAPACITY = "scheduler.event.executor.queueCapacity";
	int DEFAULT_EVENT_EXECUTOR_QUEUE_CAPACITY = 50;
	
	/**
	 * Thread priority for threads in event executor pool - 6 by default (a little higher priority than normal 5).
	 */
	String PROPERTY_EVENT_EXECUTOR_THREAD_PRIORITY = "scheduler.event.executor.threadPriority";
	int DEFAULT_EVENT_EXECUTOR_THREAD_PRIORITY = 6;
}
