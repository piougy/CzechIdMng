package eu.bcvsolutions.idm.acc.domain;

import java.util.List;

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

	static final String REQEX_FOR_RECIPIENTS = ",";
	
	static final String PROPERTY_GLOBAL_BREAK_DISABLED = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.break.disabled";
	
	static final String PROPERTY_GLOBAL_BREAK_WARNING_LIMIT = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.break.warningLimit";	
	
	static final String PROPERTY_GLOBAL_BREAK_DISABLE_LIMIT = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.break.disableLimit";	
	
	static final String PROPERTY_GLOBAL_BREAK_OPERATION_DISABLED = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.break.operationDisabled";	
	
	static final String PROPERTY_GLOBAL_BREAK_PERIOD = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.break.period";
	
	static final String PROPERTY_GLOBAL_BREAK_TEMPLATE_WARNING = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.break.templateWarning";	
	
	static final String PROPERTY_GLOBAL_BREAK_TEMPLATE_DISABLE = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.break.templateDisable";	
	
	static final String PROPERTY_GLOBAL_BREAK_IDENTITY_RECIPIENTS = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.break.identityRecipients";
	
	static final String PROPERTY_GLOBAL_BREAK_ROLE_RECIPIENTS = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.break.roleRecipients";

	static final String PROPERTY_GLOBAL_BREAK_OPERATION_TYPE = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.break.operationType";

	
	@Override
	default java.lang.String getConfigurableType() {
		return "provisioning";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = Configurable.super.getPropertyNames();
		properties.add(PROPERTY_GLOBAL_BREAK_DISABLE_LIMIT);
		properties.add(PROPERTY_GLOBAL_BREAK_DISABLED);
		properties.add(PROPERTY_GLOBAL_BREAK_IDENTITY_RECIPIENTS);
		properties.add(PROPERTY_GLOBAL_BREAK_OPERATION_DISABLED);
		properties.add(PROPERTY_GLOBAL_BREAK_PERIOD);
		properties.add(PROPERTY_GLOBAL_BREAK_ROLE_RECIPIENTS);
		properties.add(PROPERTY_GLOBAL_BREAK_TEMPLATE_DISABLE);
		properties.add(PROPERTY_GLOBAL_BREAK_TEMPLATE_WARNING);
		properties.add(PROPERTY_GLOBAL_BREAK_WARNING_LIMIT);
		properties.add(PROPERTY_GLOBAL_BREAK_OPERATION_TYPE);
		return properties;
	}
	
	Boolean getDisabled();
	Integer getWarningLimit();
	Integer getDisableLimit();
	Boolean getOperationDisabled();
	Long getPeriod();
	IdmNotificationTemplateDto getWarningTemplate();
	IdmNotificationTemplateDto getDisableTemplate();
	List<IdmIdentityDto> getIdentityRecipients();
	List<IdmRoleDto> getRoleRecipients();
	ProvisioningOperationType getOperationType();
}
