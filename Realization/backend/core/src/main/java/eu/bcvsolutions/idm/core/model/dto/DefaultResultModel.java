package eu.bcvsolutions.idm.core.model.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.bcvsolutions.idm.core.exception.ResultCode;

@JsonInclude(Include.NON_NULL)
public class DefaultResultModel implements ResultModel {
	
	private String id;
	private Date creation;	
	/**
	 * Idm error / message code
	 */
	private String statusEnum;
	/**
	 * internal message
	 */
	private String message;
	/**
	 * Parameters - for localization etc.
	 */
	private List<Object> parameters;
	
	private String module;
	
	public DefaultResultModel() {
		this.id = UUID.randomUUID().toString();
		this.creation = new Date();
	}
	
	public DefaultResultModel(ResultCode resultCode, Object[] parameters) {
		this(resultCode, null, parameters);
	}
	
	/**
	 * 
	 * @param resultCode
	 * @param message Overrides automatic resultCode message
	 * @param parameters
	 */
	public DefaultResultModel(ResultCode resultCode, String message, Object[] parameters) {
		this();
		this.statusEnum = resultCode.getCode();
		this.module = resultCode.getModule();
		String messageFormat = (StringUtils.isEmpty(message)) ? resultCode.getMessage() : message;
		try {
			this.message = String.format(messageFormat, parameters);
		} catch(IllegalFormatException ex) {
			this.message = messageFormat;
		}
		if(parameters != null) {
			this.parameters = new ArrayList<>(Arrays.asList(parameters));
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public void setParameters(List<Object> parameters) {
		this.parameters = parameters;
	}
	
	public List<Object> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<>();
		}
		return parameters;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getStatusEnum() {
		return statusEnum;
	}
	
	public void setStatusEnum(String statusEnum) {
		this.statusEnum = statusEnum;
	}
	
	public String getModule() {
		return module;
	}
	
	public void setModule(String module) {
		this.module = module;
	}
}