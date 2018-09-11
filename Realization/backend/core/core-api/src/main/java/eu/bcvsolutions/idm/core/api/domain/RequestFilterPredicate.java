package eu.bcvsolutions.idm.core.api.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Is using only for searching in universal request agenda (IdmRequestItem).
 * That annotations has no impact on standard searching.
 * 
 * Defines in which DTO's field will be value searching. If that annotation is
 * not present and filter field contains value, then will be searching in all
 * DTO's fields.
 * 
 * Only filtering by UUID fields are supported now! Only equals operations are
 * supported now!
 * 
 * 
 * @author svandav
 *
 */
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RequestFilterPredicate {

	/**
	 * Defines in which DTO's field will be value searching.
	 * 
	 * @return
	 */
	String field();

}
