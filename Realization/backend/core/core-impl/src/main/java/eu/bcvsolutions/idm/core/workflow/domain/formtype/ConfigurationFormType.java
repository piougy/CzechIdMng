package eu.bcvsolutions.idm.core.workflow.domain.formtype;

import java.util.Map;

/**
 * Form type for configuration task. 
 * Configuration is from String
 * 
 * @author svandav
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class ConfigurationFormType extends AbstractComponentFormType {

	private static final long serialVersionUID = 1L;
	public static final String TYPE_NAME = "configuration";
	
	public ConfigurationFormType(Map<String, String> values) {
		super(values);
	}

	public ConfigurationFormType() {
		super(null);
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
