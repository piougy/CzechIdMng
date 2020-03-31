package eu.bcvsolutions.idm.core.api.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * If DTO is inheritable, then we can have problem with find correct service.
 * This annotation describe, for what DTO is Service registered.
 * 
 * @author Vít Švanda
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Inheritable {

	/**
	 * Describe, for what DTO is Service registered.
	 */
	Class<? extends BaseDto> dtoService();

}