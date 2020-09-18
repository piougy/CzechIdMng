package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleCompositionProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.model.event.RoleCompositionEvent.RoleCompositionEventType;

/**
 * Clear sub role cache, when  role composition is changed (CUD).
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
@Component(RoleCompositionEvictCacheProcessor.PROCESSOR_NAME)
@Description("Clear sub role cache, when  role composition is changed (CUD).")
public class RoleCompositionEvictCacheProcessor 
		extends CoreEventProcessor<IdmRoleCompositionDto> 
		implements RoleCompositionProcessor {

	public static final String PROCESSOR_NAME = "core-role-composition-evict-cache-processor";
	//
	@Autowired private IdmCacheManager cacheManager;

	public RoleCompositionEvictCacheProcessor() {
		super(RoleCompositionEventType.CREATE, RoleCompositionEventType.UPDATE, RoleCompositionEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleCompositionDto> process(EntityEvent<IdmRoleCompositionDto> event) {
		// evict all cached sub roles
		cacheManager.evictCache(IdmRoleCompositionService.ALL_SUB_ROLES_CACHE_NAME);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10;
	}
}
