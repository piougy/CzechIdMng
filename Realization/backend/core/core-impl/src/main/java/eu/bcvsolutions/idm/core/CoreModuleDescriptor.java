package eu.bcvsolutions.idm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;

@Component
public class CoreModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "core";
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	@Override
	public String getDescription() {
		return "Core services, entities, domain objects. Required module.";
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
		groupPermissions.addAll(Arrays.asList(NotificationGroupPermission.values()));
		return groupPermissions;
	}
	
}
