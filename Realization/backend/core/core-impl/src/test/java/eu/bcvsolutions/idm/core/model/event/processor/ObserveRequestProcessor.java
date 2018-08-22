package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;

/**
 * Listen execute event on registered role requests
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class ObserveRequestProcessor extends CoreEventProcessor<IdmRoleRequestDto> {

	// observed requests
	private static Map<UUID, CountDownLatch> listenContents = new ConcurrentHashMap<>();
	
	public ObserveRequestProcessor() {
		super(RoleRequestEventType.NOTIFY);
	}
		
	@Override
	public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
		listenContents.get(event.getContent().getId()).countDown();
		//
		return null;
	}
	
	@Override
	public boolean supports(EntityEvent<?> event) {
		if (!super.supports(event)) {
			return false;
		}
		return listenContents.containsKey(((BaseDto) event.getContent()).getId());
	}
	
	public static void listenContent(UUID contentId) {
		listenContents.put(contentId, new CountDownLatch(1));
	}
	
	/**
	 * Wait for task ends
	 * 
	 * @throws InterruptedException
	 */
	public static void waitForEnd(UUID contentId) throws InterruptedException {
		listenContents.get(contentId).await();
	}

	@Override
	public int getOrder() {
		return Integer.MAX_VALUE;
	}
}
