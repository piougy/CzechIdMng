package eu.bcvsolutions.idm.core.workflow.domain.formtype;

import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Roman Kučera
 */
public class TaskHistoryFormType extends AbstractComponentFormType {

	private static final long serialVersionUID = 1L;
	public static final String TYPE_NAME = "taskHistory";

	public TaskHistoryFormType(Map<String, String> values) {
		super(values);
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {
		return propertyValue;
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {
		if (modelValue == null) {
			return null;
		}
		if (!(modelValue instanceof String)) {
			try {
				return new ObjectMapper().writeValueAsString(modelValue);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return modelValue.toString();
			}
		}
		return (String) modelValue;
	}

	@Override
	public String getName() {
		return TYPE_NAME;
	}
}
