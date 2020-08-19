package eu.bcvsolutions.idm.acc.event.processor.module;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.security.evaluator.IdentityAccountByAccountEvaluator;
import eu.bcvsolutions.idm.acc.security.evaluator.ReadAccountByIdentityEvaluator;
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
import eu.bcvsolutions.idm.core.model.event.processor.module.InitUserRoleProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;

/**
 * Init user role for acc module.
 * 
 * @see InitUserRoleProcessor
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(AccInitUserRoleProcessor.PROCESSOR_NAME)
@Description("Init user role for acc module (by configuration 'idm.sec.core.role.default') - adds authorization policies for acc module.")
public class AccInitUserRoleProcessor extends AbstractInitApplicationProcessor {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccInitUserRoleProcessor.class);
	public static final String PROCESSOR_NAME = "acc-init-user-role-processor";
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
		IdmRoleDto userRole = roleConfiguration.getDefaultRole();
		if (userRole == null) {
			LOG.debug("User role is not configured. Authorities will not be updated.");
			//
			return null;
		} else if (userRole.getRoleType() != RoleType.SYSTEM) {
			LOG.debug("User role has not system type. Authorities will not be updated.");
			//
			return null;
		}
		//
		// User role policies (by https://wiki.czechidm.com/devel/documentation/security/dev/authorization#default_settings_of_permissions_for_an_identity_profile).
		//
		// find already configured role policies
		List<IdmAuthorizationPolicyDto> configuredPolicies = findConfiguredPolicies(userRole);
		//
		// account
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setRole(userRole.getId());
		policy.setGroupPermission(AccGroupPermission.ACCOUNT.getName());
		policy.setAuthorizableType(AccAccount.class.getCanonicalName());
		policy.setEvaluator(ReadAccountByIdentityEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// identity account
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(userRole.getId());
		policy.setGroupPermission(AccGroupPermission.IDENTITYACCOUNT.getName());
		policy.setAuthorizableType(AccIdentityAccount.class.getCanonicalName());
		policy.setEvaluator(IdentityAccountByAccountEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// target systems
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(AccGroupPermission.SYSTEM.getName());
		// TODO: #799
		// policy.setAuthorizableType(SysSystem.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after core user role is created
		return CoreEvent.DEFAULT_ORDER + 15;
	}
}
