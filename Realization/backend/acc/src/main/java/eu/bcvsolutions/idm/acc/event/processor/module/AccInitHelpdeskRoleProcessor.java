package eu.bcvsolutions.idm.acc.event.processor.module;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
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
 * Init helpdesk role for acc module.
 * 
 * @see InitHelpdeskRoleProcessor
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(AccInitHelpdeskRoleProcessor.PROCESSOR_NAME)
@Description("Init helpdesk role for acc module (by configuration 'idm.sec.core.role.helpdesk') - "
		+ "adds authorization policies for acc module.")
public class AccInitHelpdeskRoleProcessor extends AbstractInitApplicationProcessor {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccInitHelpdeskRoleProcessor.class);
	public static final String PROCESSOR_NAME = "acc-init-helpdesk-role-processor";
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
		IdmRoleDto helpdeskRole = roleConfiguration.getHelpdeskRole();
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
		// provisioning queue
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ);
		policy.setRole(helpdeskRole.getId());
		policy.setGroupPermission(AccGroupPermission.PROVISIONINGOPERATION.getName());
		policy.setAuthorizableType(SysProvisioningOperation.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// provisioning archive
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ);
		policy.setRole(helpdeskRole.getId());
		policy.setGroupPermission(AccGroupPermission.PROVISIONINGARCHIVE.getName());
		policy.setAuthorizableType(SysProvisioningArchive.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after core user role is created
		return CoreEvent.DEFAULT_ORDER + 35;
	}
}
