package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * Configuration for identity (private - sec)
 * 
 * TODO: #813
 * 
 * @author Radek Tomiška
 *
 */
public interface PrivateIdentityConfiguration extends Configurable {
	
	@Override
	default String getConfigurableType() {
		return "identity";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default public boolean isSecured() {
		return true;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		return properties;
	}
	
	/**
	 * Returns public configuration
	 * 
	 * @return
	 */
	IdentityConfiguration getPublicConfiguration();
	
}
