package eu.bcvsolutions.idm.core.workflow.config;

import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.autodeployment.SingleResourceAutoDeploymentStrategy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.workflow.domain.CustomActivityBehaviorFactory;
import eu.bcvsolutions.idm.core.workflow.domain.formtype.CustomFormTypes;

/**
 * Workflow configuration for:
 * - custom behavior (sending email through {@link EmailService})
 * - support for custom form types (decisions etc.)
 * 
 * @author svanda, tomiska
 *
 */
@Configuration
public class WorkflowConfig {
	
	ProcessEngineConfigurationImpl processEngineConfiguration;
	
	/**
	 * Initialize custom behavior
	 * 
	 * @return
	 */
	@Bean
	public ActivityBehaviorFactory activityBehaviorFactory() {
		CustomActivityBehaviorFactory customActivityBehaviorFactory = new CustomActivityBehaviorFactory();
		// Evaluate expression in workflow
		customActivityBehaviorFactory.setExpressionManager(((SpringProcessEngineConfiguration) processEngineConfiguration).getExpressionManager());
		// For catch email
		((SpringProcessEngineConfiguration) processEngineConfiguration).getBpmnParser().setActivityBehaviorFactory(customActivityBehaviorFactory);
		return customActivityBehaviorFactory;
	}
	
	/**
	 * * Adds suppor for custom form types
	 * * configure single resource deployment strategy (coming soon)
	 * 
	 * @return
	 */
	@Bean
	public BeanPostProcessor activitiConfigurer() {
		return new BeanPostProcessor() {

			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {				
				if (bean instanceof ProcessEngineFactoryBean) {					
					processEngineConfiguration = (ProcessEngineConfigurationImpl) (((ProcessEngineFactoryBean) bean).getProcessEngineConfiguration());
					((SpringProcessEngineConfiguration) processEngineConfiguration).setDeploymentMode(SingleResourceAutoDeploymentStrategy.DEPLOYMENT_MODE);
					processEngineConfiguration.setFormTypes(new CustomFormTypes());
				}
				return bean;
			}

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				return bean;
			}
		};

	}


}
