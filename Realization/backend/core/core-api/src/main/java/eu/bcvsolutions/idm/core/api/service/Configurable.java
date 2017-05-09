package eu.bcvsolutions.idm.core.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;

/**
 * Configurable object by {@link ConfigurationService}.
 * 
 * @author Radek Tomiška
 *
 */
public interface Configurable {

	/**
	 * Configuration service for accessing to configured properties
	 * 
	 * @return
	 */
	ConfigurationService getConfigurationService();
	
	/**
	 * Configurable type identifier - e.g. "processor", "filter"
	 * @return
	 */
	String getConfigurableType();
	
	/**
	 * Module identifier
	 * 
	 * @return
	 */
	default String getModule() {
		return EntityUtils.getModule(this.getClass());
	}
	
	/**
	 * Unique (module scope) configurable object identifier. Its used in configuration key etc.
	 * 
	 * @return
	 */
	default String getName() {
		String name = this.getClass().getCanonicalName();
		if (StringUtils.isEmpty(name)) {
			// TODO: inline classes ...
			return null;
		}
		return SpinalCase.format(name);
	}
	
	/**
	 * Returns true, when configurable object could be disabled
	 * 
	 * @return
	 */
	default boolean isDisableable() {
		return true;
	}
	
	/**
	 * Returns true, when configurable object is disabled
	 * 
	 * @return
	 */
	default boolean isDisabled() {
		// check for processor is enabled, if configuration service is given
		if (getConfigurationService() != null) {
			return !getConfigurationService().getBooleanValue(
					getConfigurationPrefix()
					+ ConfigurationService.PROPERTY_SEPARATOR
					+ ConfigurationService.PROPERTY_ENABLED, true);
		}
		// enabled by default
		return false;
	}
	
	/**
	 * Returns prefix to configuration for this configurable object. 
	 * Under this prefix could be found all configurable object's properties.
	 * 
	 * @return
	 */
	default String getConfigurationPrefix() {
		return ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
				+ getModule()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ getConfigurableType()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ getName();
	}
	
	/**
	 * Returns configuration property names for this configurable object
	 * 
	 * @return
	 */
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(ConfigurationService.PROPERTY_ENABLED);
		properties.add(ConfigurationService.PROPERTY_ORDER);
		return properties;
	}
	
	/**
	 * Returns configuration properties for this configurable object (all properties by configuration prefix)
	 * 
	 * @see {@link #getConfigurationPrefix()}
	 * @see {@link #getPropertyNames()}
	 * @see ConfigurationService
	 * 
	 * @return
	 */
	default ConfigurationMap getConfigurationProperties() {
		ConfigurationMap configs = new ConfigurationMap();
		if (getConfigurationService() == null) {
			return configs;
		}
		for (String propertyName : getPropertyNames()) {
			configs.put(propertyName, getConfigurationProperty(propertyName));
		}
		return configs;
	}
	
	/**
	 * Returns whole property name with prefix in configuration
	 * 
	 * @param propertyName
	 * @return
	 */
	default String getConfigurationPropertyName(String propertyName) {
		return getConfigurationPrefix()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ propertyName;
	}
	
	/**
	 * Returns configured value for given propertyName. If no value for given key is configured, then returns {@code null}.
	 * 
	 * @param propertyName
	 * @return
	 */
	default String getConfigurationProperty(String propertyName) {
		return getConfigurationProperty(propertyName, null);
	}
	
	/**
	 * Returns configured value for given propertyName. If no value for given key is configured, then returns given defaultValue.
	 * 
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	default String getConfigurationProperty(String propertyName, String defaultValue) {
		if (getConfigurationService() == null) {
			return null;
		}
		return getConfigurationService().getValue(
				getConfigurationPropertyName(propertyName), 
				defaultValue);
	}
}
