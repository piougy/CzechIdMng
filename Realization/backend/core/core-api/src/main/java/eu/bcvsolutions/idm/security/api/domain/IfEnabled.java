package eu.bcvsolutions.idm.security.api.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
// import eu.bcvsolutions.idm.core.model.service.IdmConfigurationService; // TODO: split configuration service vs IdmConfigurationService

/**
 * Checks, if given modules or configuration properties are enabled before method invocation.
 * 
 * @author Radek Tomiška
 *
 * @see ModuleDescriptor
 * @see ModuleService
 * @see IdmConfigurationService
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface IfEnabled {
	
	/**
	 * Module ids (alias)
	 * 
	 * @see ModuleService
	 * 
	 * @return
	 */
	@AliasFor("module")
	String[] value() default {};
	
	/**
	 * Module ids
	 * 
	 * @see ModuleService
	 * 
	 * @return
	 */	
	@AliasFor("value")
	String[] module() default {};
	
	/**
	 * configuration property value
	 * 
	 * @see IdmConfigurationService
	 * 
	 * @return
	 */
	String[] property() default {};
}
