package eu.bcvsolutions.idm.core.eav.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;

/**
 * Processor to set original source for IdmFormInstanceDto
 *
 * @author Marek Klement
 * @since 10.2.0
 */
@Component(FormInstanceSaveOriginalSourceProcessor.PROCESSOR_NAME)
@Description("Save original source for IdmFormInstanceDto")
public class FormInstanceSaveOriginalSourceProcessor extends CoreEventProcessor<IdmFormInstanceDto> {

	public static final String PROCESSOR_NAME = "core-form-instance-save-original-source";

	@Autowired
	private FormService formService;
	@Autowired
	private LookupService lookupService;

	public FormInstanceSaveOriginalSourceProcessor() {
		super(CoreEventType.UPDATE);
	}

	@Override
	public EventResult<IdmFormInstanceDto> process(EntityEvent<IdmFormInstanceDto> event) {
		// IdmFormInstanceDto don't have saved previous value by default
		IdmFormInstanceDto content = event.getContent();
		// get owner to find his current value
		BaseDto baseDto = lookupService.lookupDto(content.getOwnerType(), content.getOwnerId());
		// get current value before change
		IdmFormInstanceDto formInstance = formService.getFormInstance(baseDto, content.getFormDefinition());
		//
		event.setOriginalSource(formInstance);
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return Integer.MIN_VALUE; // before change we need previous value
	}
}
