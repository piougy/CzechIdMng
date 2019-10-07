package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleRequestProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;

/**
 * Finisher of request for change permissions. Changing state of request to 'Executed'.
 * 
 * @author Vít Švanda
 *
 */
@Component
@Description("Finisher of request for change permissions. Changing state of request to 'Executed'.")
public class RoleRequestRealizationFinisherProcessor extends CoreEventProcessor<IdmRoleRequestDto>
		implements RoleRequestProcessor {

	public static final String PROCESSOR_NAME = "role-request-realization-finisher-processor";

	private final IdmRoleRequestService service;

	@Autowired
	public RoleRequestRealizationFinisherProcessor(IdmRoleRequestService service) {
		super(RoleRequestEventType.NOTIFY);
		Assert.notNull(service, "Service is required.");
		this.service = service;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto requestDto = event.getContent();
		RoleRequestState state = requestDto.getState();
		if(RoleRequestState.APPROVED  == state || RoleRequestState.IN_PROGRESS  == state) {
			requestDto.setState(RoleRequestState.EXECUTED);
			
			IdmRoleRequestDto returnedReqeust = service.refreshSystemState(requestDto);;
			event.setContent(service.save(returnedReqeust));
		}
		
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Finisher must be started on the end.
	 */
	@Override
	public int getOrder() {
		return Integer.MAX_VALUE - 1000;
	}

}
