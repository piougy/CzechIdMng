package eu.bcvsolutions.idm.core.workflow.domain.formtype;

import java.io.IOException;

import org.activiti.engine.form.AbstractFormType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.workflow.model.dto.DecisionFormTypeDto;

public class DecisionFormType extends AbstractFormType {

	private static final long serialVersionUID = 1L;
	public static final String TYPE_NAME = "decision";

	private ObjectMapper objectMapper = new ObjectMapper();

	public String getName() {
		return TYPE_NAME;
	}

	public Object convertFormValueToModelValue(String propertyValue) {
		try {
			return objectMapper.readValue(propertyValue, DecisionFormTypeDto.class);
		} catch (IOException e) {
			throw new CoreException(e);
		}

	}

	public String convertModelValueToFormValue(Object modelValue) {
		try {
			return objectMapper.writeValueAsString(modelValue);
		} catch (JsonProcessingException e) {
			throw new CoreException(e);
		}
	}

}