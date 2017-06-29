package eu.bcvsolutions.idm.core.config.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.config.swagger.AbstractSwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Core module swagger configuration
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "springfox.documentation.swagger", name = "enabled", matchIfMissing = true)
public class CoreSwaggerConfig extends AbstractSwaggerConfig {
	
	@Autowired private CoreModuleDescriptor moduleDescriptor;
	
	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}
	
	@Bean
	public Docket coreApi() {
		return api("eu.bcvsolutions.idm.core");
	}
}