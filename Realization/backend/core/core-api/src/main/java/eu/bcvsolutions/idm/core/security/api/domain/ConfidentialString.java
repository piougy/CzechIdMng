package eu.bcvsolutions.idm.core.security.api.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * String is stored in confidential storage. ConfidentialString's owner is needed to load value from confidential storage.
 * 
 * @author Radek Tomiška
 */
@JsonSerialize(using = ConfidentialStringSerializer.class)
public class ConfidentialString implements Serializable {

	private static final long serialVersionUID = 8353899479448492699L;
	
	@JsonIgnore
	private String key;
	
	public ConfidentialString() {
	}
	
	public ConfidentialString(String key) {
		this.key = key;
	}
	
	@Override
	public String toString() {
		return GuardedString.SECRED_PROXY_STRING;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
}
