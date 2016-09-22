package eu.bcvsolutions.idm.security.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import eu.bcvsolutions.idm.core.model.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.model.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.ModuleService;

/**
 * Checks, if given modules are enabled before method invocation.
 * 
 * @author Radek Tomiška
 *
 * @see ModuleDescriptor
 * @see ModuleService
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface IfEnabled {
	
	/**
	 * Module ids
	 * 
	 * @see ModuleService
	 * 
	 * @return
	 */	
	String[] module();
	
	/**
	 * configuration property value
	 * 
	 * @see IdmConfigurationService
	 * 
	 * @return
	 */
	String[] property() default {};
}
