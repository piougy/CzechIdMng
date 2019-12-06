package eu.bcvsolutions.idm.core.api.dto;

import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Detault response model
 * 
 * @author Radek Tomiška
 */
@JsonInclude(Include.NON_NULL)
public class DefaultResultModel implements ResultModel {
	
	private static final long serialVersionUID = 7396789300001882593L;
	private UUID id; // TODO: orchestrate with transaction id
	private ZonedDateTime creation;	
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
	private Map<String, Object> parameters = new LinkedHashMap<>();
	
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
		this.id = UUID.randomUUID();
		this.creation = ZonedDateTime.now();
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

	@Override
	public ZonedDateTime getCreation() {
		return creation;
	}
	
	public Map<String, Object> getParameters() {
		return Collections.unmodifiableMap(this.parameters);
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public String getStatusEnum() {
		return statusEnum;
	}
	
	@Override
	public String getModule() {
		return module;
	}
	
	@Override
	public HttpStatus getStatus() {
		return status;
	}
	
	@Override
	public int getStatusCode() {
		return statusCode;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("[{1}:{2}:{0}] {3} ({4})", id, module, statusEnum, message, parameters);
	}
	
	/**
	 * Model is serialized in LRT, WF etc.
	 * We need to solve legacy issues with joda (old) vs. java time (new) usage.
	 * 
	 * @param ois
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws Exception {
		GetField readFields = ois.readFields();
		//
		id = (UUID) readFields.get("id", null);
		creation = DtoUtils.toZonedDateTime(readFields.get("creation", null));
		statusEnum = (String) readFields.get("statusEnum", null);
		message = (String) readFields.get("message", null);
		parameters = (Map<String, Object>) readFields.get("parameters", null);
		module = (String) readFields.get("module", null);
		statusCode = readFields.get("statusCode", 0);
		status = (HttpStatus) readFields.get("status", null);
    } 
}