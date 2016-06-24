package eu.bcvsolutions.idm.core.workflow.config;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.workflow.domain.formtype.CustomFormTypes;

@Configuration
public class WorkflowConfig {
	

//	@Bean
//	public ActivityBehaviorFactory activityBehaviorFactory() {
//		CustomActivityBehaviorFactory customActivityBehaviorFactory = new CustomActivityBehaviorFactory();
//		//Evaluate expression in workflow
//		customActivityBehaviorFactory.setExpressionManager(((SpringProcessEngineConfiguration) processEngineConfiguration).getExpressionManager());
//		//For catch email
//		((SpringProcessEngineConfiguration) processEngineConfiguration).getBpmnParser().setActivityBehaviorFactory(customActivityBehaviorFactory);
//		return customActivityBehaviorFactory;
//	}
	
//	@Autowired
//	ProcessEngineConfiguration processEngineConfiguration;
	
	
//	@Bean
//	public BeanPostProcessor activitiConfigurer() {
//		return new BeanPostProcessor() {
//
//			@Override
//			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//				if (bean instanceof SpringProcessEngineConfiguration) {
//					List<AbstractFormType> customFormTypes = Arrays.<AbstractFormType> asList(new DecisionFormType());
//					((SpringProcessEngineConfiguration) bean).setCustomFormTypes(customFormTypes);
//				}
//				return bean;
//			}
//
//			@Override
//			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//				return bean;
//			}
//		};
//
//	}
	
	@Bean
	public BeanPostProcessor activitiConfigurer() {
		return new BeanPostProcessor() {

			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
				if (bean instanceof ProcessEngineFactoryBean) {
					ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineFactoryBean) bean).getProcessEngineConfiguration(); //
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
