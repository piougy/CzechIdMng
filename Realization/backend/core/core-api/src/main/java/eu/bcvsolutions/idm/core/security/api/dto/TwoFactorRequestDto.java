package eu.bcvsolutions.idm.core.security.api.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import io.swagger.annotations.ApiModelProperty;

/**
 * Two factor login request.
 * 
 * @author Radek Tomiška
 * @since 10.7.0
 */
public class TwoFactorRequestDto {

	@NotNull
	@ApiModelProperty(required = true, notes = "Logged identity's authentication token.")
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	private GuardedString token;
	@NotNull
	@ApiModelProperty(required = true, notes = "Two factor authentication verify 6-digit code.", example = "123456")
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	private GuardedString verificationCode;
	
	public GuardedString getToken() {
		return token;
	}

	public void setToken(GuardedString token) {
		this.token = token;
	}
	
	public void setVerificationCode(GuardedString verificationCode) {
		this.verificationCode = verificationCode;
	}
	
	public GuardedString getVerificationCode() {
		return verificationCode;
	}
}
