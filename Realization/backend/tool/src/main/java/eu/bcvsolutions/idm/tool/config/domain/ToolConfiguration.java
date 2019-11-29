package eu.bcvsolutions.idm.tool.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * Tool configuration - interface
 *
 * @author BCV solutions s.r.o.
 */
public interface ToolConfiguration extends Configurable {

	@Override
	default String getConfigurableType() {
		return "configuration";
	}

	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		return properties;
	}
}
