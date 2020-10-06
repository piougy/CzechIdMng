package eu.bcvsolutions.idm.core.model.event.processor.module;

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
import eu.bcvsolutions.idm.core.model.entity.IdmDelegation;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.DelegationByDelegationDefinitionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.DelegationDefByDelegatorAndDelegateEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.DelegationDefinitionByDelegateEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.DelegationDefinitionByDelegatorContractEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.DelegationDefinitionByDelegatorEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.SelfDelegationDefinitionByDelegateEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.SelfDelegationDefinitionByDelegatorEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SelfIdentityEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SubordinatesEvaluator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Init role with permissions for a delegations.
 *
 * @author Vít Švanda
 * @since 10.6.0
 */
@Component(InitDelegationRoleProcessor.PROCESSOR_NAME)
@Description("Init role with permissions for a delegations."
		+ "Role is created, when not exist. "
		+ "Role will not be created, when configuration property is empty (defined, but empty string is given). "
		+ "Role is created with 'SYSTEM' role type - type is checked, when role authorities are created (or updated)."
		+ "Role is placed into role catalogue 'CzechIdM Roles' item, if item is defined.")
public class InitDelegationRoleProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitDelegationRoleProcessor.class);
	public static final String PROCESSOR_NAME = "core-init-delegation-role-processor";
	//
	@Autowired
	private RoleConfiguration roleConfiguration;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private InitRoleCatalogueProcessor initRoleCatalogueProcessor;

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
		IdmRoleDto delegationRole = roleConfiguration.getDelegationRole();
		if (delegationRole == null) {
			String delegationCode = roleConfiguration.getDelegationRoleCode();
			if (delegationCode == null) {
				LOG.warn("Delegation role does not exist and is not configured (by property [{}]). User role will not be created.", RoleConfiguration.PROPERTY_DELEGATION_ROLE);
				//
				return null;
			}
			delegationRole = new IdmRoleDto();
			delegationRole.setCode(delegationCode);
			delegationRole.setName("Delegation");
			delegationRole.setDescription(PRODUCT_PROVIDED_ROLE_DESCRIPTION);
			delegationRole.setRoleType(RoleType.SYSTEM);
			delegationRole = roleService.save(delegationRole);
			//
			// register role into default catalogue
			initRoleCatalogueProcessor.registerRole(delegationRole);
			//
			LOG.info("Delegation role [{}] created.", delegationCode);
		}
		if (delegationRole == null) {
			LOG.debug("Delegation role is not configured. Authorities will not be updated.");
			//
			return null;
		} else if (delegationRole.getRoleType() != RoleType.SYSTEM) {
			LOG.debug("Delegation role has not system type. Authorities will not be updated.");
			//
			return null;
		}

		// Delegation role policies (by https://wiki.czechidm.com/devel/documentation/security/dev/authorization#manager_and_subordinates).

		// Find already configured role policies.
		List<IdmAuthorizationPolicyDto> configuredPolicies = findConfiguredPolicies(delegationRole);

		// Identities - Adds permission for choose a delegator from user's subordinates.
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(
				IdentityBasePermission.DELEGATOR);
		policy.setRole(delegationRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		policy.setAuthorizableType(IdmIdentity.class.getCanonicalName());
		policy.setEvaluator(SubordinatesEvaluator.class);
		savePolicy(configuredPolicies, policy);

		// Identities - Adds permission for choose as a delegator logged user.
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(
				IdentityBasePermission.DELEGATOR);
		policy.setRole(delegationRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		policy.setAuthorizableType(IdmIdentity.class.getCanonicalName());
		policy.setEvaluator(SelfIdentityEvaluator.class);
		savePolicy(configuredPolicies, policy);

		// Identities - Adds permission for choose a delegate. Anyone can be choose by default as delegate!
		policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(
				IdentityBasePermission.DELEGATE);
		policy.setRole(delegationRole.getId());
		policy.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		policy.setAuthorizableType(IdmIdentity.class.getCanonicalName());
		policy.setEvaluator(BasePermissionEvaluator.class);
		savePolicy(configuredPolicies, policy);

		// Delegation definition - User can create or delete delegation if has permission for choose delegator AND delegate in this delegation.
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(delegationRole.getId());
		policy.setPermissions(
				IdmBasePermission.CREATE,
				IdmBasePermission.DELETE);
		policy.setGroupPermission(CoreGroupPermission.DELEGATIONDEFINITION.getName());
		policy.setAuthorizableType(IdmDelegationDefinition.class.getCanonicalName());
		policy.setEvaluator(DelegationDefByDelegatorAndDelegateEvaluator.class);
		savePolicy(configuredPolicies, policy);

		// Delegation definition - Logged user can read or delete his delegations (where is delegate).
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(delegationRole.getId());
		policy.setPermissions(
				IdmBasePermission.READ,
				IdmBasePermission.DELETE);
		policy.setGroupPermission(CoreGroupPermission.DELEGATIONDEFINITION.getName());
		policy.setAuthorizableType(IdmDelegationDefinition.class.getCanonicalName());
		policy.setEvaluator(SelfDelegationDefinitionByDelegateEvaluator.class);
		savePolicy(configuredPolicies, policy);

		// Delegation definition - Logged user can read or delete his delegations (where is delegator).
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(delegationRole.getId());
		policy.setPermissions(
				IdmBasePermission.READ,
				IdmBasePermission.DELETE);
		policy.setGroupPermission(CoreGroupPermission.DELEGATIONDEFINITION.getName());
		policy.setAuthorizableType(IdmDelegationDefinition.class.getCanonicalName());
		policy.setEvaluator(SelfDelegationDefinitionByDelegatorEvaluator.class);
		savePolicy(configuredPolicies, policy);

		// Delegation definition - Adds transitive permissions via delegate. If has logged user UPDATE permission for some identity,
		// then he can create or delete its delegations (where is this identity delegate).
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(delegationRole.getId());
		policy.setGroupPermission(CoreGroupPermission.DELEGATIONDEFINITION.getName());
		policy.setAuthorizableType(IdmDelegationDefinition.class.getCanonicalName());
		policy.setEvaluator(DelegationDefinitionByDelegateEvaluator.class);
		savePolicy(configuredPolicies, policy);

		// Delegation definition - Adds transitive permissions via delegator. If has logged user UPDATE permission for some identity,
		// then he can create or delete its delegations (where is this identity delegator).
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(delegationRole.getId());
		policy.setGroupPermission(CoreGroupPermission.DELEGATIONDEFINITION.getName());
		policy.setAuthorizableType(IdmDelegationDefinition.class.getCanonicalName());
		policy.setEvaluator(DelegationDefinitionByDelegatorEvaluator.class);
		savePolicy(configuredPolicies, policy);

		// Delegation definition - Adds transitive permissions via delegator's contract. If has logged user UPDATE permission for some identity's contract,
		// then he can create or delete its delegations (where is this identity delegator's contract).
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(delegationRole.getId());
		policy.setGroupPermission(CoreGroupPermission.DELEGATIONDEFINITION.getName());
		policy.setAuthorizableType(IdmDelegationDefinition.class.getCanonicalName());
		policy.setEvaluator(DelegationDefinitionByDelegatorContractEvaluator.class);
		savePolicy(configuredPolicies, policy);

		// Delegation instance - Adds transitive permissions via a delegation definition. User has same permissions for instances of delegation as on a definition of delegation.
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(delegationRole.getId());
		policy.setGroupPermission(CoreGroupPermission.DELEGATIONDEFINITION.getName());
		policy.setAuthorizableType(IdmDelegation.class.getCanonicalName());
		policy.setEvaluator(DelegationByDelegationDefinitionEvaluator.class);
		savePolicy(configuredPolicies, policy);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// after user role is created
		return CoreEvent.DEFAULT_ORDER + 50;
	}
}
