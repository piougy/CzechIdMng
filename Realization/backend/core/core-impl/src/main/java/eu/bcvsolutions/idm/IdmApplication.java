package eu.bcvsolutions.idm;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.plugin.core.config.EnablePluginRegistries;

import eu.bcvsolutions.idm.core.api.config.flyway.IdmFlywayAutoConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;

/**
 * Application entry point
 * 
 * TODO: support other packages than 'eu.bcv ...' for component scanning 
 * 
 * @author Radek Tomiška
 *
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = { 
		FlywayAutoConfiguration.class // see {@link IdmFlywayAutoConfiguration}lass 
		})
@EnableCaching
@EnablePluginRegistries({ ModuleDescriptor.class })
public class IdmApplication extends SpringBootServletInitializer {
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		application.bannerMode(Banner.Mode.OFF);
        return application.sources(IdmApplication.class, IdmFlywayAutoConfiguration.class);
    }
}
