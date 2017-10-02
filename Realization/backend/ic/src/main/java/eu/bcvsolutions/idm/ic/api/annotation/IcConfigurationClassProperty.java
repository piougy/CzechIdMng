package eu.bcvsolutions.idm.ic.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation keeps configuration property informations in configuration class.
 * @author svandav
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IcConfigurationClassProperty {

    public int order() default -1;
 
    public String helpMessage() default "";
    
    public String displayName() default "";

    public boolean confidential() default false;

    /**
     * Is this property required?
     */
    public boolean required() default false;
    
    /**
     * Defines how property would be rendered
     * @return
     */
    public String face() default "";
    

}
