package eu.bcvsolutions.idm.core.config.domain;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * Configuration for features with roles.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultRoleConfiguration extends AbstractConfiguration implements RoleConfiguration {	
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultRoleConfiguration.class);
	//
	@Autowired private LookupService lookupService;
	
	@Override
	public String getDefaultRoleCode() {
		String roleCode = getConfigurationService().getValue(PROPERTY_DEFAULT_ROLE, DEFAULT_DEFAULT_ROLE);
		if (StringUtils.isBlank(roleCode)) {
			LOG.debug("Default role is not configuration, returning null. Change configuration [{}]", PROPERTY_DEFAULT_ROLE);
			return null;
		}
		//
		return roleCode;
	}

	@Override
	public UUID getDefaultRoleId() {
		IdmRoleDto role = getDefaultRole();
		return role == null ? null : role.getId(); // warning messages are included getDefaultRole()
	}
	
	@Override
	public IdmRoleDto getDefaultRole() {
		String roleCode = getDefaultRoleCode();
		if (roleCode == null) {
			return null;
		}
		// lookup - uuid or code could be given
		IdmRoleDto role = (IdmRoleDto) lookupService.lookupDto(IdmRoleDto.class, roleCode);
		if (role == null) {
			LOG.warn("Default role with code [{}] not found, returning null. Change configuration [{}]", roleCode, PROPERTY_DEFAULT_ROLE);
			return null;
		}
		return role;
	}
	
	@Override
	public String getAdminRoleCode() {
		String roleCode = getConfigurationService().getValue(PROPERTY_ADMIN_ROLE, DEFAULT_ADMIN_ROLE);
		if (StringUtils.isBlank(roleCode)) {
			LOG.debug("Admin role is not configured, returning null. Change configuration [{}]", PROPERTY_ADMIN_ROLE);
			return null;
		}
		//
		return roleCode;
	}

	@Override
	public UUID getAdminRoleId() {
		IdmRoleDto role = getAdminRole();
		return role == null ? null : role.getId(); // warning messages are included getDefaultRole()
	}
	
	@Override
	public IdmRoleDto getAdminRole() {
		String roleCode = getAdminRoleCode();
		if (roleCode == null) {
			return null;
		}
		// lookup - uuid or code could be given
		IdmRoleDto role = (IdmRoleDto) lookupService.lookupDto(IdmRoleDto.class, roleCode);
		if (role == null) {
			LOG.warn("Admin role with code [{}] not found, returning null. Change configuration [{}]", roleCode, PROPERTY_ADMIN_ROLE);
			return null;
		}
		return role;
	}
	
	@Override
	public String getHelpdeskRoleCode() {
		String roleCode = getConfigurationService().getValue(PROPERTY_HELPDESK_ROLE, DEFAULT_HELPDESK_ROLE);
		if (StringUtils.isBlank(roleCode)) {
			LOG.debug("Helpdesk role is not configured, returning null. Change configuration [{}]", PROPERTY_HELPDESK_ROLE);
			return null;
		}
		//
		return roleCode;
	}
	
	@Override
	public IdmRoleDto getHelpdeskRole() {
		String roleCode = getHelpdeskRoleCode();
		if (roleCode == null) {
			return null;
		}
		// lookup - uuid or code could be given
		IdmRoleDto role = (IdmRoleDto) lookupService.lookupDto(IdmRoleDto.class, roleCode);
		if (role == null) {
			LOG.warn("Helpdesk role with code [{}] not found, returning null. Change configuration [{}]", roleCode, PROPERTY_HELPDESK_ROLE);
			return null;
		}
		return role;
	}
	
	@Override
	public String getUserManagerRoleCode() {
		String roleCode = getConfigurationService().getValue(PROPERTY_USER_MANAGER_ROLE, DEFAULT_USER_MANAGER_ROLE);
		if (StringUtils.isBlank(roleCode)) {
			LOG.debug("User manager role is not configured, returning null. Change configuration [{}]", PROPERTY_USER_MANAGER_ROLE);
			return null;
		}
		//
		return roleCode;
	}
	
	@Override
	public IdmRoleDto getUserManagerRole() {
		String roleCode = getUserManagerRoleCode();
		if (roleCode == null) {
			return null;
		}
		// lookup - uuid or code could be given
		IdmRoleDto role = (IdmRoleDto) lookupService.lookupDto(IdmRoleDto.class, roleCode);
		if (role == null) {
			LOG.warn("User manager role with code [{}] not found, returning null. Change configuration [{}]", roleCode, PROPERTY_USER_MANAGER_ROLE);
			return null;
		}
		return role;
	}
	
	@Override
	public String getRoleManagerRoleCode() {
		String roleCode = getConfigurationService().getValue(PROPERTY_ROLE_MANAGER_ROLE, DEFAULT_ROLE_MANAGER_ROLE);
		if (StringUtils.isBlank(roleCode)) {
			LOG.debug("Role manager role is not configured, returning null. Change configuration [{}]", PROPERTY_ROLE_MANAGER_ROLE);
			return null;
		}
		//
		return roleCode;
	}
	
	@Override
	public IdmRoleDto getRoleManagerRole() {
		String roleCode = getRoleManagerRoleCode();
		if (roleCode == null) {
			return null;
		}
		// lookup - uuid or code could be given
		IdmRoleDto role = (IdmRoleDto) lookupService.lookupDto(IdmRoleDto.class, roleCode);
		if (role == null) {
			LOG.warn("Role manager role with code [{}] not found, returning null. Change configuration [{}]", roleCode, PROPERTY_ROLE_MANAGER_ROLE);
			return null;
		}
		return role;
	}
	
	@Override
	public IdmRoleDto getRoleForApproveChangeOfRole() {
		String roleCode = getConfigurationService().getValue(PROPERTY_APPROVE_ROLE_CHANGE_ROLE, DEFAULT_ADMIN_ROLE);
		if (StringUtils.isBlank(roleCode)) {
			LOG.debug("Role for approve change of role is not configured, returning null. Change configuration [{}]", PROPERTY_APPROVE_ROLE_CHANGE_ROLE);
			return null;
		}
		// lookup - uuid or code could be given
		IdmRoleDto role = (IdmRoleDto) lookupService.lookupDto(IdmRoleDto.class, roleCode);
		if (role == null) {
			LOG.warn("Role for approve change of role with code [{}] not found, returning null. Change configuration [{}]", roleCode, PROPERTY_APPROVE_ROLE_CHANGE_ROLE);
			return null;
		}
		return role;
	}
	
	@Override
	public String getCodeEnvironmentSeperator() {
		return getConfigurationService().getValue(PROPERTY_CODE_ENVIRONMENT_SEPARATOR, DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
	}
}
