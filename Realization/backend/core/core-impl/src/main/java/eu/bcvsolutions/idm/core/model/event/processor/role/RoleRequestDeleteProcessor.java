package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleRequestProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;

/**
 * Processor for delete role-request
 * 
 * @author Vít Švanda
 *
 */
@Component(RoleRequestDeleteProcessor.PROCESSOR_NAME)
@Description("Processor for delete role-request")
public class RoleRequestDeleteProcessor extends CoreEventProcessor<IdmRoleRequestDto> implements RoleRequestProcessor {
	public static final String PROCESSOR_NAME = "role-request-delete-processor";

	private final IdmRoleRequestService service;

	@Autowired
	public RoleRequestDeleteProcessor(IdmRoleRequestService service) {
		super(RoleRequestEventType.DELETE);
		//
		Assert.notNull(service, "Service is required.");
		//
		this.service = service;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto dto = event.getContent();
		service.deleteInternal(dto);
		
		return new DefaultEventResult<>(event, this);
	}
}
