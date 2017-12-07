package eu.bcvsolutions.idm.acc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.core.api.domain.PropertyModuleDescriptor;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmWebsocketLog;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;

/**
 * Account management module descriptor
 * 
 * TODO: module dependencies - core, ic
 * 
 * @author Radek Tomiška
 *
 */
@Component
@PropertySource("classpath:module-" + AccModuleDescriptor.MODULE_ID + ".properties")
@ConfigurationProperties(prefix = "module." + AccModuleDescriptor.MODULE_ID + ".build", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class AccModuleDescriptor extends PropertyModuleDescriptor {

	public static final String MODULE_ID = "acc";
	public static final String TOPIC_PROVISIONING = String.format("%s:provisioning", MODULE_ID);
	public static final String TOPIC_NEW_PASSWORD = String.format("%s:newPassword", MODULE_ID);
	public static final String TOPIC_NEW_PASSWORD_ALL_SYSTEMS = String.format("%s:newPasswordAllSystems", MODULE_ID);
	public static final String TOPIC_PROVISIONING_BREAK_WARNING = String.format("%s:provisioningBreakWarning", MODULE_ID);
	public static final String TOPIC_PROVISIONING_BREAK_DISABLE = String.format("%s:provisioningBreakDisable", MODULE_ID);

	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	@Override
	public List<GroupPermission> getPermissions() {
		return Arrays.asList(AccGroupPermission.values());
	}
	
	public List<IdmNotificationConfigurationDto> getDefaultNotificationConfigurations() {
		List<IdmNotificationConfigurationDto> configs = new ArrayList<>();
		//
		configs.add(new IdmNotificationConfigurationDto(
				TOPIC_PROVISIONING,
				null, IdmWebsocketLog.NOTIFICATION_TYPE, 
				"Notification with new provisioning", 
				getNotificationTemplateId("provisioningSuccess"))
				);
		//
		configs.add(new IdmNotificationConfigurationDto(
				TOPIC_PROVISIONING_BREAK_WARNING,
				null,
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains warning with information about provisioning operation.",
				getNotificationTemplateId("provisioningWarning"))
				);
		//
		configs.add(new IdmNotificationConfigurationDto(
				TOPIC_PROVISIONING_BREAK_DISABLE,
				null,
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about blocked system for provisioning operation.",
				getNotificationTemplateId("provisioningDisable"))
				);
		//
		configs.add(new IdmNotificationConfigurationDto(
				TOPIC_NEW_PASSWORD, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains new password and information about new account.", 
				getNotificationTemplateId("newPassword"))
				);
		//
		configs.add(new IdmNotificationConfigurationDto(
				TOPIC_NEW_PASSWORD_ALL_SYSTEMS, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains new password and information about acconunts.", 
				getNotificationTemplateId("newPasswordAllSystems"))
				);
		return configs;
	}

	@Override
	public boolean isDocumentationAvailable() {
		return true;
	}
}
