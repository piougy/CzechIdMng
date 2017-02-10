package eu.bcvsolutions.idm.core.scheduler.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AutowiringSpringBeanJobFactory;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultSchedulerManager;

/**
 * Quartz scheduler configuration
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Configuration
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
public class SchedulerConfig {
	
	@Autowired
	private ApplicationContext context;
//	@Autowired
//	private DataSource dataSource; // TODO: after flyway will be enabled

	@Bean
	public AutowiringSpringBeanJobFactory jobFactory(ApplicationContext applicationContext) {
		AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		return jobFactory;
	}
	
	/**
	 * Enables scheduler injection through application
	 * 
	 * @param dataSource
	 * @param jobFactory
	 * @return
	 * @throws IOException
	 */
	@Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
		try {
			SchedulerFactoryBean factory = new SchedulerFactoryBean();
	        factory.setOverwriteExistingJobs(true); // update triggers in DB whe config file is changed
	        // TODO: after flyway will be enabled
	        // factory.setDataSource(dataSource);
	        factory.setJobFactory(jobFactory(context));
	        factory.setQuartzProperties(quartzProperties());
	        return factory;
		} catch (IOException ex) {
			throw new CoreException("Quartz properties initialization failed", ex);
		}
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

	@Bean(name = "schedulerManager")
	public SchedulerManager schedulerManager() {
		return new DefaultSchedulerManager(context, schedulerFactoryBean().getScheduler());
	}
}
