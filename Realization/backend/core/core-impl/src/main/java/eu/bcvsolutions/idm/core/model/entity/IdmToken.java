package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Persistent token
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_token", indexes = {
	@Index(name = "idx_idm_token_o_id", columnList = "owner_id"),
	@Index(name = "idx_idm_token_o_type", columnList = "owner_type"),
	@Index(name = "idx_idm_token_exp", columnList = "expiration"),
	@Index(name = "idx_idm_token_token", columnList = "token"),
	@Index(name = "idx_idm_token_type", columnList = "token_type"),
	@Index(name = "idx_idm_token_external_id", columnList = "external_id")
})
public class IdmToken extends AbstractEntity implements Disableable, ExternalIdentifiable {

	private static final long serialVersionUID = 1L;

	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "owner_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String ownerType;
	
	@Column(name = "owner_id", length = 16)
	private UUID ownerId;
	
	@Size(max = DefaultFieldLengths.LOG)
	@Column(name = "token", length = DefaultFieldLengths.LOG)
	private String token;

	@Size(max = DefaultFieldLengths.ENUMARATION)
	@Column(name = "token_type", length = DefaultFieldLengths.ENUMARATION)
	private String tokenType;
	
	@Column(name = "module_id")
	private String moduleId;
	
	@Column(name = "properties", length = Integer.MAX_VALUE)
	private ConfigurationMap properties; // full token, cached authorites etc
	
	@NotNull
	@Column(name = "issued_at")
	private DateTime issuedAt;
	
	@Column(name = "expiration")
	private DateTime expiration;

	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled; // e.g. logout, authorities removed
	
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

	@Override
	public boolean isDisabled() {
		return disabled;
	}
	
	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public DateTime getExpiration() {
		return expiration;
	}

	public void setExpiration(DateTime expiration) {
		this.expiration = expiration;
	}
	
	public void setProperties(ConfigurationMap properties) {
		this.properties = properties;
	}
	
	public ConfigurationMap getProperties() {
		if (properties == null) {
			properties = new ConfigurationMap();
		}
		return properties;
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
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
}
