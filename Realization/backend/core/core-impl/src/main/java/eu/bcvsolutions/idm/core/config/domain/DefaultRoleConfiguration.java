package eu.bcvsolutions.idm.core.config.domain;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Configuration for features with identities.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultRoleConfiguration extends AbstractConfiguration implements RoleConfiguration {	
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultRoleConfiguration.class);
	private final LookupService lookupService;
	
	@Autowired
	public DefaultRoleConfiguration(LookupService lookupService) {
		Assert.notNull(lookupService);
		//
		this.lookupService = lookupService;
	}

	@Override
	public UUID getDefaultRoleId() {
		String roleCode = getConfigurationService().getValue(PROPERTY_DEFAULT_ROLE, DEFAULT_DEFAULT_ROLE);
		if (StringUtils.isBlank(roleCode)) {
			LOG.debug("Default role is not configuration, returning null. Change configuration [{}]", PROPERTY_DEFAULT_ROLE);
			return null;
		}
		// lookup - uuid or code could be given
		IdmRole role = (IdmRole) lookupService.lookupEntity(IdmRole.class, roleCode);
		if (role == null) {
			LOG.warn("Default role with code [{}] not found, returning null. Change configuration [{}]", roleCode, PROPERTY_DEFAULT_ROLE);
			return null;
		}
		return role.getId();
	}

	@Override
	public UUID getAdminRoleId() {
		String roleCode = getConfigurationService().getValue(PROPERTY_ADMIN_ROLE, DEFAULT_ADMIN_ROLE);
		if (StringUtils.isBlank(roleCode)) {
			LOG.debug("Admin role is not configured, returning null. Change configuration [{}]", PROPERTY_ADMIN_ROLE);
			return null;
		}
		// lookup - uuid or code could be given
		IdmRole role = (IdmRole) lookupService.lookupEntity(IdmRole.class, roleCode);
		if (role == null) {
			LOG.warn("Admin role with code [{}] not found, returning null. Change configuration [{}]", roleCode, PROPERTY_ADMIN_ROLE);
			return null;
		}
		return role.getId();
	}
	
	
}
