package eu.bcvsolutions.idm.core.model.event.processor.password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.event.PasswordEvent.PasswordEventType;

/**
 * Delete processor for {@link IdmPasswordDto}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Delete password for identity.")
public class PasswordDeleteProcessor extends CoreEventProcessor<IdmPasswordDto> {

	private static final String PROCESSOR_NAME = "password-delete-processor";
	@Autowired
	private IdmPasswordService service;

	public PasswordDeleteProcessor() {
		super(PasswordEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmPasswordDto> process(EntityEvent<IdmPasswordDto> event) {
		IdmPasswordDto passwordDto = event.getContent();
		//
		service.deleteInternal(passwordDto);
		//
		return new DefaultEventResult<>(event, this);
	}
}
