package eu.bcvsolutions.idm.core.security.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;

public class LoginDto {

	private String username;
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	private GuardedString password;
	private String token;
	private IdmJwtAuthenticationDto authentication;
	@JsonIgnore
	private boolean skipMustChange = false;
	// identifier which module authenticate user
	private String authenticationModule;

	public String getUsername() {
		return username;
	}

	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
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
