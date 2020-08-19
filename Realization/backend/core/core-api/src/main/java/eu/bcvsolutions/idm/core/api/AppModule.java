package eu.bcvsolutions.idm.core.api;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;

/**
 * Main application module.
 * AppModule (~descriptor) is used, when application is started (INIT).
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
public interface AppModule extends ModuleDescriptor {

	String MODULE_ID = "app";
	
}
