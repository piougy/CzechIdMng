package eu.bcvsolutions.idm.tool.config.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.config.swagger.AbstractSwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.tool.ToolModuleDescriptor;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Tool module swagger configuration
 *
 * @author BCV solutions s.r.o.
 */
@Configuration
@ConditionalOnProperty(prefix = "springfox.documentation.swagger", name = "enabled", matchIfMissing = true)
public class ToolSwaggerConfig extends AbstractSwaggerConfig {

	@Autowired private ToolModuleDescriptor moduleDescriptor;

	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}

	@Bean
	public Docket toolApi() {
		return api("eu.bcvsolutions.idm.rest");
	}
}
