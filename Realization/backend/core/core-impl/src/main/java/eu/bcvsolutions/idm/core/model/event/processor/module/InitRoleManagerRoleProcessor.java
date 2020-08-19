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
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRule;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRuleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleFormAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.AuthorizationPolicyByRoleEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.AutomaticRoleRequestByWfInvolvedIdentityEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.AutomaticRoleRuleRequestByRequestEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleCatalogueRoleByRoleEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleCompositionBySubRoleEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleCompositionBySuperiorRoleEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleFormAttributeByRoleEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleGuaranteeByRoleEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleGuaranteeEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleGuaranteeRoleByRoleEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleTreeNodeByRoleEvaluator;

/**
 * Init role manager role (person).
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitRoleManagerRoleProcessor.PROCESSOR_NAME)
@Description("Init role manager role for core module (by configuration 'idm.sec.core.role.roleManager'). "
		+ "Role is created, when not exist. "
		+ "Role will not be created, when configuration property is empty (defined, but empty string is given). "
		+ "Role is created with 'SYSTEM' role type - type is checked, when role authorities are created (or updated)."
		+ "Role is placed into role catalogue 'CzechIdM Roles' item, if item is defined.")
public class InitRoleManagerRoleProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitRoleManagerRoleProcessor.class);
	public static final String PROCESSOR_NAME = "core-init-role-manager-role-processor";
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
		IdmRoleDto roleManagerRole = roleConfiguration.getRoleManagerRole();
		if (roleManagerRole == null) {
			String roleManagerRoleCode = roleConfiguration.getRoleManagerRoleCode();
			if (roleManagerRoleCode == null) {
				LOG.warn("Role manager role does not exist and is not configured (by property [{}]). User role will not be created.", RoleConfiguration.PROPERTY_ROLE_MANAGER_ROLE);
				//
				return null;
			}
			roleManagerRole = new IdmRoleDto();
			roleManagerRole.setCode(roleManagerRoleCode);
			roleManagerRole.setName("Role manager");
			roleManagerRole.setDescription(PRODUCT_PROVIDED_ROLE_DESCRIPTION);
			roleManagerRole.setRoleType(RoleType.SYSTEM);
			roleManagerRole = roleService.save(roleManagerRole);
			// 
			// register role into default catalogue
			initRoleCatalogueProcessor.registerRole(roleManagerRole);
			//
			LOG.info("Role manager role [{}] created.", roleManagerRoleCode);
		}
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
		// guaranteed roles
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(
				IdmBasePermission.READ,
				IdmBasePermission.UPDATE);
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLE.getName());
		policy.setAuthorizableType(IdmRole.class.getCanonicalName());
		policy.setEvaluator(RoleGuaranteeEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// configured role guarantees
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLEGUARANTEE.getName());
		policy.setAuthorizableType(IdmRoleGuarantee.class.getCanonicalName());
		policy.setEvaluator(RoleGuaranteeByRoleEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// configured role guarantees by role
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLEGUARANTEEROLE.getName());
		policy.setAuthorizableType(IdmRoleGuaranteeRole.class.getCanonicalName());
		policy.setEvaluator(RoleGuaranteeRoleByRoleEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// automatic roles (tree)
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLETREENODE.getName());
		policy.setAuthorizableType(IdmRoleTreeNode.class.getCanonicalName());
		policy.setEvaluator(RoleTreeNodeByRoleEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// automatic role by attribute - usable for request
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLETREENODE.getName());
		policy.setAuthorizableType(IdmRoleTreeNode.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// automatic roles (attribute)
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ);
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.AUTOMATICROLEATTRIBUTE.getName());
		policy.setAuthorizableType(IdmAutomaticRoleAttribute.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// automatic roles (attribute - rules)
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ);
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.AUTOMATICROLEATTRIBUTERULE.getName());
		policy.setAuthorizableType(IdmAutomaticRoleAttributeRule.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// request for automatic roles
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ);
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.AUTOMATICROLEREQUEST.getName());
		policy.setAuthorizableType(IdmAutomaticRoleRequest.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		//  read automatic role requests in workflow approval
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.UPDATE);
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.AUTOMATICROLEREQUEST.getName());
		policy.setAuthorizableType(IdmAutomaticRoleRequest.class.getCanonicalName());
		policy.setEvaluator(AutomaticRoleRequestByWfInvolvedIdentityEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// request for automatic roles - rules
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.AUTOMATICROLEATTRIBUTERULEREQUEST.getName());
		policy.setAuthorizableType(IdmAutomaticRoleAttributeRuleRequest.class.getCanonicalName());
		policy.setEvaluator(AutomaticRoleRuleRequestByRequestEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// role authorization policies
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.AUTHORIZATIONPOLICY.getName());
		policy.setAuthorizableType(IdmAuthorizationPolicy.class.getCanonicalName());
		policy.setEvaluator(AuthorizationPolicyByRoleEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// catalogue items by role
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLECATALOGUEROLE.getName());
		policy.setAuthorizableType(IdmRoleCatalogueRole.class.getCanonicalName());
		policy.setEvaluator(RoleCatalogueRoleByRoleEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		//  business roles - superior
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLECOMPOSITION.getName());
		policy.setAuthorizableType(IdmRoleComposition.class.getCanonicalName());
		policy.setEvaluator(RoleCompositionBySuperiorRoleEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		//  business roles - sub
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLECOMPOSITION.getName());
		policy.setAuthorizableType(IdmRoleComposition.class.getCanonicalName());
		policy.setEvaluator(RoleCompositionBySubRoleEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		//  form definitions
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.FORMDEFINITION.getName());
		policy.setAuthorizableType(IdmFormDefinition.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		//  role attributes
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(roleManagerRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLEFORMATTRIBUTE.getName());
		policy.setAuthorizableType(IdmRoleFormAttribute.class.getCanonicalName());
		policy.setEvaluator(RoleFormAttributeByRoleEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// after user role is created
		return CoreEvent.DEFAULT_ORDER + 40;
	}
}
