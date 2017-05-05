package eu.bcvsolutions.idm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * Core module descriptor - required module
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class CoreModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "core";
	public static final String CHANGE_IDENTITY_ROLES = String.format("%s:changeIdentityRole", MODULE_ID);
	public static final String DISAPPROVE_IDENTITY_ROLES = String.format("%s:disapproveIdentityRole", MODULE_ID);
	public static final String RETURN_REQUEST_IDENTITY_ROLES = String.format("%s:returnRequestIdentityRole", MODULE_ID);
	
	@Autowired
	private IdmNotificationTemplateService templateService;
	
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
		List<NotificationConfigurationDto> configs = new ArrayList<>();
		//
		configs.add(new NotificationConfigurationDto(CHANGE_IDENTITY_ROLES, null, IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about result WF (change identity roles).", templateService.getTemplateByCode("changeIdentityRole").getId()));
		//
		configs.add(new NotificationConfigurationDto(DISAPPROVE_IDENTITY_ROLES, null, IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about disapprove role request.", templateService.getTemplateByCode("disapproveIdentityRole").getId()));
		//
		configs.add(new NotificationConfigurationDto(RETURN_REQUEST_IDENTITY_ROLES, null, IdmEmailLog.NOTIFICATION_TYPE,
				"This message contains information about return role request.", templateService.getTemplateByCode("returnRequestIdentityRole").getId()));
		//
		return configs;
	}
}
