package eu.bcvsolutions.idm.core.model.event.processor.module;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
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
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeList;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeListItem;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmProfile;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.security.api.domain.ContractBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.ContractGuaranteeByIdentityContractEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.ContractPositionByIdentityContractEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.IdentityContractByIdentityEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.IdentityRoleByIdentityEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.IdentityRoleByRoleEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SelfIdentityEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.profile.SelfProfileEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleCanBeRequestedEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleRequestByIdentityEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleRequestByWfInvolvedIdentityEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.SelfRoleRequestEvaluator;

/**
 * Init user role (person). User role is configured as default role.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitUserRoleProcessor.PROCESSOR_NAME)
@Description("Init user role for core module (by configuration 'idm.sec.core.role.default'). "
		+ "Role is created, when not exist. "
		+ "Role will not be created, when configuration property is empty (defined, but empty string is given). "
		+ "Role is created with 'SYSTEM' role type - type is checked, when role authorities are created (or updated)."
		+ "Role is placed into role catalogue 'CzechIdM Roles' item, if item is defined.")
public class InitUserRoleProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitUserRoleProcessor.class);
	public static final String PROCESSOR_NAME = "core-init-user-role-processor";
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
		IdmRoleDto userRole = roleConfiguration.getDefaultRole();
		if (userRole == null) {
			String userRoleCode = roleConfiguration.getDefaultRoleCode();
			if (userRoleCode == null) {
				LOG.warn("User role does not exist and is not configured (by property [{}]). User role will not be created.", RoleConfiguration.PROPERTY_DEFAULT_ROLE);
				//
				return null;
			}
			userRole = new IdmRoleDto();
			userRole.setCode(userRoleCode);
			userRole.setName("User role");
			userRole.setDescription(PRODUCT_PROVIDED_ROLE_DESCRIPTION);
			userRole.setRoleType(RoleType.SYSTEM);
			userRole = roleService.save(userRole);
			// 
			// register role into default catalogue
			initRoleCatalogueProcessor.registerRole(userRole);
			//
			LOG.info("User role [{}] created.", userRoleCode);
		}
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
		// self policy
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(
				IdmBasePermission.AUTOCOMPLETE, 
				IdmBasePermission.READ, 
				IdentityBasePermission.PASSWORDCHANGE, 
				IdentityBasePermission.CHANGEPERMISSION);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		policy.setAuthorizableType(IdmIdentity.class.getCanonicalName());
		policy.setEvaluator(SelfIdentityEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// read identity roles by identity
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITYROLE.getName());
		policy.setAuthorizableType(IdmIdentityRole.class.getCanonicalName());
		policy.setEvaluator(IdentityRoleByIdentityEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// roles that can be requested
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(RoleBasePermission.CANBEREQUESTED);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLE.getName());
		policy.setAuthorizableType(IdmRole.class.getCanonicalName());
		policy.setEvaluator(RoleCanBeRequestedEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// request roles by copy them from other identity 
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITYROLE.getName());
		policy.setAuthorizableType(IdmIdentityRole.class.getCanonicalName());
		policy.setEvaluator(IdentityRoleByRoleEvaluator.class);
		ConfigurationMap properties = new ConfigurationMap();
		properties.put(IdentityRoleByRoleEvaluator.PARAMETER_CAN_BE_REQUESTED_ONLY, true);
		policy.setEvaluatorProperties(properties);
		savePolicy(configuredPolicies, policy);
		//
		// read identity contracts by identity
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITYCONTRACT.getName());
		policy.setAuthorizableType(IdmIdentityContract.class.getCanonicalName());
		policy.setEvaluator(IdentityContractByIdentityEvaluator.class);
		properties = new ConfigurationMap();
		properties.put(
				IdentityContractByIdentityEvaluator.PARAMETER_INCLUDE_PERMISSIONS, 
				StringUtils.join(Lists.newArrayList(IdmBasePermission.AUTOCOMPLETE.getName(), IdmBasePermission.READ.getName(), ContractBasePermission.CHANGEPERMISSION.getName()), ",")
		);
		policy.setEvaluatorProperties(properties);
		savePolicy(configuredPolicies, policy);
		//
		// read contract positions by contract
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.CONTRACTPOSITION.getName());
		policy.setAuthorizableType(IdmContractPosition.class.getCanonicalName());
		policy.setEvaluator(ContractPositionByIdentityContractEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// read contract guarantees by identity contract
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.CONTRACTGUARANTEE.getName());
		policy.setAuthorizableType(IdmContractGuarantee.class.getCanonicalName());
		policy.setEvaluator(ContractGuaranteeByIdentityContractEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// self role requests
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLEREQUEST.getName());
		policy.setAuthorizableType(IdmRoleRequest.class.getCanonicalName());
		policy.setEvaluator(SelfRoleRequestEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// role requests by identity
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLEREQUEST.getName());
		policy.setAuthorizableType(IdmRoleRequest.class.getCanonicalName());
		policy.setEvaluator(RoleRequestByIdentityEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// role requests in approval
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ, IdmBasePermission.UPDATE, IdmBasePermission.CREATE, IdmBasePermission.DELETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLEREQUEST.getName());
		policy.setAuthorizableType(IdmRoleRequest.class.getCanonicalName());
		policy.setEvaluator(RoleRequestByWfInvolvedIdentityEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// workflow task read and execute
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ, IdmBasePermission.EXECUTE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.WORKFLOWTASK.getName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// read and change self identity profile
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.READ, IdmBasePermission.UPDATE, IdmBasePermission.CREATE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.PROFILE.getName());
		policy.setAuthorizableType(IdmProfile.class.getCanonicalName());
		policy.setEvaluator(SelfProfileEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		setAutocompletePolicies(configuredPolicies, userRole);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Autocomplete - base usage in selectbox through application.
	 */
	private void setAutocompletePolicies(List<IdmAuthorizationPolicyDto> configuredPolicies, IdmRoleDto userRole) {
		//
		// identities
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		policy.setAuthorizableType(IdmIdentity.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy); 
		//
		// profiles
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.PROFILE.getName());
		policy.setAuthorizableType(IdmProfile.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy); 
		//
		// roles
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLE.getName());
		policy.setAuthorizableType(IdmRole.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy); 
		//
		// role catalogue
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.ROLECATALOGUE.getName());
		policy.setAuthorizableType(IdmRoleCatalogue.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy); 
		//
		// contracts
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITYCONTRACT.getName());
		policy.setAuthorizableType(IdmIdentityContract.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy); 
		//
		// tree types
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.TREETYPE.getName());
		policy.setAuthorizableType(IdmTreeType.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// tree nodes
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.TREENODE.getName());
		policy.setAuthorizableType(IdmTreeNode.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);
		//
		// scheduler / LRT
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.SCHEDULER.getName());
		policy.setAuthorizableType(IdmLongRunningTask.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy); 
		//
		// code lists 
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.CODELIST.getName());
		policy.setAuthorizableType(IdmCodeList.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy); 
		//
		// code list items
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		policy.setRole(userRole.getId());
		policy.setGroupPermission(CoreGroupPermission.CODELISTITEM.getName());
		policy.setAuthorizableType(IdmCodeListItem.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy); 
	}

	@Override
	public int getOrder() {
		// after admin role is created
		return CoreEvent.DEFAULT_ORDER + 10;
	}
}
