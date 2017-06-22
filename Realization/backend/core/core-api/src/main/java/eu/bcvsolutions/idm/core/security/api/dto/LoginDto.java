package eu.bcvsolutions.idm.core.security.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import io.swagger.annotations.ApiModelProperty;

/**
 * Identity authentication
 * 
 * @author Svanda
 * @author Radek Tomiška
 *
 */
public class LoginDto extends LoginRequestDto {
	
	@JsonProperty(access = Access.READ_ONLY)
	@ApiModelProperty(readOnly = true, notes = "Logged identity's authentication token.")
	private String token;
	@JsonProperty(access = Access.READ_ONLY)
	@ApiModelProperty(readOnly = true, notes = "Logged identity's authentication with granted authorities.")
	private IdmJwtAuthenticationDto authentication;
	@JsonIgnore
	private boolean skipMustChange = false;
	@JsonProperty(access = Access.READ_ONLY)
	@ApiModelProperty(readOnly = true, notes = "Which module authenticated identity.")
	private String authenticationModule; // identifier - which module authenticated identity
	
	public LoginDto() {
	}
	
	public LoginDto(String username, GuardedString password) {
		super(username, password);
	}
	
	public LoginDto(LoginRequestDto loginRequest) {
		this(loginRequest.getUsername(), loginRequest.getPassword());
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setAuthentication(IdmJwtAuthenticationDto authenticationDto) {
		this.authentication = authenticationDto;
	}

	public IdmJwtAuthenticationDto getAuthentication() {
		return this.authentication;
	}

	public boolean isSkipMustChange() {
		return skipMustChange;
	}

	public void setSkipMustChange(boolean skipMustChange) {
		this.skipMustChange = skipMustChange;
	}

	public String getAuthenticationModule() {
		return authenticationModule;
	}

	public void setAuthenticationModule(String authenticationModule) {
		this.authenticationModule = authenticationModule;
	}

}
