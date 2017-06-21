package eu.bcvsolutions.idm.example.config.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.config.swagger.AbstractSwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Example module swagger configuration
 *
 * @author Radek Tomiška
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "springfox.documentation.swagger", name = "enabled", matchIfMissing = true)
public class ExampleSwaggerConfig extends AbstractSwaggerConfig {

	@Autowired private ExampleModuleDescriptor moduleDescriptor;

	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}

	@Bean
	public Docket exampleApi() {
		return api("eu.bcvsolutions.idm.example");
	}
}
