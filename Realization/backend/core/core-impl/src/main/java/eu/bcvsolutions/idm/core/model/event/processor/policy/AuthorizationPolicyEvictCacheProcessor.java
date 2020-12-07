package eu.bcvsolutions.idm.core.model.event.processor.policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.model.event.AuthorizationPolicyEvent.AuthorizationPolicyEventType;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;

/**
 * Clear authorization policy caches, when authorization policies are changed (CUD).
 * 
 * @author Radek Tomiška
 * @since 10.4.1
 */
@Component(AuthorizationPolicyEvictCacheProcessor.PROCESSOR_NAME)
@Description("Clear authorization policy caches, when authorization policies are changed (CUD).")
public class AuthorizationPolicyEvictCacheProcessor extends CoreEventProcessor<IdmAuthorizationPolicyDto> {

	public static final String PROCESSOR_NAME = "core-authorization-policy-evict-cache-processor";
	//
	@Autowired private IdmCacheManager cacheManager;

	public AuthorizationPolicyEvictCacheProcessor() {
		super(AuthorizationPolicyEventType.CREATE, AuthorizationPolicyEventType.UPDATE, AuthorizationPolicyEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmAuthorizationPolicyDto> process(EntityEvent<IdmAuthorizationPolicyDto> event) {
		// cached permissions
		cacheManager.evictCache(AuthorizationManager.PERMISSION_CACHE_NAME);
		// cached configured authorization policies
		cacheManager.evictCache(AuthorizationManager.AUTHORIZATION_POLICY_DEFINITION_CACHE_NAME);
		// cached identity authorization policies
		cacheManager.evictCache(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10;
	}

}
