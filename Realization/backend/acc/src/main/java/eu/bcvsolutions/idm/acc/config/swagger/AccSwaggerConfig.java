package eu.bcvsolutions.idm.acc.config.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.api.config.domain.AbstractSwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Core module swagger configuration
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@EnableSwagger2
@ConditionalOnProperty(prefix = "springfox.documentation.swagger", name = "enabled", matchIfMissing = true)
public class AccSwaggerConfig extends AbstractSwaggerConfig {
	
	@Autowired private AccModuleDescriptor moduleDescriptor;
	
	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}
	
	@Bean
	public Docket accApi() {
		return api("eu.bcvsolutions.idm.acc");
	}

	/**
	 * TODO: property file usage or by module
	 * 
	 * @return
	 */
	@Override
	protected ApiInfo metaData() {
        ApiInfo apiInfo = new ApiInfo(
                "CzechIdM Acc RESTfull API",
                "CzechIdM RESTfull API for Acc module.",
                getModuleDescriptor().getVersion(),
                "Terms of service",
                new Contact("BCV solutions s.r.o.", "https://github.com/bcvsolutions/CzechIdMng", "info@bcvsolutions.eu"),
               "MIT",
                "https://github.com/bcvsolutions/CzechIdMng/blob/develop/LICENSE",
                Lists.newArrayList());
        return apiInfo;
    }
}