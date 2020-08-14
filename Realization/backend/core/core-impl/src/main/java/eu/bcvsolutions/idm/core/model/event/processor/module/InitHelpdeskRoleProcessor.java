package eu.bcvsolutions.idm.core.model.event.processor.module;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;

/**
 * Init helpdesk role (person).
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitHelpdeskRoleProcessor.PROCESSOR_NAME)
@Description("Init helpdesk role for core module (by configuration 'idm.sec.core.role.helpdesk'). "
		+ "Role is created, when not exist. "
		+ "Role will not be created, when configuration property is empty (defined, but empty string is given). "
		+ "Role is created with 'SYSTEM' role type - type is checked, when role authorities are created (or updated)."
		+ "Role is placed into role catalogue 'CzechIdM Roles' item, if item is defined.")
public class InitHelpdeskRoleProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitHelpdeskRoleProcessor.class);
	public static final String PROCESSOR_NAME = "core-init-helpdesk-role-processor";
	//
	@Autowired private RoleConfiguration roleConfiguration;
	@Autowired private IdmRoleService roleService;
	@Autowired private InitRoleCatalogueProcessor initRoleCatalogueProcessor;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<ModuleDescriptorDto> event) {
		return super.conditional(event) && isInitDataEnabled();
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		IdmRoleDto helpdeskRole = roleConfiguration.getHelpdeskRole();
		if (helpdeskRole == null) {
			String helpdeskRoleCode = roleConfiguration.getHelpdeskRoleCode();
			if (helpdeskRoleCode == null) {
				LOG.warn("Helpdesk role does not exist and is not configured (by property [{}]). User role will not be created.", RoleConfiguration.PROPERTY_HELPDESK_ROLE);
				//
				return null;
			}
			helpdeskRole = new IdmRoleDto();
			helpdeskRole.setCode(helpdeskRoleCode);
			helpdeskRole.setRoleType(RoleType.SYSTEM);
			helpdeskRole = roleService.save(helpdeskRole);
			// 
			// register role into default catalogue
			initRoleCatalogueProcessor.registerRole(helpdeskRole);
			//
			LOG.info("Helpdesk role [{}] created.", helpdeskRoleCode);
		}
		if (helpdeskRole == null) {
			LOG.debug("Helpdesk role is not configured. Authorities will not be updated.");
			//
			return null;
		} else if (helpdeskRole.getRoleType() != RoleType.SYSTEM) {
			LOG.debug("Helpdesk role has not system type. Authorities will not be updated.");
			//
			return null;
		}
		//
		// Helpdesk role policies (by https://wiki.czechidm.com/devel/documentation/security/dev/authorization#settings_of_permissions_for_the_helpdesk_role).
		//
		// find already configured role policies
		List<IdmAuthorizationPolicyDto> configuredPolicies = findConfiguredPolicies(helpdeskRole);
		//
		// identities
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(
				IdmBasePermission.AUTOCOMPLETE, 
				IdmBasePermission.READ, 
				IdmBasePermission.COUNT, 
				IdentityBasePermission.PASSWORDCHANGE);
		policy.setRole(helpdeskRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		policy.setAuthorizableType(IdmIdentity.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		//  read audit
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ);
		policy.setRole(helpdeskRole.getId());
		policy.setGroupPermission(CoreGroupPermission.AUDIT.getName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// notifications
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ);
		policy.setRole(helpdeskRole.getId());
		policy.setGroupPermission(NotificationGroupPermission.NOTIFICATION.getName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// after user role is created
		return CoreEvent.DEFAULT_ORDER + 30;
	}
}
