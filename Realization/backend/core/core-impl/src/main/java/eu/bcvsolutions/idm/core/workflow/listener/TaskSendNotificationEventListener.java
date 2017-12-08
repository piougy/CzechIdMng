package eu.bcvsolutions.idm.core.workflow.listener;

import java.util.List;
import java.util.stream.Collectors;

import org.activiti.engine.FormService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.workflow.config.WorkflowConfig;

/**
 * Send notification for events {@link ActivitiEventType.TASK_CREATED} and {@link ActivitiEventType.TASK_ASSIGNED}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class TaskSendNotificationEventListener implements ActivitiEventListener{

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskSendNotificationEventListener.class);
	
	@Autowired private IdmIdentityService identityService;
	@Autowired private NotificationManager notificationManager;
	@Autowired private ApplicationContext context;
	@Autowired private ConfigurationService configurationService;
	/*
	 * FormService couldn't be autowired, get from context
	 */
	private FormService formService;
	
	@Override
	public void onEvent(ActivitiEvent event) {
		switch (event.getType()) {
		case TASK_ASSIGNED:
			LOG.debug("TaskSendNotificationEventListener - recieve event [{}]", event.getType());
			//
			// check global disable or enable send notification from WF
			if (!configurationService.getBooleanValue(WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY, true)) {
				LOG.debug("TaskSendNotificationEventListener - Notification is disabled by [{}] configuration property.", WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY);
				break;
			}
			//
			if (event instanceof ActivitiEntityEventImpl &&
					((ActivitiEntityEventImpl)event).getEntity() instanceof TaskEntity) {
				TaskEntity taskEntity = (TaskEntity) ((ActivitiEntityEventImpl)event).getEntity();
				//
				if (isSendDisabledByTask(taskEntity)) {
					break;
				}
				//
				// if not exist assigne send message to all candidates
				if (taskEntity.getAssignee() == null) {
					for (IdentityLink candidate : taskEntity.getCandidates()) {
						sendNotification(CoreModuleDescriptor.TOPIC_WF_TASK_ASSIGNED, taskEntity, candidate.getUserId());
					}
				} else {
					sendNotification(CoreModuleDescriptor.TOPIC_WF_TASK_ASSIGNED, taskEntity, taskEntity.getAssignee());
				}
			} else {
				LOG.info("TaskSendNotificationEventListener - can't get TaskEntity from event [{}] ", event.getExecutionId());
			}
			break;
			
		case TASK_COMPLETED:
			LOG.debug("TaskSendNotificationEventListener - recieve event [{}] [not implemented for now]", event.getType());
			break;
			
		case TASK_CREATED:
			LOG.debug("TaskSendNotificationEventListener - recieve event [{}]", event.getType());
			//
			// check global disable or enable send notification from WF
			if (!configurationService.getBooleanValue(WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY, true)) {
				LOG.debug("TaskSendNotificationEventListener - Notification is disabled by [{}] configuration property.", WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY);
				break;
			}
			//
			if (event instanceof ActivitiEntityEventImpl &&
					((ActivitiEntityEventImpl)event).getEntity() instanceof TaskEntity) {
				TaskEntity taskEntity = (TaskEntity) ((ActivitiEntityEventImpl)event).getEntity();
				//
				if (isSendDisabledByTask(taskEntity)) {
					break;
				}
				//
				// if not exist assigne send message to all candidates
				if (taskEntity.getAssignee() == null) {
					for (IdentityLink candidate : taskEntity.getCandidates()) {
						sendNotification(CoreModuleDescriptor.TOPIC_WF_TASK_CREATED, taskEntity, candidate.getUserId());
					}
				} else {
					sendNotification(CoreModuleDescriptor.TOPIC_WF_TASK_CREATED, taskEntity, taskEntity.getAssignee());
				}
			} else {
				LOG.info("TaskSendNotificationEventListener - can't get TaskEntity from event [{}] ", event.getExecutionId());
			}
			break;

		default:
			LOG.debug("TaskSendNotificationEventListener - receive not required event [{}]", event.getType());
			break;
		}
		
	}
	
	@Override
	public boolean isFailOnException() {
		// can be ignored if logging fails...
		LOG.debug("TaskSendNotificationEventListener - receive exception");
		return false;
	}
	
	/**
	 * Get url from application properties
	 * @param taskEntity
	 * @return
	 */
	private String getUrlToTask(TaskEntity taskEntity) {
		 String origins = configurationService.getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN);
		 //
		 if (origins != null && !origins.isEmpty()) {
			 String origin = origins.trim().split(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN_SEPARATOR)[0];
			 return origin + "/#/task/" + taskEntity.getId();
		 }
		 return null;
	}
	
	/**
	 * Send notification for topic, template is defined by topic. Before send message will be check if exists identity.
	 * @param topic
	 * @param taskEntity
	 * @param username
	 */
	private void sendNotification(String topic, TaskEntity taskEntity, String username) {
		IdmIdentityDto identity = identityService.getByUsername(username);
		//
		if (identity == null) {
			LOG.info("TaskSendNotificationEventListener - Identity [{}] not found, message will not be sent.", username);
			return;
		}
		// send message to topic
		notificationManager.send(topic,
				new IdmMessageDto
				.Builder()
					.addParameter("identity", username)
					.addParameter("url", getUrlToTask(taskEntity))
					.addParameter("subject", taskEntity.getName())
				.build(), identity);
	}
	
	/**
	 * Check if task has disable send notification
	 * @param taskEntity
	 * @return
	 */
	private boolean isSendDisabledByTask(TaskEntity taskEntity) {
		List<FormProperty> properties = getFormProperties(taskEntity.getId());
		if(properties == null){
			return false;
		}
		// get only properties that has id or name equals to SEND_NOTIFICATION_FROM_WF
		properties = properties.stream()
			.filter(
					property ->
						(property.getId() != null && property.getId().equals(WorkflowConfig.SEND_NOTIFICATION_FROM_WF_ATTRIBUTE))
						||
						(property.getName() != null && property.getName().equals(WorkflowConfig.SEND_NOTIFICATION_FROM_WF_ATTRIBUTE))
					)
			.collect(Collectors.toList());
		//
		// if property SEND_NOTIFICATION_FROM_WF is empty send notification
		if (!properties.isEmpty()) {
			// check value of this property
			for (FormProperty property : properties) {
				if (property.getValue().equals(Boolean.FALSE.toString())) {
					LOG.debug("TaskSendNotificationEventListener - Send notification is disabled by task [{}]", taskEntity.getName());
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Method return form properties for task id
	 * @param taskId
	 * @return
	 */
	private List<FormProperty> getFormProperties(String taskId) {
		//
		// initial form service
		if (formService == null) {
			formService = context.getBean(FormService.class);
		}
		//
		TaskFormData formData = formService.getTaskFormData(taskId);
		if (formData != null) {
			return formData.getFormProperties();
		}
		return null;
	}
}
