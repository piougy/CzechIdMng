package eu.bcvsolutions.idm.acc.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;

/**
 * Configuration properties for global provisioning break
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component("provisioningBreakConfiguration")
public class DefaultProvisioningBreakConfiguration extends AbstractConfiguration implements ProvisioningBreakConfiguration {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultProvisioningBreakConfiguration.class);
	
	private final LookupService lookupService;
	
	@Autowired
	public DefaultProvisioningBreakConfiguration(
			LookupService lookupService) {
		//
		Assert.notNull(lookupService);
		//
		this.lookupService = lookupService;
	}
	
	@Override
	public Boolean getDisabled(ProvisioningEventType eventType) {
		return this.getConfigurationBooleanValue(getPrefix(eventType) + PROPERTY_DISABLED);
	}

	@Override
	public Integer getWarningLimit(ProvisioningEventType eventType) {
		return this.getConfigurationIntegerValue(getPrefix(eventType) + PROPERTY_WARNING_LIMIT);
	}

	@Override
	public Integer getDisableLimit(ProvisioningEventType eventType) {
		return this.getConfigurationIntegerValue(getPrefix(eventType) + PROPERTY_DISABLE_LIMIT);
	}

	@Override
	public Long getPeriod(ProvisioningEventType eventType) {
		return this.getConfigurationLongValue(getPrefix(eventType) + PROPERTY_PERIOD);
	}

	@Override
	public IdmNotificationTemplateDto getWarningTemplate(ProvisioningEventType eventType) {
		String templateId = getConfigurationValue(getPrefix(eventType) + PROPERTY_TEMPLATE_WARNING);
		//
		if (templateId == null) {
			return null;
		}
		//
		return (IdmNotificationTemplateDto) lookupService.lookupDto(IdmNotificationTemplateDto.class, templateId);
	}

	@Override
	public IdmNotificationTemplateDto getDisableTemplate(ProvisioningEventType eventType) {
		String templateId = getConfigurationValue(getPrefix(eventType) + PROPERTY_TEMPLATE_DISABLE);
		//
		if (templateId == null) {
			return null;
		}
		//
		return (IdmNotificationTemplateDto) lookupService.lookupDto(IdmNotificationTemplateDto.class, templateId);
	}

	@Override
	public List<IdmIdentityDto> getIdentityRecipients(ProvisioningEventType eventType) {
		List<IdmIdentityDto> recipients = new ArrayList<>();
		String identities = getConfigurationValue(getPrefix(eventType) + PROPERTY_IDENTITY_RECIPIENTS);
		//
		if (identities == null) {
			return recipients;
		}
		//
		for (String identityId : identities.split(REQEX_FOR_RECIPIENTS)) {
			identityId = identityId.trim();
			IdmIdentityDto identityDto = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identityId);
			if (identityDto == null) {
				LOG.error("Identity for id [{}] not found", identityId);
			} else {
				recipients.add(identityDto);
			}
		}
		return recipients;
	}

	@Override
	public List<IdmRoleDto> getRoleRecipients(ProvisioningEventType eventType) {
		List<IdmRoleDto> recipients = new ArrayList<>();
		String roles = getConfigurationValue(getPrefix(eventType) + PROPERTY_ROLE_RECIPIENTS);
		//
		if (roles == null) {
			return recipients;
		}
		//
		for (String roleId : roles.split(REQEX_FOR_RECIPIENTS)) {
			roleId = roleId.trim();
			IdmRoleDto roleDto = (IdmRoleDto) lookupService.lookupDto(IdmRoleDto.class, roleId);
			if (roleDto == null) {
				LOG.error("Role for id [{}] not found", roleId);
			} else {
				recipients.add(roleDto);
			}
		}
		return recipients;
	}
	
	/**
	 * Method return configuration prefix for given operation type
	 * 
	 * @return
	 */
	private String getPrefix(ProvisioningEventType eventType) {
		return eventType.name().toLowerCase() + ConfigurationService.PROPERTY_SEPARATOR;
	}
}
