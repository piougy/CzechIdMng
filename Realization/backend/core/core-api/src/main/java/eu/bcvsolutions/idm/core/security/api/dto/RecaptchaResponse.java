package eu.bcvsolutions.idm.core.security.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from ReCaptcha validation. See https://developers.google.com/recaptcha/docs/verify.
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 */
public class RecaptchaResponse {

	@JsonProperty("success")
	private boolean success;	
	
	@JsonProperty("hostname")
	private String hostname;
	
	@JsonProperty("error-codes")
	private List<String> errorCodes;

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

	public List<String> getErrorCodes() {
		if (errorCodes == null) {
			errorCodes = new ArrayList<>();
		}
		return errorCodes;
	}

	public void setErrorCodes(List<String> errorCodes) {
		this.errorCodes = errorCodes;
	}
}
