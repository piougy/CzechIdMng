package eu.bcvsolutions.idm.core.exception;

import java.text.MessageFormat;

import org.springframework.validation.FieldError;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.model.dto.DefaultResultModel;

/**
 * Form field validation error
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public class FieldErrorModel extends DefaultErrorModel {

	@JsonIgnore
	private final String objectName;
	@JsonIgnore
	private final String field;
	@JsonIgnore
	private final String code;
	
	public FieldErrorModel(String objectName, String field, String code, String message) {
		super(CoreResultCode.BAD_VALUE, message);
		this.objectName = objectName;
		this.field = field;
		this.code = code;
		setParameters(Lists.newArrayList(objectName, field, code));
	}
	
	public FieldErrorModel(FieldError fieldError) {
		this(fieldError.getObjectName(), fieldError.getField(), fieldError.getCode(), fieldError.getDefaultMessage());
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
