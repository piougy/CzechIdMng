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
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.security.api.domain.ContractBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.identity.IdentityRoleByContractEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SubordinateContractEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SubordinatesEvaluator;

/**
 * Init user manager role (person).
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitUserManagerRoleProcessor.PROCESSOR_NAME)
@Description("Init user manager role for core module (by configuration 'idm.sec.core.role.userManager'). "
		+ "Role is created, when not exist. "
		+ "Role will not be created, when configuration property is empty (defined, but empty string is given). "
		+ "Role is created with 'SYSTEM' role type - type is checked, when role authorities are created (or updated)."
		+ "Role is placed into role catalogue 'CzechIdM Roles' item, if item is defined.")
public class InitUserManagerRoleProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitUserManagerRoleProcessor.class);
	public static final String PROCESSOR_NAME = "core-init-user-manager-role-processor";
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
		IdmRoleDto userManagerRole = roleConfiguration.getUserManagerRole();
		if (userManagerRole == null) {
			String userManagerRoleCode = roleConfiguration.getUserManagerRoleCode();
			if (userManagerRoleCode == null) {
				LOG.warn("User manager role does not exist and is not configured (by property [{}]). User role will not be created.", RoleConfiguration.PROPERTY_USER_MANAGER_ROLE);
				//
				return null;
			}
			userManagerRole = new IdmRoleDto();
			userManagerRole.setCode(userManagerRoleCode);
			userManagerRole.setRoleType(RoleType.SYSTEM);
			userManagerRole = roleService.save(userManagerRole);
			//
			// register role into default catalogue
			initRoleCatalogueProcessor.registerRole(userManagerRole);
			//
			LOG.info("User manager role [{}] created.", userManagerRoleCode);
		}
		if (userManagerRole == null) {
			LOG.debug("User manager role is not configured. Authorities will not be updated.");
			//
			return null;
		} else if (userManagerRole.getRoleType() != RoleType.SYSTEM) {
			LOG.debug("User manager role has not system type. Authorities will not be updated.");
			//
			return null;
		}
		//
		// User manager role policies (by https://wiki.czechidm.com/devel/documentation/security/dev/authorization#manager_and_subordinates).
		//
		// find already configured role policies
		List<IdmAuthorizationPolicyDto> configuredPolicies = findConfiguredPolicies(userManagerRole);
		//
		// identities
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(
				IdmBasePermission.AUTOCOMPLETE, 
				IdmBasePermission.READ);
		policy.setRole(userManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		policy.setAuthorizableType(IdmIdentity.class.getCanonicalName());
		policy.setEvaluator(SubordinatesEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// contracts
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(
				IdmBasePermission.AUTOCOMPLETE, 
				IdmBasePermission.READ,
				ContractBasePermission.CHANGEPERMISSION);
		policy.setRole(userManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITYCONTRACT.getName());
		policy.setAuthorizableType(IdmIdentityContract.class.getCanonicalName());
		policy.setEvaluator(SubordinateContractEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// assigned roles
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(userManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITYROLE.getName());
		policy.setAuthorizableType(IdmIdentityRole.class.getCanonicalName());
		policy.setEvaluator(IdentityRoleByContractEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// after user role is created
		return CoreEvent.DEFAULT_ORDER + 50;
	}
}
