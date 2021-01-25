package eu.bcvsolutions.idm.core.api.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Persisted tokens.
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
@Relation(collectionRelation = "tokens")
public class IdmTokenDto extends AbstractDto implements Disableable, ExternalIdentifiable {

	private static final long serialVersionUID = 1L;
	//
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String ownerType;
	private UUID ownerId;
	@NotNull
	private String token;
	@NotNull
	private String tokenType; // CIDMST, LLT, password reset, confirm...
	@NotNull
	private ZonedDateTime issuedAt;
	private ZonedDateTime expiration;
	private ConfigurationMap properties;
	private boolean disabled;
	private String moduleId;
	private boolean secretVerified = true;
	
	public IdmTokenDto() {
	}
	
	public IdmTokenDto(UUID id) {
		super(id);
	}
	
	public IdmTokenDto(Auditable auditable) {
		super(auditable);
	}
	
	public String getOwnerType() {
		return ownerType;
	}
	
	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	
	public UUID getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public ZonedDateTime getExpiration() {
		return expiration;
	}

	public void setExpiration(ZonedDateTime expiration) {
		this.expiration = expiration;
	}

	public ConfigurationMap getProperties() {
		if (properties == null) {
			properties = new ConfigurationMap();
		}
		return properties;
	}

	public void setProperties(ConfigurationMap properties) {
		this.properties = properties;
	}
	
	public ZonedDateTime getIssuedAt() {
		return issuedAt;
	}
	
	public void setIssuedAt(ZonedDateTime issuedAt) {
		this.issuedAt = issuedAt;
	}
	
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	public String getModuleId() {
		return moduleId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	/**
	 * Token is verified (by two factor) authentication, if needed.
	 * 
	 * @return true => token is verified or verification is not required
	 * @since 10.7.0
	 */
	public boolean isSecretVerified() {
		return secretVerified;
	}
	
	/**
	 * Token is verified (by two factor) authentication, if needed.
	 * 
	 * @param verified  true => token is verified or verification is not required. False => verification is required
	 * @since 10.7.0
	 */
	public void setSecretVerified(boolean secretVerified) {
		this.secretVerified = secretVerified;
	}
}
