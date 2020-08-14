package eu.bcvsolutions.idm.core.model.event.processor.module;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
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
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;

/**
 * Init administrator role (person).
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitAdminRoleProcessor.PROCESSOR_NAME)
@Description("Init administrator role (by configuration 'idm.sec.core.role.admin'). "
		+ "Role is created, when no other role exists (=> is the first role in application). "
		+ "Role will not be created, when is deleted and any other role exists. "
		+ "Role will not be created, when configuration property is empty (defined, but empty string is given). "
		+ "Role is created with 'SYSTEM' role type - type is checked, when role authorities are created (or updated).")
public class InitAdminRoleProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitAdminRoleProcessor.class);
	public static final String PROCESSOR_NAME = "core-init-admin-role-processor";
	//
	@Autowired private RoleConfiguration roleConfiguration;
	@Autowired private IdmRoleService roleService;
	@Autowired private InitRoleCatalogueProcessor initRoleCatalogueProcessor;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		//
		// create super admin role
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		if (adminRole == null && roleService.find(PageRequest.of(0, 1)).getTotalElements() == 0) {
			String adminRoleCode = roleConfiguration.getAdminRoleCode();
			if (adminRoleCode == null) {
				LOG.warn("Admin role does not exist and is not configured (by property [{}]). Admin role will not be created.", RoleConfiguration.PROPERTY_ADMIN_ROLE);
				//
				return null;
			}
			//
			adminRole = new IdmRoleDto();
			adminRole.setCode(adminRoleCode);
			adminRole.setRoleType(RoleType.SYSTEM);
			adminRole = roleService.save(adminRole);
			// 
			// register role into default catalogue
			initRoleCatalogueProcessor.registerRole(adminRole);
			//
			LOG.info("Super admin Role created [id: {}]", adminRoleCode);
		}
		if (adminRole == null) {
			LOG.debug("Admin role is not configured or was deleted (other roles exists). Authorities will not be updated.");
			//
			return null;
		} else if (adminRole.getRoleType() != RoleType.SYSTEM) {
			LOG.debug("Admin role has not system type. Authorities will not be updated.");
			//
			return null;
		}
		// find already configured role policies
		List<IdmAuthorizationPolicyDto> configuredPolicies = findConfiguredPolicies(adminRole);
		//
		// app admin authorization policy
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setGroupPermission(IdmGroupPermission.APP.getName());
		policy.setPermissions(IdmBasePermission.ADMIN);
		policy.setRole(adminRole.getId());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// 0 => admin role is created at start
		return CoreEvent.DEFAULT_ORDER;
	}
}
