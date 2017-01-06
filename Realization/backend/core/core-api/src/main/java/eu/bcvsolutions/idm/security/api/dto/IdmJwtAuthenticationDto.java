package eu.bcvsolutions.idm.security.api.dto;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

/**
 * JWT token autentocation dto
 * 
 * @author svandav
 *
 */
public class IdmJwtAuthenticationDto {

	private String currentUsername;
	private UUID currentIdentityId;
	private String originalUsername;
	private UUID originaIdentityId;
	private Date expiration; // TODO: datetime
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

	public UUID getCurrentIdentityId() {
		return currentIdentityId;
	}

	public void setCurrentIdentityId(UUID currentIdentityId) {
		this.currentIdentityId = currentIdentityId;
	}

	public UUID getOriginaIdentityId() {
		return originaIdentityId;
	}

	public void setOriginaIdentityId(UUID originaIdentityId) {
		this.originaIdentityId = originaIdentityId;
	}
	
	public boolean isExpired() {
		if (expiration == null) {
			return false;
		}
		return expiration.before(new Date());
	}
}
