package eu.bcvsolutions.idm.core.model.event.processor.password;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.PasswordEvent.PasswordEventType;

/**
 * Check password never expires and remove valid till
 *
 * @author Ondrej Kopr
 *
 */
@Component
@Description("Check password never expires.")
public class PasswordNeverExpiresProcessor extends CoreEventProcessor<IdmPasswordDto> {

	private static final String PROCESSOR_NAME = "password-never-expires-processor";

	public PasswordNeverExpiresProcessor() {
		super(PasswordEventType.CREATE, PasswordEventType.UPDATE);
	}

	@Override
	public EventResult<IdmPasswordDto> process(EntityEvent<IdmPasswordDto> event) {
		IdmPasswordDto passwordDto = event.getContent();
		//
		// If this password never expires, set valid till to null. Even if someone set valid till value.
		if (passwordDto.isPasswordNeverExpires()) {
			passwordDto.setValidTill(null);
		}
		//
		event.setContent(passwordDto);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return -10;
	}
}
