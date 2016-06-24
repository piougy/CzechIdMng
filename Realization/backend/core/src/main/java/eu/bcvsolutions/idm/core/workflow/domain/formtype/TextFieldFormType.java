package eu.bcvsolutions.idm.core.workflow.domain.formtype;

import java.util.Map;


/**
 * Form type for text field component
 * @author svandav
 *
 */
public class TextFieldFormType extends AbstractComponentFormType {
	
	private static final long serialVersionUID = 1L;
	public static final String TYPE_NAME = "textField";

	public TextFieldFormType(Map<String, String> values) {
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
		return (String) modelValue;
	}

}
