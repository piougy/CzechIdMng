package eu.bcvsolutions.idm.core.security.api.dto;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JWT token authentication dto
 * 
 * @author svandav
 */
public class IdmJwtAuthenticationDto {

	private UUID id;
	private String currentUsername;
	private UUID currentIdentityId;
	private String originalUsername;
	private UUID originalIdentityId;
	@JsonProperty("exp") // by rfc7519
	private DateTime expiration;
	@JsonProperty("iat") // by rfc7519
	private DateTime issuedAt;
	private String fromModule;

	public IdmJwtAuthenticationDto() {
	}
	
	public void setId(UUID id) {
		this.id = id;
	}
	
	public UUID getId() {
		return id;
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

	public DateTime getExpiration() {
		return expiration;
	}

	public void setExpiration(DateTime expiration) {
		this.expiration = expiration;
	}

	public DateTime getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(DateTime issuedAt) {
		this.issuedAt = issuedAt;
	}

	public UUID getCurrentIdentityId() {
		return currentIdentityId;
	}

	public void setCurrentIdentityId(UUID currentIdentityId) {
		this.currentIdentityId = currentIdentityId;
	}

	public UUID getOriginalIdentityId() {
		return originalIdentityId;
	}

	public void setOriginalIdentityId(UUID originaIdentityId) {
		this.originalIdentityId = originaIdentityId;
	}
	
	public boolean isExpired() {
		if (expiration == null) {
			return false;
		}
		return expiration.isBefore(DateTime.now());
	}
	
	public String getFromModule() {
		return fromModule;
	}

	public void setFromModule(String fromModule) {
		this.fromModule = fromModule;
	}
}
