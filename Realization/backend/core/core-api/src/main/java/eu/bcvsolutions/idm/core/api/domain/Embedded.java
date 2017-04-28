package eu.bcvsolutions.idm.core.api.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Mark field for convert BaseEntity to UUID (and conversely) and embedded DTO entity.
 * 
 * @author svandav
 *
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Embedded {

	/**
	 * If true, then will be add object as embedded entity to DTO. 
	 * Identifier (UUID) will be setted only.
	 * 
	 * @return
	 */
	boolean enabled() default true;

	/**
	 * Define class of DTO for transform object to embedded DTO entity (and conversely)
	 * 
	 * @return
	 */
	Class<? extends AbstractDto> dtoClass();

}
