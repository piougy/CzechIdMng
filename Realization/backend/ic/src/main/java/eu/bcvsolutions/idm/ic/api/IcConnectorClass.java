
package eu.bcvsolutions.idm.ic.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation identifies connector
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IcConnectorClass {

	
	String name();
	
	String framework();
	
	String version();

    /**
     * The display name for connector
     */
    String displayName();
   

}
