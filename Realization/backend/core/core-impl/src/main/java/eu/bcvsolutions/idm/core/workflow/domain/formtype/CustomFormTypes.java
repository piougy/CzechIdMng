package eu.bcvsolutions.idm.core.workflow.domain.formtype;

import java.util.LinkedHashMap;
import java.util.Map;

import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.impl.form.BooleanFormType;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.DoubleFormType;
import org.activiti.engine.impl.form.FormTypes;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;

public class CustomFormTypes extends FormTypes {

	public CustomFormTypes() {
		// register Activiti's default form types
		addFormType(new StringFormType());
		addFormType(new LongFormType());
		addFormType(new DateFormType("yyyy-MM-dd"));
		addFormType(new BooleanFormType());
		addFormType(new DoubleFormType());
		addFormType(new DecisionFormType());
	}

	@Override
	public AbstractFormType parseFormPropertyType(FormProperty formProperty) {
		Map<String, String> values = new LinkedHashMap<>();
		for (FormValue formValue : formProperty.getFormValues()) {
			values.put(formValue.getId(), formValue.getName());
		}
		switch (formProperty.getType()) {
			case TextAreaFormType.TYPE_NAME: {
				return new TextAreaFormType(values);
			}
			case TextFieldFormType.TYPE_NAME: {
				return new TextFieldFormType(values);
			}
			case CheckboxFormType.TYPE_NAME: {
				return new CheckboxFormType(values);
			}
			default: {
				// delegate construction of all other types
				return super.parseFormPropertyType(formProperty);
			}
		}
	}
}