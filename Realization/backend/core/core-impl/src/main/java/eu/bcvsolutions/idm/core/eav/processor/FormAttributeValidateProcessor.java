package eu.bcvsolutions.idm.core.eav.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.event.FormAttributeEvent.FormAttributeEventType;
import eu.bcvsolutions.idm.core.eav.api.event.processor.FormAttributeProcessor;
import eu.bcvsolutions.idm.core.eav.api.exception.ChangeConfidentialException;
import eu.bcvsolutions.idm.core.eav.api.exception.ChangePersistentTypeException;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;

/**
 * Change persistent type and confidential is possible, only if no form values for this attribute is persisted.
 * 
 * TODO: implement data migrations.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component
@Description("Change persistent type and confidential is possible, only if no form values for this attribute is persisted.")
public class FormAttributeValidateProcessor
		extends CoreEventProcessor<IdmFormAttributeDto> 
		implements FormAttributeProcessor {
	
	public static final String PROCESSOR_NAME = "core-form-attribute-validate-processor";
	//
	@Autowired private FormService formService;
	
	public FormAttributeValidateProcessor() {
		super(FormAttributeEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmFormAttributeDto> process(EntityEvent<IdmFormAttributeDto> event) {
		IdmFormAttributeDto dto = event.getContent();
		IdmFormAttributeDto original = event.getOriginalSource();
		// just for sure - wrong event type can be provided manually
		if (original == null) {
			return new DefaultEventResult<>(event, this);
		}
		//
		boolean persistentTypeChanged = original.getPersistentType() != dto.getPersistentType();
		boolean confidentialChanged = original.isConfidential() != dto.isConfidential();
		if (!persistentTypeChanged && !confidentialChanged) {
			return new DefaultEventResult<>(event, this);
		}
		//
		// Change persistent type is possible, only if no form values for this attribute is persisted.
		IdmFormValueFilter<FormableEntity> filter = new IdmFormValueFilter<>();
		filter.setAttributeId(dto.getId());
		try {
			if (formService.findValues(filter, PageRequest.of(0, 1)).getTotalElements() > 0) {
				throwException(dto.getCode(), persistentTypeChanged, confidentialChanged, null);
			}
		} catch (ResultCodeException ex) {
			// some form definition cannot have owner specified - drop and create attribute is supported only
			throwException(dto.getCode(), persistentTypeChanged, confidentialChanged, ex);
		}
		//
		return new DefaultEventResult<>(event, this);
	}

	
	private void throwException(String attributeCode, boolean persistentTypeChanged, boolean confidentialChanged, Exception ex) {
		if (persistentTypeChanged) {
			throw new ChangePersistentTypeException(attributeCode, ex);
		}
		if (confidentialChanged) {
			throw new ChangeConfidentialException(attributeCode, ex);
		}
	}
}
