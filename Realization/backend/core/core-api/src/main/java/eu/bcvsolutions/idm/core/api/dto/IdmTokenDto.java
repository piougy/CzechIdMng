package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Persisted tokens
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
	private String tokenType; // cidmst, password reset, confirm...
	@NotNull
	private DateTime issuedAt;
	private DateTime expiration;
	private ConfigurationMap properties;
	private boolean disabled;
	private String moduleId;
	
	public IdmTokenDto() {
	}
	
	public IdmTokenDto(UUID id) {
		super(id);
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

	public DateTime getExpiration() {
		return expiration;
	}

	public void setExpiration(DateTime expiration) {
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
	
	public DateTime getIssuedAt() {
		return issuedAt;
	}
	
	public void setIssuedAt(DateTime issuedAt) {
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
}
