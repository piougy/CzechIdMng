package eu.bcvsolutions.idm.core.api.event.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Objects;

import eu.bcvsolutions.idm.core.api.AppModule;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.ModuleDescriptorEvent.ModuleDescriptorEventType;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;

/**
 * Application init - create init data.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
public abstract class AbstractInitApplicationProcessor 
		extends CoreEventProcessor<ModuleDescriptorDto> 
		implements ModuleProcessor {
	
	public static final String PROPERTY_INIT_DATA_ENABLED = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.init.data.enabled";
	public static final boolean DEFAULT_INIT_DATA_ENABLED = true;
	protected static final String PRODUCT_PROVIDED_ROLE_DESCRIPTION = "Product provided role.";
	//
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;

	public AbstractInitApplicationProcessor() {
		// listen app module (~ application) init event
		super(ModuleDescriptorEventType.INIT);
	}
	
	@Override
	public boolean conditional(EntityEvent<ModuleDescriptorDto> event) {
		// listen app module (~ application) init event
		return super.conditional(event) && event.getContent().getModule().equals(AppModule.MODULE_ID);
	}
	
	/**
	 * Create and update init data after application starts.
	 * 
	 * @return
	 */
	protected boolean isInitDataEnabled() {
		return getConfigurationService().getBooleanValue(PROPERTY_INIT_DATA_ENABLED, DEFAULT_INIT_DATA_ENABLED);
	}
	
	/**
	 * Find all configured role policies (disabled included).
	 * 
	 * @param role related role
	 * @return configured policies
	 */
	protected List<IdmAuthorizationPolicyDto> findConfiguredPolicies(IdmRoleDto role) {
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
		filter.setRoleId(role.getId());
		return authorizationPolicyService.find(filter, null).getContent();
	}
	
	/**
	 * Create or update autorization policy by authorizable type (~ group permission) and evaluator type.
	 * 
	 * @param configuredPolicies already configured policies.
	 * @param policy new policy setting
	 */
	protected void savePolicy(List<IdmAuthorizationPolicyDto> configuredPolicies, IdmAuthorizationPolicyDto policy) {
		IdmAuthorizationPolicyDto configuredPolicy = isConfigured(configuredPolicies, policy);
		if (configuredPolicy == null) {
			// create policy
			authorizationPolicyService.save(policy);
			return;
		}
		// update policy if needed.
		if (hasSameConfiguration(configuredPolicy, policy)) {
			return;
		}
		configuredPolicy.setBasePermissions(policy.getBasePermissions());
		configuredPolicy.setEvaluatorProperties(policy.getEvaluatorProperties());
		//
		authorizationPolicyService.save(configuredPolicy);
	}
	
	/**
	 * Is configured by authorizable type (~ group permission) and evaluator type.
	 * 
	 * @param configuredPolicies
	 * @param policy
	 * @return
	 */
	protected IdmAuthorizationPolicyDto isConfigured(List<IdmAuthorizationPolicyDto> configuredPolicies, IdmAuthorizationPolicyDto policy) {
		return configuredPolicies
			.stream()
			.filter(one -> {
				// policies are configured the same way
				return Objects.equal(one.getAuthorizableType(), policy.getAuthorizableType())
						&& Objects.equal(one.getEvaluatorType(), policy.getEvaluatorType())
						&& Objects.equal(one.getGroupPermission(), policy.getGroupPermission());
			})
			.findFirst()
			.orElse(null);
	}
	
	/**
	 * Same permissions and configured properties
	 * 
	 * @param configuredPolicy
	 * @param policy
	 * @return
	 */
	protected boolean hasSameConfiguration(IdmAuthorizationPolicyDto configuredPolicy, IdmAuthorizationPolicyDto policy) {
		return Objects.equal(configuredPolicy.getPermissions(), policy.getPermissions()) // set => order doesn't matter
				&& Objects.equal(configuredPolicy.getEvaluatorProperties(), policy.getEvaluatorProperties());
	}
	
}
