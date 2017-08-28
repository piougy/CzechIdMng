
package eu.bcvsolutions.idm.ic.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfigurationClass;

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
    
    Class<? extends IcConnectorConfigurationClass> configurationClass();
   

}
