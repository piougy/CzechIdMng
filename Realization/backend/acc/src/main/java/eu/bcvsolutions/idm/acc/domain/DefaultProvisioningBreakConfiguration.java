package eu.bcvsolutions.idm.acc.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;

@Component("provisioningBreakConfiguration")
public class DefaultProvisioningBreakConfiguration extends AbstractConfiguration implements ProvisioningBreakConfiguration {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultProvisioningBreakConfiguration.class);
	
	private final IdmIdentityService identityService;
	private final IdmRoleService roleService;
	private final IdmNotificationTemplateService notificationTemplateService;
	
	@Autowired
	public DefaultProvisioningBreakConfiguration(
			IdmIdentityService identityService,
			IdmRoleService roleService,
			IdmNotificationTemplateService notificationTemplateService) {
		//
		Assert.notNull(identityService);
		Assert.notNull(roleService);
		Assert.notNull(notificationTemplateService);
		//
		this.identityService = identityService;
		this.roleService = roleService;
		this.notificationTemplateService = notificationTemplateService;
	}
	
	@Override
	public Boolean getDisabled() {
		return this.getConfigurationBooleanValue(PROPERTY_GLOBAL_BREAK_DISABLED);
	}

	@Override
	public Integer getWarningLimit() {
		return this.getConfigurationIntegerValue(PROPERTY_GLOBAL_BREAK_WARNING_LIMIT);
	}

	@Override
	public Integer getDisableLimit() {
		return this.getConfigurationIntegerValue(PROPERTY_GLOBAL_BREAK_DISABLE_LIMIT);
	}

	@Override
	public Boolean getOperationDisabled() {
		return this.getConfigurationBooleanValue(PROPERTY_GLOBAL_BREAK_OPERATION_DISABLED);
	}

	@Override
	public Long getPeriod() {
		return this.getConfigurationLongValue(PROPERTY_GLOBAL_BREAK_PERIOD);
	}

	@Override
	public IdmNotificationTemplateDto getWarningTemplate() {
		String templateId = getConfigurationValue(PROPERTY_GLOBAL_BREAK_TEMPLATE_WARNING);
		//
		return notificationTemplateService.get(templateId);
	}

	@Override
	public IdmNotificationTemplateDto getDisableTemplate() {
		String templateId = getConfigurationValue(PROPERTY_GLOBAL_BREAK_TEMPLATE_DISABLE);
		//
		return  notificationTemplateService.get(templateId);
	}

	@Override
	public List<IdmIdentityDto> getIdentityRecipients() {
		List<IdmIdentityDto> recipients = new ArrayList<>();
		String identities = getConfigurationValue(PROPERTY_GLOBAL_BREAK_IDENTITY_RECIPIENTS);
		//
		for (String identityId : identities.split(REQEX_FOR_RECIPIENTS)) {
			IdmIdentityDto identityDto = identityService.get(identityId);
			if (identityDto == null) {
				LOG.error("Identity for id [{}] not found", identityId);
			} else {
				recipients.add(identityDto);
			}
		}
		return recipients;
	}

	@Override
	public List<IdmRoleDto> getRoleRecipients() {
		List<IdmRoleDto> recipients = new ArrayList<>();
		String roles = getConfigurationValue(PROPERTY_GLOBAL_BREAK_ROLE_RECIPIENTS);
		//
		for (String roleId : roles.split(REQEX_FOR_RECIPIENTS)) {
			roleId = roleId.trim();
			IdmRoleDto roleDto = roleService.get(roleId);
			if (roleDto == null) {
				LOG.error("Role for id [{}] not found", roleId);
			} else {
				recipients.add(roleDto);
			}
		}
		return recipients;
	}

	@Override
	public ProvisioningOperationType getOperationType() {
		String operationType = getConfigurationValue(PROPERTY_GLOBAL_BREAK_OPERATION_TYPE);
		return ProvisioningOperationType.valueOf(operationType);
	}

}
