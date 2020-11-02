package eu.bcvsolutions.idm.core.scheduler.api.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;

/**
 * LRT cannot have multiple instances executed concurrently.
 * Works differently than quartz {@code DisallowConcurrentExecution} - task is not canceled, but added into LRT queue as ACCEPTED.
 * Task will be started asynchronously after concurrent task ends.
 * 
 * 
 * @author Radek Tomiška
 * @since 10.4.8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IdmCheckConcurrentExecution {
	
	/**
	 * Disallow concurrent execution for given task types.
	 * Annotated task will be used as default, if no value is given. 
	 * Be careful: If value is given, then annotated task is not added automatically - add annotated task too, 
	 * when other or more task types have to be added.
	 * 
	 * @return disallow concurrent execution for task types
	 */
	Class<? extends LongRunningTaskExecutor<?>>[] taskTypes() default {};

}

