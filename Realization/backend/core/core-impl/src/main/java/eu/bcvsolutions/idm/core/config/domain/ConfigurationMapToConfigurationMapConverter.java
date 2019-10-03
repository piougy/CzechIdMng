package eu.bcvsolutions.idm.core.config.domain;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;

/**
 * Simple ConfigurationMap converter - map without template is not provided by model mapper out of box.
 * 
 * @author Radek Tomiška
 * @since 9.7.9
 */
public class ConfigurationMapToConfigurationMapConverter implements Converter<ConfigurationMap, ConfigurationMap> {

	@Override
	public ConfigurationMap convert(MappingContext<ConfigurationMap, ConfigurationMap> context) {
		// FIXME: create deep copy.
		return context == null ? null : context.getSource();
	}

}