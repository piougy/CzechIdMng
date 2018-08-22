package eu.bcvsolutions.idm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.PropertyModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityMonitoredFieldsProcessor;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.notification.entity.IdmConsoleLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * Core module descriptor - required module
 * 
 * TODO: refactor to configuration
 * TODO: Split api / impl (see RptModuleDescriptor) - Core module descriptor should be in api
 * 
 * @author Radek Tomiška
 *
 */
@Component
@PropertySource("classpath:module-" + CoreModuleDescriptor.MODULE_ID + ".properties")
@ConfigurationProperties(prefix = "module." + CoreModuleDescriptor.MODULE_ID + ".build", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class CoreModuleDescriptor extends PropertyModuleDescriptor {

	public static final String MODULE_ID = "core";
	public static final String TOPIC_CHANGE_IDENTITY_ROLES = String.format("%s:changeIdentityRole", MODULE_ID);
	public static final String TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER = String.format("%s:changeIdentityRoleImplementer", MODULE_ID);
	public static final String TOPIC_DISAPPROVE_IDENTITY_ROLES = String.format("%s:disapproveIdentityRole", MODULE_ID);
	public static final String TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER = String.format("%s:disapproveIdentityRoleImplementer", MODULE_ID);
	public static final String TOPIC_RETURN_REQUEST_IDENTITY_ROLES = String.format("%s:returnRequestIdentityRole", MODULE_ID);		
	public static final String TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER = String.format("%s:returnRequestIdentityRoleImplementer", MODULE_ID);
	public static final String TOPIC_WF_TASK_CREATED = String.format("%s:wfTaskCreated", MODULE_ID);
	public static final String TOPIC_WF_TASK_ASSIGNED = String.format("%s:wfTaskAssigned", MODULE_ID);
	public static final String TOPIC_PASSWORD_EXPIRATION_WARNING = String.format("%s:passwordExpirationWarning", MODULE_ID);
	public static final String TOPIC_PASSWORD_EXPIRED = String.format("%s:passwordExpired", MODULE_ID);
	public static final String TOPIC_IDENTITY_MONITORED_CHANGED_FIELDS = String.format("%s:%s", MODULE_ID, IdentityMonitoredFieldsProcessor.TOPIC);
	public static final String TOPIC_PASSWORD_CHANGED = String.format("%s:passwordChanged", MODULE_ID);
	public static final String TOPIC_PASSWORD_SET = String.format("%s:passwordSet", MODULE_ID);
	public static final String TOPIC_EVENT = String.format("%s:event", MODULE_ID);
	public static final String TOPIC_LOGIN_BLOCKED = String.format("%s:loginBlocked", MODULE_ID);
	public static final String TOPIC_BULK_ACTION_END = String.format("%s:bulkActionEnd", MODULE_ID);

	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	/**
	 * Core module can't be disabled
	 */
	@Override
	public boolean isDisableable() {
		return false;
	}
	
	@Override
	public List<GroupPermission> getPermissions() {
		List<GroupPermission> groupPermissions = new ArrayList<>();
		groupPermissions.addAll(Arrays.asList(IdmGroupPermission.values()));
		groupPermissions.addAll(Arrays.asList(CoreGroupPermission.values()));
		groupPermissions.addAll(Arrays.asList(NotificationGroupPermission.values()));
		return groupPermissions;
	}
	
	@Override
	public List<NotificationConfigurationDto> getDefaultNotificationConfigurations() {
		// TODO: this doesn't make good sense now - should be moved to xml at all?
		//
		List<NotificationConfigurationDto> configs = new ArrayList<>();
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_CHANGE_IDENTITY_ROLES, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about result WF (change identity roles).", 
				getNotificationTemplateId("changeIdentityRole")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about result WF (change identity roles).", 
				getNotificationTemplateId("changeIdentityRoleImplementer")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_DISAPPROVE_IDENTITY_ROLES, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about disapprove role request.", 
				getNotificationTemplateId("disapproveIdentityRole")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about disapprove role request.", 
				getNotificationTemplateId("disapproveIdentityRoleImplementer")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_RETURN_REQUEST_IDENTITY_ROLES, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about return role request.", 
				getNotificationTemplateId("returnRequestIdentityRole")));
		//		
		configs.add(new NotificationConfigurationDto(
				TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about return role request.", 
				getNotificationTemplateId("returnRequestIdentityRoleImplementer")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_WF_TASK_ASSIGNED, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about new assigned task to user.", 
				getNotificationTemplateId("wfTaskNotificationMessage")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_WF_TASK_CREATED, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about new assigned task to user.", 
				getNotificationTemplateId("wfTaskNotificationMessage")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_PASSWORD_EXPIRATION_WARNING, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"Password expiration warning.", 
				getNotificationTemplateId("passwordExpirationWarning")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_PASSWORD_EXPIRED, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"Password expired.", 
				getNotificationTemplateId("passwordExpired")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_IDENTITY_MONITORED_CHANGED_FIELDS, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about changed fields on Identity.", 
				getNotificationTemplateId(IdentityMonitoredFieldsProcessor.EMAIL_TEMPLATE)));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_PASSWORD_CHANGED, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"Password has been changed.", 
				getNotificationTemplateId("passwordChanged")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_PASSWORD_SET, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"Password has been set.", 
				getNotificationTemplateId("passwordChanged")));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_EVENT, 
				null, 
				IdmConsoleLog.NOTIFICATION_TYPE,
				"Events (asynchronous).", 
				null));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_LOGIN_BLOCKED, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"Login is blocked.", 
				getNotificationTemplateId(AuthenticationManager.TEMPLATE_LOGIN_IS_BLOCKED)));
		//
		configs.add(new NotificationConfigurationDto(
				TOPIC_BULK_ACTION_END, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"Bulk action ended.", 
				getNotificationTemplateId("bulkActionEnd")));
		return configs;
	}
	
	@Override
	public boolean isDocumentationAvailable() {
		return true;
	}

	@Override
	public List<ResultCode> getResultCodes() {
		return Arrays.asList(CoreResultCode.values());
	}
}
