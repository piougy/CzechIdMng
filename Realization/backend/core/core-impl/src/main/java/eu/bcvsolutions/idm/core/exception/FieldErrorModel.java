package eu.bcvsolutions.idm.core.exception;

import java.text.MessageFormat;

import javax.validation.ConstraintViolation;

import org.springframework.validation.FieldError;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;

/**
 * Form field validation error
 * 
 * @author Radek Tomiška 
 *
 */
public class FieldErrorModel extends DefaultErrorModel {

	private static final long serialVersionUID = 1927685199638984683L;
	@JsonIgnore
	private final String objectName;
	@JsonIgnore
	private final String field;
	@JsonIgnore
	private final String code;
	
	public FieldErrorModel(String objectName, String field, String code, String message) {
		super(CoreResultCode.BAD_VALUE, message, ImmutableMap.of("objectName", objectName, "field", field, "code", code));
		this.objectName = objectName;
		this.field = field;
		this.code = code;
	}
	
	/**
	 * Creates {@link FieldErrorModel} from {@link FieldError}.
	 * 
	 * @param fieldError required
	 */
	public FieldErrorModel(FieldError fieldError) {
		this(fieldError.getObjectName(), fieldError.getField(), fieldError.getCode(), fieldError.getDefaultMessage());
	}
	
	/**
	 * Creates {@link FieldErrorModel} from {@link ConstraintViolation}.
	 * 
	 * @param violation required
	 */
	public FieldErrorModel(ConstraintViolation<?> violation) {
		this(
				violation.getRootBeanClass() == null ? "entity" : violation.getRootBeanClass().getSimpleName(), 
				violation.getPropertyPath() == null ? "field" : violation.getPropertyPath().toString(), 
						violation.getConstraintDescriptor() == null ? "invalid" : violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(), 
				violation.getMessage());
	}
	
	@Override
	public String getStatusEnum() {
		return MessageFormat.format("{0}_{1}_{2}", objectName, field, code).toUpperCase();
	}

	public String getObjectName() {
		return objectName;
	}

	public String getField() {
		return field;
	}

	public String getCode() {
		return code;
	}
	
	
}
