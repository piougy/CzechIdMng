package eu.bcvsolutions.idm.vs.config.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.config.swagger.AbstractSwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.vs.VirtualSystemModuleDescriptor;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Virtual system module swagger configuration
 *
 * @author Radek Tomiška
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "springfox.documentation.swagger", name = "enabled", matchIfMissing = true)
public class VirtualSystemSwaggerConfig extends AbstractSwaggerConfig {

	@Autowired private VirtualSystemModuleDescriptor moduleDescriptor;

	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}

	@Bean
	public Docket vsApi() {
		return api("eu.bcvsolutions.idm.vs.rest");
	}
}
