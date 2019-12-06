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
 * Processor for save role-request
 * 
 * @author Vít Švanda
 *
 */
@Component(RoleRequestSaveProcessor.PROCESSOR_NAME)
@Description("Processor for save role-request")
public class RoleRequestSaveProcessor extends CoreEventProcessor<IdmRoleRequestDto> implements RoleRequestProcessor {
	public static final String PROCESSOR_NAME = "role-request-save-processor";

	private final IdmRoleRequestService service;

	@Autowired
	public RoleRequestSaveProcessor(IdmRoleRequestService service) {
		super(RoleRequestEventType.CREATE, RoleRequestEventType.UPDATE);
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
		dto = service.saveInternal(dto);
		event.setContent(dto);

		return new DefaultEventResult<>(event, this);
	}
}
