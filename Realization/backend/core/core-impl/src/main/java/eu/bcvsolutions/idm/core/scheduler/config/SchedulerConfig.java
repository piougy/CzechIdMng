package eu.bcvsolutions.idm.core.scheduler.config;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.simpl.RAMJobStore;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.config.flyway.CoreFlywayConfig;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmDependentTaskTriggerRepository;
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
public class SchedulerConfig implements SchedulerConfiguration {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSchedulerManager.class);
	//
	@Value("${" + PROPERTY_PROPERETIES_LOCATION + ":" + DEFAULT_PROPERETIES_LOCATION + "}")
    private String propertiesLocation;

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
    public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext context) {
		try {
			Properties quartzProperties = quartzProperties();
			SchedulerFactoryBean factory = new SchedulerFactoryBean();
	        factory.setOverwriteExistingJobs(true); // update triggers in DB when config file is changed
	        // if store is set to DB set data source, else store in RAM
	        Object store = quartzProperties.get("org.quartz.jobStore.class");
	        if (store != null && !StringUtils.equals(store.toString(), RAMJobStore.class.getCanonicalName())) {
	        	DataSource dataSource = (DataSource) context.getBean("dataSource");
	        	factory.setDataSource(dataSource);
	        }
	        factory.setJobFactory(jobFactory(context));
	        factory.setQuartzProperties(quartzProperties);
	        return factory;
		} catch (IOException ex) {
			throw new CoreException("Quartz properties initialization failed", ex);
		}
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource(propertiesLocation));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    @DependsOn(CoreFlywayConfig.NAME)
	@Bean(name = "schedulerManager")
	public SchedulerManager schedulerManager(ApplicationContext context, IdmDependentTaskTriggerRepository dependentTaskTriggerRepository) {
    	Scheduler scheduler = schedulerFactoryBean(context).getScheduler();
    	SchedulerManager manager = new DefaultSchedulerManager(
				context, 
				schedulerFactoryBean(context).getScheduler(), 
				dependentTaskTriggerRepository);
		// read all task - checks obsolete task types and remove them before scheduler starts automatically
		try {
			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(DefaultSchedulerManager.DEFAULT_GROUP_NAME))) {
				try {
					JobDetail jobDetail = scheduler.getJobDetail(jobKey);
					if (jobDetail == null) {
						// job does not exists
						return null;
					}
					// test task is still installed
					jobDetail.getJobClass().newInstance();
				} catch (org.quartz.SchedulerException ex) {
					if (ex.getCause() instanceof ClassNotFoundException) {
						manager.deleteTask(jobKey.getName());
						//
						LOG.warn("Job [{}] inicialization failed, job class was removed, scheduled task is removed.", jobKey, ex);
					} else {
						throw new CoreException(ex);
					}
				} catch (InstantiationException | IllegalAccessException| BeansException | IllegalArgumentException ex) {
					manager.deleteTask(jobKey.getName());
					//
					LOG.warn("Job [{}] inicialization failed, scheduled task is removed", jobKey, ex);
				}
			}
		} catch (org.quartz.SchedulerException ex) {
			throw new CoreException(ex);
		}
		//
		return manager;
	}
}
