package eu.bcvsolutions.idm.core.eav.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.FormableDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.InvalidFormException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormProjectionManager;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;

/**
 * Validate formable entity's (owner's) basic fields.
 * Moved from {@link FormableSaveProcessor} to standalone processor => can be turned off.
 * 
 * @see FormableSaveProcessor
 * @author Radek Tomiška
 * @since 11.0.0
 */
@Component(FormableValidateBasicFieldsProcessor.PROCESSOR_NAME)
@Description("Validate formable entity's (owner's) basic fields.")
public class FormableValidateBasicFieldsProcessor extends CoreEventProcessor<FormableDto> {
	
	public static final String PROCESSOR_NAME = "core-formable-validate-basic-fields-processor";
	//
	@Autowired private FormService formService;
	@Autowired private FormProjectionManager formProjectionManager;
	
	public FormableValidateBasicFieldsProcessor() {
		super(CoreEventType.CREATE, CoreEventType.UPDATE); // ~ save dto
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<FormableDto> process(EntityEvent<FormableDto> event) {
		FormableDto savedDto = event.getContent(); // saved dto
		//
		IdmFormInstanceDto basicFields = formProjectionManager.getBasicFieldsInstance(savedDto);
		if (basicFields != null) {
			List<InvalidFormAttributeDto> errors = formService.validate(basicFields);
			if (!errors.isEmpty()) {
				throw new InvalidFormException(errors);
			}
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return -50; // before save
	}
}
