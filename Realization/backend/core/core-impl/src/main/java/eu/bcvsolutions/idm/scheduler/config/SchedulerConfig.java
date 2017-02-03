package eu.bcvsolutions.idm.scheduler.config;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import eu.bcvsolutions.idm.scheduler.service.impl.AutowiringSpringBeanJobFactory;

/**
 * Quartz scheduler configuration
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
public class SchedulerConfig {

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
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, JobFactory jobFactory) throws IOException {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true); // update triggers in DB whe config file is changed
        // TODO: after flyway will be enabled
        // factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties());
        return factory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }
}
