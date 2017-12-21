package eu.bcvsolutions.idm.core.api.domain;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;

/**
 * Add default methods implementation for {@link ModuleDescriptor}.
 * No additional configuration in needed. 
 * Read information directly from pom file.
 * Cannot be used in integration tests (manifest for pom file is generated after tests are done) - use {@link PropertyModuleDescriptor}.
 * 
 * @see PropertyModuleDescriptor
 * @see ModuleDescriptor
 * @see ModuleService
 * 
 * @author Radek Tomiška
 */
public abstract class AbstractModuleDescriptor implements ModuleDescriptor {

	@Autowired(required = false)
	private IdmNotificationTemplateService notificationTemplateService; // optional dependency
	
	@Override
	public boolean supports(String delimiter) {
		return getId().equals(delimiter);
	}

	/**
	 * Returns module version from pom project
	 */
	@Override
	public String getName() {
		return getClass().getPackage().getImplementationTitle();
	}

	/**
	 * Returns module version from pom project
	 */
	@Override
	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
	}
	
	/**
	 * Returns module vendor from pom project
	 */
	@Override
	public String getVendor() {
		return getClass().getPackage().getImplementationVendor();
	}
	
	/**
	 * Returns null by default. Use {@link PropertyModuleDescriptor}.
	 * 
	 * @see PropertyModuleDescriptor
	 */
	@Override
	public String getVendorUrl() {
		return null;
	}
	
	/**
	 * Returns null by default. Use {@link PropertyModuleDescriptor}.
	 * 
	 * @see PropertyModuleDescriptor
	 */
	@Override
	public String getVendorEmail() {
		return null;
	}
	
	/**
	 * Returns null by default. Use {@link PropertyModuleDescriptor}.
	 * 
	 * @see PropertyModuleDescriptor
	 */
	@Override
	public String getBuildNumber() {
		return null;
	}
	
	/**
	 * Returns null by default. Use {@link PropertyModuleDescriptor}.
	 * 
	 * @see PropertyModuleDescriptor
	 */
	@Override
	public String getBuildTimestamp() {
		return null;
	}
	
	/**
	 * Returns description from pom project
	 */
	@Override
	public String getDescription() {
		return getClass().getPackage().getSpecificationTitle();
	}

	/**
	 * Returns empty permissions
	 */
	@Override
	public List<GroupPermission> getPermissions() {
		return Collections.emptyList();
	}
	
	@Override
	public boolean isDisableable() {
		return true;
	}
	
	/**
	 * Returns empty notifications list
	 */
	@Override
	public List<NotificationConfigurationDto> getDefaultNotificationConfigurations() {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns {@code false} by default. Override, when documentation is exposed as webjar.
	 */
	@Override
	public boolean isDocumentationAvailable() {
		return false;
	}
	
	/**
	 * Returns notification template by given code 
	 * @param code
	 * @return
	 * @throws IllegalArgumentException if template by given code is not found
	 */
	protected UUID getNotificationTemplateId(String code) {
		Assert.hasLength(code);
		//
		IdmNotificationTemplateDto notificationTemplate = notificationTemplateService.getTemplateByCode(code);
		if (notificationTemplate == null) {
			throw new IllegalArgumentException(String.format(
					"System template with code [%s] for module [%s] not found. Check template's path configuration [%s].", 
					code, 
					getId(),
					IdmNotificationTemplateService.TEMPLATE_FOLDER));
		}
		return notificationTemplate.getId();
	}
	
}
