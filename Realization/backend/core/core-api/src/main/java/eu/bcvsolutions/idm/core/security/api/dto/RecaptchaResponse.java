package eu.bcvsolutions.idm.core.security.api.dto;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from ReCaptcha validation. See https://developers.google.com/recaptcha/docs/verify.
 * 
 * @author Filip Mestanek
 */
public class RecaptchaResponse {

	@JsonProperty("success")
	private boolean success;
	
	@JsonProperty("hostname")
	private String hostname;
	
	@JsonProperty("error-codes")
	private Collection<String> errorCodes;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Collection<String> getErrorCodes() {
		return errorCodes;
	}

	public void setErrorCodes(Collection<String> errorCodes) {
		this.errorCodes = errorCodes;
	}
}
