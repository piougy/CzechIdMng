package eu.bcvsolutions.idm.core.config;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import com.google.common.annotations.Beta;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

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

	@Bean
	@Primary
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(7);
		executor.setMaxPoolSize(42);
		executor.setQueueCapacity(11);
		executor.setThreadNamePrefix("base-task-executor-");
		executor.initialize();
		//
		return new DelegatingSecurityContextAsyncTaskExecutor(executor);
	}
	
	/**
	 * Executor for LRT items. Item can be processed asynchronously - want to control pool usage.
	 * 
	 * @return
	 */
	@Beta
	@Bean(name = "longRunningTaskItemExecutor")
	public Executor longRunningTaskItemExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(20);
		executor.setThreadGroupName("provisioning-task-executor");
		executor.setThreadNamePrefix("provisioning-task-executor-");
		executor.initialize();
		executor.setWaitForTasksToCompleteOnShutdown(true);
		// TODO: reject policy configuration
		//
		return new DelegatingSecurityContextAsyncTaskExecutor(executor);
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
}