package eu.bcvsolutions.idm.acc.event.processor.module;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccRoleAccount;
import eu.bcvsolutions.idm.acc.security.evaluator.RoleAccountByRoleEvaluator;
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
import eu.bcvsolutions.idm.core.model.event.processor.module.InitHelpdeskRoleProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;

/**
 * Init role manager role for acc module.
 * 
 * @see InitHelpdeskRoleProcessor
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(AccInitRoleManagerRoleProcessor.PROCESSOR_NAME)
@Description("Init role manager role for acc module  (by configuration 'idm.sec.core.role.roleManager') - "
		+ "adds authorization policies for acc module.")
public class AccInitRoleManagerRoleProcessor extends AbstractInitApplicationProcessor {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccInitRoleManagerRoleProcessor.class);
	public static final String PROCESSOR_NAME = "acc-init-role-manager-role-processor";
	//
	@Autowired private RoleConfiguration roleConfiguration;
	
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
		IdmRoleDto roleManagerRole = roleConfiguration.getRoleManagerRole();
		if (roleManagerRole == null) {
			LOG.debug("Role manager role is not configured. Authorities will not be updated.");
			//
			return null;
		} else if (roleManagerRole.getRoleType() != RoleType.SYSTEM) {
			LOG.debug("Role manager role has not system type. Authorities will not be updated.");
			//
			return null;
		}
		//
		// Role manager role policies (by https://wiki.czechidm.com/devel/documentation/security/dev/authorization#default_settings_of_permissions_for_a_role_detail).
		//
		// find already configured role policies
		List<IdmAuthorizationPolicyDto> configuredPolicies = findConfiguredPolicies(roleManagerRole);
		//
		// account
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ);
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(AccGroupPermission.ACCOUNT.getName());
		policy.setAuthorizableType(AccAccount.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// role account
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(AccGroupPermission.ROLEACCOUNT.getName());
		policy.setAuthorizableType(AccRoleAccount.class.getCanonicalName());
		policy.setEvaluator(RoleAccountByRoleEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after core role manager role is created
		return CoreEvent.DEFAULT_ORDER + 45;
	}
}
