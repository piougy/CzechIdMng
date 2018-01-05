package eu.bcvsolutions.idm.core.config.swagger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Enable / disable swagger by configuration property
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@EnableSwagger2
@ConditionalOnProperty(prefix = "springfox.documentation.swagger", name = "enabled", matchIfMissing = true)
public class SwaggerConfig {
	
}
