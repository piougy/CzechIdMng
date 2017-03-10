package eu.bcvsolutions.idm.core.security.api.dto;

import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.authentication.Authenticator;
import eu.bcvsolutions.idm.core.security.api.domain.AuthenticationResultEnum;

/**
 * Result from authenticator see {@link AuthenticationManager} and {@link Authenticator}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class AuthenticatorResultDto {
	
	private AuthenticationResultEnum resultEnum;

	private LoginDto loginDto;
	
	private RuntimeException exception;

	public AuthenticationResultEnum getResultEnum() {
		return resultEnum;
	}

	public LoginDto getLoginDto() {
		return loginDto;
	}

	public RuntimeException getException() {
		return exception;
	}

	public void setResultEnum(AuthenticationResultEnum resultEnum) {
		this.resultEnum = resultEnum;
	}

	public void setLoginDto(LoginDto loginDto) {
		this.loginDto = loginDto;
	}

	public void setException(RuntimeException exception) {
		this.exception = exception;
	}
	
	public void setResultNotDone() {
		this.resultEnum = AuthenticationResultEnum.NOT_DONE;
	}
	
	public void setResultSuccess() {
		this.resultEnum = AuthenticationResultEnum.SUCCESS;
	}
	
	public void setResultFailture() {
		this.resultEnum = AuthenticationResultEnum.FAILTURE;
	}
}
