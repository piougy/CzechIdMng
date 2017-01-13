package eu.bcvsolutions.idm.acc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;
import eu.bcvsolutions.idm.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.notification.entity.IdmWebsocketLog;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;

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
		configs.add(new NotificationConfigurationDto(TOPIC_PROVISIONING, null, IdmWebsocketLog.NOTIFICATION_TYPE));
		return configs;
	}
}
