package eu.bcvsolutions.idm.core.eav.processor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.InvalidFormException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.event.processor.FormInstanceProcessor;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;

/**
 * validate form instance (eav attributes) before save.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component(FormInstanceValidateProcessor.PROCESSOR_NAME)
@Description("Persists entity event.")
public class FormInstanceValidateProcessor 
		extends CoreEventProcessor<IdmFormInstanceDto>
		implements FormInstanceProcessor {
	
	public static final String PROCESSOR_NAME = "core-form-instance-validate-processor";
	//
	@Autowired private FormService formService;
	
	public FormInstanceValidateProcessor() {
		super(CoreEventType.UPDATE); // eavs are updated for CUD
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmFormInstanceDto> process(EntityEvent<IdmFormInstanceDto> event) {
		IdmFormInstanceDto formInstance = event.getContent();
		Assert.notNull(formInstance.getFormDefinition());
		//
		IdmFormDefinitionDto formDefinition = formService.getDefinition(formInstance.getFormDefinition().getId());
		Assert.notNull(formDefinition);
		//
		// get distinct attributes from the sent values
		// PATCH is used - only sent attributes are validated
		Set<IdmFormAttributeDto> sentAttributes = formInstance
				.getValues()
				.stream()
				.map(IdmFormValueDto::getFormAttribute)
				.map(attributeId -> {
					return formDefinition.getMappedAttribute(attributeId);
				})
				.collect(Collectors.toSet());
		// only sent attributes in definition and instance
		formDefinition.setFormAttributes(Lists.newArrayList(sentAttributes));
		IdmFormInstanceDto validateFormInstance = new IdmFormInstanceDto();
		validateFormInstance.setOwnerId(formInstance.getOwnerId());
		validateFormInstance.setOwnerType(formInstance.getOwnerType());
		validateFormInstance.setFormDefinition(formDefinition);
		validateFormInstance.setValues(formInstance.getValues());
		// validate
		List<InvalidFormAttributeDto> errors = formService.validate(validateFormInstance);
		if (!errors.isEmpty()) {
			throw new InvalidFormException(errors);
		}
		//
		return new DefaultEventResult<>(event, this);
	}

	/**
	 * Before validation
	 */
	@Override
	public int getOrder() {
		return -50;
	}
}
