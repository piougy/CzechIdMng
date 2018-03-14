package eu.bcvsolutions.idm.core.scheduler.api.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * LRT cannot have multiple instances executed concurrently.
 * 
 * @author Radek Tomiška
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DisallowConcurrentExecution {
	
	/**
	 * When another task runs or it's scheduled already, then LRT will not be scheduled redundantly
	 * 
	 * @return
	 */
	boolean silently() default false;

}

