package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.event.AuthorizationPolicyEvent;
import eu.bcvsolutions.idm.core.model.event.AuthorizationPolicyEvent.AuthorizationPolicyEventType;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Duplicate role - duplicate / update role authorization policies.
 * 
 * @author Radek Tomiška
 * @since 10.7.0
 */
@Component(DuplicateRoleAuthorizationPolicyProcessor.PROCESSOR_NAME)
@Description("Duplicate role - duplicate / update role authorization policies.")
public class DuplicateRoleAuthorizationPolicyProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "core-duplicate-role-authorization-policy-processor";
	public static final String PARAMETER_INCLUDE_ROLE_AUTHORIZATION_POLICY = "include-role-authorization-policy";
	//
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	
	public DuplicateRoleAuthorizationPolicyProcessor() {
		super(RoleEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto include = new IdmFormAttributeDto(
				PARAMETER_INCLUDE_ROLE_AUTHORIZATION_POLICY,
				"Duplicate role authorization policies", 
				PersistentType.BOOLEAN);
		include.setDefaultValue(Boolean.TRUE.toString());
		//
		return Lists.newArrayList(include);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmRoleDto> event) {
		return super.conditional(event) 
				&& getBooleanProperty(PARAMETER_INCLUDE_ROLE_AUTHORIZATION_POLICY, event.getProperties());
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto duplicate = event.getContent(); // newly set role
		IdmRoleDto originalSource = event.getOriginalSource(); // cloned role
		Assert.notNull(originalSource.getId(), "Original source identifier is required."); // just for sure
		//
		// find current, create new and delete not present authorization policies
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
		filter.setRoleId(duplicate.getId());
		List<IdmAuthorizationPolicyDto> currentPolicies = Lists.newArrayList(
				authorizationPolicyService.find(filter, null).getContent()
		);
		// and create new authorization policies
		filter.setRoleId(originalSource.getId());
		authorizationPolicyService
			.find(filter, null)
			.filter(policy -> findCurrent(currentPolicies, policy) == null) // find and remove from list => processed
			.forEach(policy -> {				
				policy.setId(null);
				DtoUtils.clearAuditFields(policy);
				policy.setRole(duplicate.getId());
				//
				EntityEvent<IdmAuthorizationPolicyDto> subEvent = new AuthorizationPolicyEvent(
						AuthorizationPolicyEventType.CREATE, 
						policy
				);
				subEvent.setPriority(PriorityType.IMMEDIATE); // we want to be sync (same as other, but no reason now)
				//
				authorizationPolicyService.publish(subEvent, event);
			});
		//
		// remove not found (~not present in original) policies
		currentPolicies.forEach(policy -> {
			EntityEvent<IdmAuthorizationPolicyDto> subEvent = new AuthorizationPolicyEvent(
					AuthorizationPolicyEventType.DELETE, 
					policy
			);
			subEvent.setPriority(PriorityType.IMMEDIATE); // we want to be sync (same as other, but no reason now)
			//
			authorizationPolicyService.publish(subEvent, event);
		});
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 60;
	}
	
    private IdmAuthorizationPolicyDto findCurrent(
    		List<IdmAuthorizationPolicyDto> currentPolicies, 
    		IdmAuthorizationPolicyDto originalPolicy) {
    	for (int index = 0; index < currentPolicies.size(); index++) {
    		IdmAuthorizationPolicyDto policy = currentPolicies.get(index);
    		//
    		// all properties have to be the same
    		if (authorizationPolicyService.hasSameConfiguration(originalPolicy, policy)) {
    			return currentPolicies.remove(index);
    		}
    	}
    	// not found
    	return null;
    }
}
