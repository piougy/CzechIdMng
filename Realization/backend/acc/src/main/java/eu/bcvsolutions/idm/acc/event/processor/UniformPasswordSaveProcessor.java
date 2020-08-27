package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.event.UniformPasswordEvent.UniformPasswordEventType;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Save {@link AccUniformPasswordDto}
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component("accUniformPasswordSaveProcessor")
@Description("Save newly created or update exists uniform password definition.")
public class UniformPasswordSaveProcessor extends CoreEventProcessor<AccUniformPasswordDto> {

	private static final String PROCESSOR_NAME = "uniform-password-save-processor";

	@Autowired
	private AccUniformPasswordService unifromasswordService;

	public UniformPasswordSaveProcessor() {
		super(UniformPasswordEventType.CREATE, UniformPasswordEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccUniformPasswordDto> process(EntityEvent<AccUniformPasswordDto> event) {
		AccUniformPasswordDto uniformPasswordDto = event.getContent();

		uniformPasswordDto = unifromasswordService.saveInternal(uniformPasswordDto);
		event.setContent(uniformPasswordDto);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	@Override
	public boolean isDisableable() {
		return false;
	}

}
