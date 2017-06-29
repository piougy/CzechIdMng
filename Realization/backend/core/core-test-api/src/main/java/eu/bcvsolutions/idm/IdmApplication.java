package eu.bcvsolutions.idm;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.plugin.core.config.EnablePluginRegistries;

import eu.bcvsolutions.idm.core.api.config.flyway.IdmFlywayAutoConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Test application
 * 
 * @author Radek Tomiška
 *
 */
@EnableAutoConfiguration(exclude = { FlywayAutoConfiguration.class })
@EnablePluginRegistries({ ModuleDescriptor.class })
@EnableCaching
@EnableSwagger2
public class IdmApplication extends SpringBootServletInitializer {
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		application.bannerMode(Banner.Mode.OFF);
        return application.sources(IdmApplication.class, IdmFlywayAutoConfiguration.class);
    }
	
}
