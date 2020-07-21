package eu.bcvsolutions.idm.core.config;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Executor configuration
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AsyncConfig.class);
	// Env is used instead {@link ConfigurationService} - we don't want to configure this props dynamically (restart is needed anyway).
	@Autowired private ConfigurableEnvironment env;
	
	/**
	 * Scaled for combination of computing  (e.g. for automatic roles) and I/O operations (synchronization) tasks. Uses cpu count as default pool size, 
	 * leaves some space for event thread pool, which has higher priority, see {@link #eventExecutor()}.
	 * 
	 */
	@Primary
	@Override
	@Bean(name = SchedulerConfiguration.TASK_EXECUTOR_NAME)
	public Executor getAsyncExecutor() {
		int cpuCount = Runtime.getRuntime().availableProcessors();
		//
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(); 
		int corePoolSize = env.getProperty(SchedulerConfiguration.PROPERTY_TASK_EXECUTOR_CORE_POOL_SIZE, Integer.class, cpuCount);
		executor.setCorePoolSize(corePoolSize); // ~4
		int maxPoolSize = env.getProperty(SchedulerConfiguration.PROPERTY_TASK_EXECUTOR_MAX_POOL_SIZE, Integer.class, corePoolSize * 2);
		executor.setMaxPoolSize(maxPoolSize); // ~8
		int queueCapacity = env.getProperty(
				SchedulerConfiguration.PROPERTY_TASK_EXECUTOR_QUEUE_CAPACITY, 
				Integer.class, 
				SchedulerConfiguration.DEFAULT_TASK_EXECUTOR_QUEUE_CAPACITY);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadPriority(env.getProperty(SchedulerConfiguration.PROPERTY_TASK_EXECUTOR_THREAD_PRIORITY, Integer.class, 5));
		// MODE_INHERITABLETHREADLOCAL mode is not recommended in environment where thread pools are used (old SecurityContext can be reused in next thread using.).
		// Instead that the DelegatingSecurityContextRunnable is used for delegating the SecurityContext to child the thread.
		// Same is applies for TransactionContext. You have to use DelegatingTransactionContextRunnable for delegating to the child thread.
		// Beware, you have to wrap every new Thread to this delegate objects (wrappers).
		executor.setTaskDecorator(runnable -> new DelegatingSecurityContextRunnable(new DelegatingTransactionContextRunnable(runnable)));
		executor.setThreadNamePrefix("base-task-executor-");
		executor.initialize();
		//
		LOG.info("Task executor is initialized: corePoolSize [{}], maxPoolSize [{}], queueCapacity [{}]", 
				corePoolSize,
				maxPoolSize,
				queueCapacity);
		//
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		// simple error logging
		return new AsyncUncaughtExceptionHandler() {		 
		    @Override
		    public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
		       if (throwable instanceof ResultCodeException) {
		    	   ResultCodeException ex = (ResultCodeException) throwable;
		    	   LOG.error("[" + ex.getId() + "] ", ex);
		       } else {
		    	   ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR, throwable.getMessage());
		    	   LOG.error("[" + errorModel.getId() + "] ", throwable);
		       }
		    }		     
		};
	}
	
	/**
	 * Executor for event processing. Common async pool is not reused - event processing has higher priority than LRT. 
	 * Both pools should be processed parallel, not blocked.
	 * Scaled for combination I/O operations (mainly) and computing  (e.g. account management, provisioning) event processing.
	 * Uses cpu count + 1 as default pool size, 
	 * 
	 * @return
	 */
	@Bean(name = SchedulerConfiguration.EVENT_EXECUTOR_NAME)
	public Executor eventExecutor() {
		int cpuCount = Runtime.getRuntime().availableProcessors();
		int poolSize = cpuCount + 1; //5
		//
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		int corePoolSize = env.getProperty(SchedulerConfiguration.PROPERTY_EVENT_EXECUTOR_CORE_POOL_SIZE, Integer.class, poolSize);
		executor.setCorePoolSize(corePoolSize); // ~5
		int maxPoolSize = env.getProperty(SchedulerConfiguration.PROPERTY_EVENT_EXECUTOR_MAX_POOL_SIZE, Integer.class, corePoolSize * 2);
		executor.setMaxPoolSize(maxPoolSize); // ~10
		int queueCapacity = env.getProperty(
				SchedulerConfiguration.PROPERTY_EVENT_EXECUTOR_QUEUE_CAPACITY, 
				Integer.class,
				SchedulerConfiguration.DEFAULT_EVENT_EXECUTOR_QUEUE_CAPACITY);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadPriority(env.getProperty(SchedulerConfiguration.PROPERTY_EVENT_EXECUTOR_THREAD_PRIORITY, Integer.class, 6));
		executor.setThreadNamePrefix("event-task-executor-");
		// @Beta - TODO: application context closed listener
		executor.setWaitForTasksToCompleteOnShutdown(true);
		// MODE_INHERITABLETHREADLOCAL mode is not recommended in environment where thread pools are used (old SecurityContext can be reused in next thread using.).
		// Instead that the DelegatingSecurityContextRunnable is used for delegating the SecurityContext to child the thread.
		// Same is applies for TransactionContext. You have to use DelegatingTransactionContextRunnable for delegating to the child thread.
		// Beware, you have to wrap every new Thread to this delegate objects (wrappers).
		executor.setTaskDecorator(runnable -> new DelegatingSecurityContextRunnable(new DelegatingTransactionContextRunnable(runnable)));
		executor.setAwaitTerminationSeconds(30);
		executor.initialize();
		//
		LOG.info("Event executor is initialized: corePoolSize [{}], maxPoolSize [{}], queueCapacity [{}]", 
				corePoolSize,
				maxPoolSize,
				queueCapacity);
		//
		return executor;
	}
}