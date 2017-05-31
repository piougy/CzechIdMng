package eu.bcvsolutions.idm.core.security.api.dto;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Request for ReCaptcha validation, see https://developers.google.com/recaptcha/docs/verify.
 * 
 * @author Filip Mestanek
 */
public class RecaptchaRequest {
	
	@NotEmpty
	private String response;
	
	private String remoteIp;

	
	public RecaptchaRequest() {}
	

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
}
