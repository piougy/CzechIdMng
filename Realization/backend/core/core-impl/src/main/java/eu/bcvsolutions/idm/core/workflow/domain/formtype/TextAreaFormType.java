package eu.bcvsolutions.idm.core.workflow.domain.formtype;

import java.util.Map;

/**
 * Form type for text area component
 * @author svandav
 *
 */
public class TextAreaFormType extends AbstractComponentFormType {

	private static final long serialVersionUID = 1L;
	public static final String TYPE_NAME = "textArea";

	public TextAreaFormType(Map<String, String> values) {
		super(values);
	}

	@Override
	public String getName() {
		return TYPE_NAME;
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
		if(!(modelValue instanceof String)){
			return modelValue.toString();
		}
		return (String) modelValue;
	}

}
