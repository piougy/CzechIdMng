package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.AbstractRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleRequestEvent.AutomaticRoleRequestEventType;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;

/**
 * Processor realizes of the request for change the automatic role
 * 
 * @author svandav
 *
 */
@Component
@Description("Processor realizes of the request for change the automatic role")
public class AutomaticRoleRequestRealizationProcessor extends CoreEventProcessor<IdmAutomaticRoleRequestDto> {

	public static final String PROCESSOR_NAME = "automatic-role-request-realization-processor";

	private final IdmAutomaticRoleRequestService service;

	@Autowired
	public AutomaticRoleRequestRealizationProcessor(IdmAutomaticRoleRequestService service) {
		super(AutomaticRoleRequestEventType.EXECUTE);
		//
		Assert.notNull(service);
		//
		this.service = service;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmAutomaticRoleRequestDto> process(EntityEvent<IdmAutomaticRoleRequestDto> event) {
		AbstractRequestDto dto = event.getContent();
		event.setContent(service.executeRequest(dto.getId()));

		return new DefaultEventResult<>(event, this);
	}
}
