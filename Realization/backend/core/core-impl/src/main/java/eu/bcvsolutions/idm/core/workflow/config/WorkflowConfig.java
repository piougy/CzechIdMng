package eu.bcvsolutions.idm.core.workflow.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.autodeployment.SingleResourceAutoDeploymentStrategy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.notification.service.api.EmailNotificationSender;
import eu.bcvsolutions.idm.core.workflow.domain.CustomActivityBehaviorFactory;
import eu.bcvsolutions.idm.core.workflow.domain.formtype.CustomFormTypes;
import eu.bcvsolutions.idm.core.workflow.listener.CandidateToUUIDEventListener;
import eu.bcvsolutions.idm.core.workflow.listener.StartSubprocessEventListener;
import eu.bcvsolutions.idm.core.workflow.listener.TaskSendNotificationEventListener;

/**
 * Workflow configuration for: - custom behavior (sending email through
 * {@link EmailNotificationSender}) - support for custom form types (decisions etc.) -
 * support for custom activiti event listeners
 * 
 * @author svanda, tomiska
 *
 */
@Configuration
public class WorkflowConfig {
	
	/*
	 * Attribute that is in WF. Enable or disable send notification. When this
	 * attribute is empty is send notification 
	 */
	public static String SEND_NOTIFICATION_FROM_WF_ATTRIBUTE = "sendNotification";
	
	/*
	 * Configuration attribute that allow global enable
	 * or disable bulk sending notification from WF
	 * This application property has higher priority than attribute set into task. 
	 */
	public static String  SEND_NOTIFICATION_CONFIGURATION_PROPERTY = "idm.sec.core.wf.notification.send";
	
	@Autowired
	private StartSubprocessEventListener startSubprocesEventListener;
	@Autowired
	private TaskSendNotificationEventListener taskSendNotificationEventListener;
	@Autowired
	private CandidateToUUIDEventListener candidateToUUIDEventListener;
	
	// Only local variable (no autowired bean)
	private ProcessEngineConfigurationImpl processEngineConfiguration;

	/**
	 * Initialize custom behavior
	 * 
	 * @return
	 */
	@Bean()
	public ActivityBehaviorFactory activityBehaviorFactory() {
		CustomActivityBehaviorFactory customActivityBehaviorFactory = new CustomActivityBehaviorFactory();
		// Evaluate expression in workflow
		customActivityBehaviorFactory.setExpressionManager(
				((SpringProcessEngineConfiguration) processEngineConfiguration).getExpressionManager());
		// For catch email
		((SpringProcessEngineConfiguration) processEngineConfiguration).getBpmnParser()
				.setActivityBehaviorFactory(customActivityBehaviorFactory);
		return customActivityBehaviorFactory;
	}

	/**
	 * * Adds support for custom form types * configure single resource
	 * deployment strategy (coming soon) * Add custom Activiti event listeners
	 * 
	 * @return
	 */
	@Bean()
	public BeanPostProcessor activitiConfigurer() {
		return new BeanPostProcessor() {

			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
				if (bean instanceof ProcessEngineFactoryBean) {
					processEngineConfiguration = (ProcessEngineConfigurationImpl) (((ProcessEngineFactoryBean) bean)
							.getProcessEngineConfiguration());
					
					// Set UUID ID generator
					StrongUuidGenerator uuidGenerator = new StrongUuidGenerator();
					processEngineConfiguration.setIdGenerator(uuidGenerator);
					
					((SpringProcessEngineConfiguration) processEngineConfiguration)
							.setDeploymentMode(SingleResourceAutoDeploymentStrategy.DEPLOYMENT_MODE);
									
					((ProcessEngineConfigurationImpl)processEngineConfiguration).setFormTypes(new CustomFormTypes());
					
					//Add ours Activiti event listeners to engine configuration
					addActivitiEventListeners(processEngineConfiguration);
				}
				return bean;
			}

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				return bean;
			}
		};

	}

	/**
	 * Add ours custom Activiti event listeners to engine configuration
	 * 
	 * @param processEngineConfiguration
	 */
	private void addActivitiEventListeners(ProcessEngineConfigurationImpl processEngineConfiguration) {
		Map<String, List<ActivitiEventListener>> typedListeners = new HashMap<>();
		typedListeners.put(ActivitiEventType.PROCESS_STARTED.name(),
				Stream.of(startSubprocesEventListener).collect(Collectors.toList()));
		typedListeners.put(ActivitiEventType.TASK_ASSIGNED.name(), 
				Stream.of(taskSendNotificationEventListener, candidateToUUIDEventListener).collect(Collectors.toList()));
		typedListeners.put(ActivitiEventType.TASK_CREATED.name(), 
				Stream.of(taskSendNotificationEventListener, candidateToUUIDEventListener).collect(Collectors.toList()));
		typedListeners.put(ActivitiEventType.TASK_COMPLETED.name(), 
				Stream.of(taskSendNotificationEventListener, candidateToUUIDEventListener).collect(Collectors.toList()));
		typedListeners.put(ActivitiEventType.ENTITY_CREATED.name(), 
				Stream.of(candidateToUUIDEventListener).collect(Collectors.toList()));
		typedListeners.put(ActivitiEventType.ENTITY_INITIALIZED.name(), 
				Stream.of(candidateToUUIDEventListener).collect(Collectors.toList()));
		
		
		processEngineConfiguration.setTypedEventListeners(typedListeners);
	}

}
