package eu.bcvsolutions.idm.acc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmWebsocketLog;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
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
public class AccModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "acc";
	public static final String TOPIC_PROVISIONING = String.format("%s:provisioning", MODULE_ID);
	public static final String TOPIC_NEW_PASSWORD = String.format("%s:newPassword", MODULE_ID);
	
	@Autowired
	private IdmNotificationTemplateService templateService;
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	@Override
	public List<GroupPermission> getPermissions() {
		return Arrays.asList(AccGroupPermission.values());
	}
	
	public List<NotificationConfigurationDto> getDefaultNotificationConfigurations() {
		List<NotificationConfigurationDto> configs = new ArrayList<>();
		//
		configs.add(new NotificationConfigurationDto(TOPIC_PROVISIONING,
				null, IdmWebsocketLog.NOTIFICATION_TYPE, "Notification with new provisioning", templateService.getTemplateByCode("provisioningSuccess").getId()));
		//
		configs.add(new NotificationConfigurationDto(TOPIC_NEW_PASSWORD, null, IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains new password and information about new account.", templateService.getTemplateByCode("newPassword").getId()));
		return configs;
	}


}
