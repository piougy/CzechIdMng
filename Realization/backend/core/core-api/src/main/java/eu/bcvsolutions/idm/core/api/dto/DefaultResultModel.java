package eu.bcvsolutions.idm.core.api.dto;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;

/**
 * Detault response model
 * 
 * @author Radek Tomiška
 *
 */
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
	private final Map<String, Object> parameters = new LinkedHashMap<>();
	
	private String module;
	
	/**
	 * Http status code
	 */
	private int statusCode;
	/**
	 * Http status name
	 */
	private HttpStatus status;
	
	public DefaultResultModel() {
		this.id = UUID.randomUUID().toString();
		this.creation = new Date();
	}
	
	public DefaultResultModel(ResultCode resultCode, Map<String, Object> parameters) {
		this(resultCode, null, parameters);
	}
	
	public DefaultResultModel(ResultCode resultCode) {
		this(resultCode, null, null);
	}
	
	public DefaultResultModel(ResultCode resultCode, String message) {
		this(resultCode, message, null);
	}
	
	/**
	 * 
	 * @param resultCode
	 * @param message Overrides automatic resultCode message
	 * @param parameters Linked hash map is needed - use ImmutableMap.of(..) or construct LinkedHashMap for preserve params order.
	 */
	public DefaultResultModel(ResultCode resultCode, String message, Map<String, Object> parameters) {
		this();
		this.statusEnum = resultCode.getCode();
		this.module = resultCode.getModule();
		this.status = resultCode.getStatus();
		this.statusCode = resultCode.getStatus().value();
		String messageFormat = (StringUtils.isEmpty(message)) ? resultCode.getMessage() : message;
		try {
			// Linked hash map is needed - use ImmutableMap.of(..) or construct LinkedHashMap for preserve params order.
			this.message = String.format(messageFormat, parameters != null ? parameters.values().toArray() : null);
		} catch(IllegalFormatException ex) {
			this.message = messageFormat;
		}
		if(parameters != null) {			
			this.parameters.putAll(parameters);
		}
	}

	public String getMessage() {
		return message;
	}

	public Date getCreation() {
		return creation;
	}
	
	public Map<String, Object> getParameters() {
		return Collections.unmodifiableMap(this.parameters);
	}

	public String getId() {
		return id;
	}

	public String getStatusEnum() {
		return statusEnum;
	}
	
	public String getModule() {
		return module;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("[{1}:{2}:{0}] {3} ({4})", id, module, statusEnum, message, parameters);
	}
}