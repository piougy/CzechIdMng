package eu.bcvsolutions.idm.acc.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;

/**
 * Configurations for provisioning break
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface ProvisioningBreakConfiguration extends Configurable {

	String REQEX_FOR_RECIPIENTS = ",";
	
	String PROPERTY_DISABLED = "disabled";
	
	String PROPERTY_WARNING_LIMIT = "warningLimit";	
	
	String PROPERTY_DISABLE_LIMIT = "disableLimit";	
	
	String PROPERTY_PERIOD = "period";
	
	String PROPERTY_TEMPLATE_WARNING = "templateWarning";	
	
	String PROPERTY_TEMPLATE_DISABLE = "templateDisable";	
	
	String PROPERTY_IDENTITY_RECIPIENTS = "identityRecipients";
	
	String PROPERTY_ROLE_RECIPIENTS = "roleRecipients";

	String GLOBAL_BREAK_PREFIX = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			AccModuleDescriptor.MODULE_ID + ConfigurationService.PROPERTY_SEPARATOR + "provisioning.break" + ConfigurationService.PROPERTY_SEPARATOR;
	
	String GLOBAL_BREAK_UPDATE_OPERATION = "update" + ConfigurationService.PROPERTY_SEPARATOR;
	
	String GLOBAL_BREAK_CREATE_OPERATION = "create" + ConfigurationService.PROPERTY_SEPARATOR;
	
	String GLOBAL_BREAK_DELETE_OPERATION = "delete" + ConfigurationService.PROPERTY_SEPARATOR;
	
	@Override
	default String getConfigurableType() {
		return "provisioning.break";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}

	@Override
	default public boolean isSecured() {
		return true;
	}
	
	@Override
	default String getConfigurationPrefix() {
		// overload, we dont want add getName()
		return (isSecured() ? ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX : ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX)
				+ getModule()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ getConfigurableType();
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		// global configuration for update operation
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_UPDATE_OPERATION + PROPERTY_DISABLE_LIMIT);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_UPDATE_OPERATION + PROPERTY_DISABLED);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_UPDATE_OPERATION + PROPERTY_IDENTITY_RECIPIENTS);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_UPDATE_OPERATION + PROPERTY_PERIOD);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_UPDATE_OPERATION + PROPERTY_ROLE_RECIPIENTS);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_UPDATE_OPERATION + PROPERTY_TEMPLATE_DISABLE);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_UPDATE_OPERATION + PROPERTY_TEMPLATE_WARNING);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_UPDATE_OPERATION + PROPERTY_WARNING_LIMIT);
		//
		// global configuration for create operation
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_CREATE_OPERATION + PROPERTY_DISABLE_LIMIT);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_CREATE_OPERATION + PROPERTY_DISABLED);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_CREATE_OPERATION + PROPERTY_IDENTITY_RECIPIENTS);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_CREATE_OPERATION + PROPERTY_PERIOD);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_CREATE_OPERATION + PROPERTY_ROLE_RECIPIENTS);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_CREATE_OPERATION + PROPERTY_TEMPLATE_DISABLE);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_CREATE_OPERATION + PROPERTY_TEMPLATE_WARNING);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_CREATE_OPERATION + PROPERTY_WARNING_LIMIT);
		//
		// global configuration for delete operation
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_DELETE_OPERATION + PROPERTY_DISABLE_LIMIT);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_DELETE_OPERATION + PROPERTY_DISABLED);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_DELETE_OPERATION + PROPERTY_IDENTITY_RECIPIENTS);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_DELETE_OPERATION + PROPERTY_PERIOD);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_DELETE_OPERATION + PROPERTY_ROLE_RECIPIENTS);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_DELETE_OPERATION + PROPERTY_TEMPLATE_DISABLE);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_DELETE_OPERATION + PROPERTY_TEMPLATE_WARNING);
		properties.add(GLOBAL_BREAK_PREFIX + GLOBAL_BREAK_DELETE_OPERATION + PROPERTY_WARNING_LIMIT);
		return properties;
	}
	
	/**
	 * Method return global configuration disabled property for given {@link ProvisioningEventType}
	 * 
	 * @param eventType
	 * @return
	 */
	Boolean getDisabled(ProvisioningEventType eventType);
	
	/**
	 * Method return global configuration warning limit property for given {@link ProvisioningEventType}
	 * 
	 * @param eventType
	 * @return
	 */
	Integer getWarningLimit(ProvisioningEventType eventType);
	
	/**
	 * Method return global configuration disable limit property for given {@link ProvisioningEventType}
	 * 
	 * @param eventType
	 * @return
	 */
	Integer getDisableLimit(ProvisioningEventType eventType);
	
	/**
	 * Method return global configuration period property for given {@link ProvisioningEventType}
	 * 
	 * @param eventType
	 * @return
	 */
	Long getPeriod(ProvisioningEventType eventType);
	
	/**
	 * Method return global configuration warning template property for given {@link ProvisioningEventType}
	 * 
	 * @param eventType
	 * @return
	 */
	IdmNotificationTemplateDto getWarningTemplate(ProvisioningEventType eventType);
	
	/**
	 * Method return global configuration disabled template property for given {@link ProvisioningEventType}
	 * 
	 * @param eventType
	 * @return
	 */
	IdmNotificationTemplateDto getDisableTemplate(ProvisioningEventType eventType);
	
	/**
	 * Method return global configuration identity recipients property for given {@link ProvisioningEventType}
	 * 
	 * @param eventType
	 * @return
	 */
	List<IdmIdentityDto> getIdentityRecipients(ProvisioningEventType eventType);
	
	/**
	 * Method return global configuration role recipients property for given {@link ProvisioningEventType}
	 * 
	 * @param eventType
	 * @return
	 */
	List<IdmRoleDto> getRoleRecipients(ProvisioningEventType eventType);
}
