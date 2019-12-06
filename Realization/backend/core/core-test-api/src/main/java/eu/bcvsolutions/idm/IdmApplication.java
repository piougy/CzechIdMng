package eu.bcvsolutions.idm;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.scheduling.annotation.EnableScheduling;

import eu.bcvsolutions.idm.core.api.config.flyway.IdmFlywayAutoConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.repository.ExtendedJpaRepositoryFactoryBean;

/**
 * Test application.
 * 
 * Prevent to rename this class - works as black magic to alias to IdmApplication.class in src package.
 * 
 * @author Radek Tomiška
 *
 */
@EnableAutoConfiguration(exclude = { FlywayAutoConfiguration.class, SecurityAutoConfiguration.class })
@EnablePluginRegistries({ ModuleDescriptor.class })
@EnableCaching
@EnableScheduling
@EnableJpaRepositories(repositoryFactoryBeanClass = ExtendedJpaRepositoryFactoryBean.class)
public class IdmApplication extends SpringBootServletInitializer {
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		application.bannerMode(Banner.Mode.OFF);
        return application.sources(IdmApplication.class, IdmFlywayAutoConfiguration.class);
    }
	
}
