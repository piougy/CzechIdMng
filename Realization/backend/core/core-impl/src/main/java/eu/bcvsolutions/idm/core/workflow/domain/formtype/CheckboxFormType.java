
package eu.bcvsolutions.idm.core.workflow.domain.formtype;

import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;

/**
 * Form type for checkbox component
 * 
 * @author svandav
 *
 */
public class CheckboxFormType extends AbstractComponentFormType {

	private static final long serialVersionUID = 1L;
	public static final String TYPE_NAME = "checkbox";

	public CheckboxFormType(Map<String, String> values) {
		super(values);
	}

	@Override
	public String getName() {
		return TYPE_NAME;
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {
		if (propertyValue == null || "".equals(propertyValue)) {
			return null;
		}
		return Boolean.valueOf(propertyValue);
	}

	public String convertModelValueToFormValue(Object modelValue) {

		if (modelValue == null) {
			return null;
		}

		if (Boolean.class.isAssignableFrom(modelValue.getClass())
				|| boolean.class.isAssignableFrom(modelValue.getClass())) {
			return modelValue.toString();
		}
		throw new ActivitiIllegalArgumentException("Model value is not boolean -" + modelValue.getClass().getName());
	}
}
