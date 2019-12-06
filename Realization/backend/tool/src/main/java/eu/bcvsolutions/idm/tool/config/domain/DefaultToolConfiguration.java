package eu.bcvsolutions.idm.tool.config.domain;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;

/**
 * Tool configuration - implementation
 *
 * @author BCV solutions s.r.o.
 *
 */
@Component("toolConfiguration")
public class DefaultToolConfiguration
		extends AbstractConfiguration
		implements ToolConfiguration {
}
