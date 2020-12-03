package eu.bcvsolutions.idm.core.security.api.dto;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModelProperty;

/**
 * Two factor authentication init registration response.
 * 
 * @author Radek Tomiška
 * @since 10.7.0
 */
public class TwoFactorRegistrationResponseDto {

	@NotEmpty
	@ApiModelProperty(required = true, notes = "Normalized identity username to two factor authentication registration (spinal-case).")
	private String username;
	@NotEmpty
	@ApiModelProperty(required = true, notes = "Two factor authentication registration secret.")
	private String verificationSecret;
	@ApiModelProperty(notes = "Two factor authentication registration qrcode (uri), when application is used.")
	private String qrcode;
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getVerificationSecret() {
		return verificationSecret;
	}
	
	public void setVerificationSecret(String verificationSecret) {
		this.verificationSecret = verificationSecret;
	}
	
	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}
	
	public String getQrcode() {
		return qrcode;
	}
}
