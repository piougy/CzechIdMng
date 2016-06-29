package eu.bcvsolutions.idm.core.security.dto;

import java.util.Collection;
import java.util.Date;

public class IdmJwtAuthenticationDto {

	private String currentUsername;
	private String originalUsername;
	private Date expiration;
	private Collection<DefaultGrantedAuthorityDto> authorities;

	public IdmJwtAuthenticationDto() {
	}

	public String getCurrentUsername() {
		return currentUsername;
	}

	public void setCurrentUsername(String currentUsername) {
		this.currentUsername = currentUsername;
	}

	public String getOriginalUsername() {
		return originalUsername;
	}

	public void setOriginalUsername(String originalUsername) {
		this.originalUsername = originalUsername;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public Collection<DefaultGrantedAuthorityDto> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<DefaultGrantedAuthorityDto> authorities) {
		this.authorities = authorities;
	}
	
}
