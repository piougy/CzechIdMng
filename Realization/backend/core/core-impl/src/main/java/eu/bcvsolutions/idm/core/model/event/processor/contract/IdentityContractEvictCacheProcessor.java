package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Clear permission cache of currently logged identity, when contract is changed (CUD).
 * Permission is based on contract (~ tree structure) basically.
 * We need to clear cache after contract is changed (e.g. work position, validity ... => evict cache on every change).
 * 
 * Processor can be disabled, if permissions are not controlled by contracts.
 * 
 * @author Radek Tomiška
 * @since 10.7.0
 */
@Component(IdentityContractEvictCacheProcessor.PROCESSOR_NAME)
@Description("Clear permission cache of currently logged identity, when contract is changed (CUD).")
public class IdentityContractEvictCacheProcessor 
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {

	public static final String PROCESSOR_NAME = "core-identity-contract-evict-cache-processor";
	//
	@Autowired private SecurityService securityService;
	@Autowired private IdmCacheManager cacheManager;

	public IdentityContractEvictCacheProcessor() {
		super(IdentityContractEventType.CREATE, IdentityContractEventType.UPDATE, IdentityContractEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		UUID loggedIdentityId = securityService.getCurrentId(); // ~ identity is logged => system authentication is ignored.
		if (loggedIdentityId != null) {
			// evict cached permissions for currently logged identity
			cacheManager.evictValue(AuthorizationManager.PERMISSION_CACHE_NAME, loggedIdentityId);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10;
	}
}
