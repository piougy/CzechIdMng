package eu.bcvsolutions.idm.vs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.PropertyModuleDescriptor;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;

/**
 * Virtual system module descriptor
 * 
 * @author Svanda
 *
 */
@Component
@PropertySource("classpath:module-" + VirtualSystemModuleDescriptor.MODULE_ID + ".properties")
@ConfigurationProperties(prefix = "module." + VirtualSystemModuleDescriptor.MODULE_ID + ".build", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class VirtualSystemModuleDescriptor extends PropertyModuleDescriptor {

	public static final String MODULE_ID = "vs";
	public static final String TOPIC_VS_REQUEST_CREATED = String.format("%s:vsRequestCreated", MODULE_ID);
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	/**
	 * Enables links to swagger documentation
	 */
	@Override
	public boolean isDocumentationAvailable() {
		return true;
	}
	

	@Override
	public List<GroupPermission> getPermissions() {
		return Arrays.asList(VirtualSystemGroupPermission.values());
	}
	
	@Override
	public List<IdmNotificationConfigurationDto> getDefaultNotificationConfigurations() {
		List<IdmNotificationConfigurationDto> configs = new ArrayList<>();
		
		configs.add(new IdmNotificationConfigurationDto(
				TOPIC_VS_REQUEST_CREATED, 
				null, 
				IdmEmailLog.NOTIFICATION_TYPE,
				"New virtual system request (for realization) was created and send to implementers.", 
				getNotificationTemplateId(TOPIC_VS_REQUEST_CREATED)));
		return configs;
	}
}
