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
 * Processor for persist password for identity.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Persists password for identity.")
public class PasswordSaveProcessor extends CoreEventProcessor<IdmPasswordDto> {

	private static final String PROCESSOR_NAME = "password-save-processor";
	@Autowired
	private IdmPasswordService service;

	public PasswordSaveProcessor() {
		super(PasswordEventType.CREATE, PasswordEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmPasswordDto> process(EntityEvent<IdmPasswordDto> event) {
		IdmPasswordDto passwordDto = event.getContent();
		//
		passwordDto = service.saveInternal(passwordDto);
		//
		event.setContent(passwordDto);
		//
		return new DefaultEventResult<>(event, this);
	}
}
