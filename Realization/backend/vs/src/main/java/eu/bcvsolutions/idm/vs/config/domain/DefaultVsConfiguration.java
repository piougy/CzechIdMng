package eu.bcvsolutions.idm.vs.config.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * Virtual system configuration - implementation
 * 
 * @author Svanda
 *
 */
@Component("vsConfiguration")
public class DefaultVsConfiguration 
		extends AbstractConfiguration
		implements VsConfiguration {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultVsConfiguration.class);
	private final LookupService lookupService;
	
	@Autowired
	public DefaultVsConfiguration(LookupService lookupService) {
		Assert.notNull(lookupService);
		//
		this.lookupService = lookupService;
	}

	
	
	@Override
	public IdmRoleDto getDefaultRole() {
		String roleCode = getConfigurationService().getValue(PROPERTY_DEFAULT_ROLE, DEFAULT_ROLE);
		if (StringUtils.isBlank(roleCode)) {
			LOG.debug("Default role for virtual systems is not configuration, returning null. Change configuration [{}]", PROPERTY_DEFAULT_ROLE);
			return null;
		}
		IdmRoleDto role = (IdmRoleDto) lookupService.lookupDto(IdmRoleDto.class, roleCode);
		if (role == null) {
			LOG.warn("Default role for virtual systems with code [{}] not found, returning null. Change configuration [{}]", roleCode, PROPERTY_DEFAULT_ROLE);
			return null;
		}
		return role;
	}
}
