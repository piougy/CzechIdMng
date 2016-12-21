package eu.bcvsolutions.idm.core.workflow.domain.formtype;

import java.text.Format;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Form type for joda LocalDate
 * 
 * @author Radek Tomiška
 *
 */
public class LocalDateFormType extends AbstractComponentFormType {

	private static final long serialVersionUID = 1L;
	public static final String TYPE_NAME = "localDate";
	private static final Format dateFormat = FastDateFormat.getInstance(CustomFormTypes.DATE_FORMAT_PATTERN);
	private static final DateTimeFormatter localDateFormat = DateTimeFormat.forPattern(CustomFormTypes.DATE_FORMAT_PATTERN);
	
	public LocalDateFormType(Map<String, String> values) {
		super(values);
	}

	@Override
	public String getName() {
		return TYPE_NAME;
	}

	public Object getInformation(String key) {
		if ("datePattern".equals(key)) {
			return CustomFormTypes.DATE_FORMAT_PATTERN;
		}
		return super.getInformation(key);
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {
		if (StringUtils.isEmpty(propertyValue)) {
			return null;
		}
		try {
			return localDateFormat.parseLocalDate(propertyValue);
		} catch (IllegalArgumentException ex) {
			throw new ActivitiIllegalArgumentException("invalid local date value " + propertyValue, ex);
		}
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {
		if (modelValue == null) {
			return null;
		}
		if (modelValue instanceof LocalDate) {
			return localDateFormat.print((LocalDate) modelValue);
		}
		// other dates (java.util, sql date etc.)
		return dateFormat.format(modelValue);
	}

}
