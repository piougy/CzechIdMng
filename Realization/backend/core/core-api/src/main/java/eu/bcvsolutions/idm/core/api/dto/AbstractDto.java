package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.Size;

import org.joda.time.DateTime;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import io.swagger.annotations.ApiModelProperty;

/**
 * Common dto
 * 
 * @author Radek Tomiška 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractDto implements BaseDto, Auditable {
	
	private static final long serialVersionUID = 7512463222974374742L;
	//
	@JsonDeserialize(as = UUID.class)
	@ApiModelProperty(required = true, notes = "Unique uuid identifier. Used as identifier in rest endpoints", dataType = "java.util.UUID")
	private UUID id;
	@ApiModelProperty(readOnly = true)
	private DateTime created;
	@ApiModelProperty(readOnly = true)
	private DateTime modified;
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(readOnly = true)
	private String creator;
	@ApiModelProperty(readOnly = true)
	private UUID creatorId;
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(readOnly = true)
	private String modifier;
	@ApiModelProperty(readOnly = true)
	private UUID modifierId;
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(readOnly = true)
	private String originalCreator;
	@ApiModelProperty(readOnly = true)
	private UUID originalCreatorId;
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(readOnly = true)
	private String originalModifier;
	@ApiModelProperty(readOnly = true)
	private UUID originalModifierId;
	@JsonProperty(value = "_trimmed", access = Access.READ_ONLY)
	@ApiModelProperty(readOnly = true)
	private boolean trimmed = false;
	@JsonProperty(value = "_embedded", access = Access.READ_ONLY)
	@ApiModelProperty(readOnly = true)
	private Map<String, BaseDto> embedded;
	@JsonIgnore
	@ApiModelProperty(readOnly = true)
	private UUID transactionId;
	@JsonIgnore
	@ApiModelProperty(readOnly = true)
	private UUID realmId;

	public AbstractDto() {
	}

	public AbstractDto(UUID id) {
		this.id = id;
	}
	
	public AbstractDto(Auditable auditable) {
		Assert.notNull(auditable, "Auditable (dto or entity) is required");
		//
		this.id = auditable.getId();
		this.created = auditable.getCreated();
		this.modified = auditable.getModified();
		this.creator = auditable.getCreator();
		this.creatorId = auditable.getCreatorId();
		this.modifier = auditable.getModifier();
		this.modifierId = auditable.getModifierId();
		this.originalCreator = auditable.getOriginalCreator();
		this.originalCreatorId = auditable.getOriginalCreatorId();
		this.originalModifier = auditable.getOriginalModifier();
		this.originalModifierId = auditable.getOriginalModifierId();
		this.transactionId = auditable.getTransactionId();
		this.realmId = auditable.getRealmId();
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		try {
			this.id = EntityUtils.toUuid(id);
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException("AbstractDto supports only UUID identifier. For different identifier generalize BaseEntity.", ex);
		}
	}

	@Override
	public DateTime getCreated() {
		return created;
	}

	@Override
	public void setCreated(DateTime created) {
		this.created = created;
	}
	
	@Override
	public DateTime getModified() {
		return modified;
	}

	@Override
	public void setModified(DateTime modified) {
		this.modified = modified;
	}

	@Override
	public String getCreator() {
		return creator;
	}

	@Override
	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public String getModifier() {
		return modifier;
	}

	@Override
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	@Override
	public String getOriginalCreator() {
		return originalCreator;
	}

	@Override
	public void setOriginalCreator(String originalCreator) {
		this.originalCreator = originalCreator;
	}

	@Override
	public String getOriginalModifier() {
		return originalModifier;
	}

	@Override
	public void setOriginalModifier(String originalModifier) {
		this.originalModifier = originalModifier;
	}
	
	@Override
	public UUID getCreatorId() {
		return creatorId;
	}

	@Override
	public void setCreatorId(UUID creatorId) {
		this.creatorId = creatorId;
	}

	@Override
	public UUID getOriginalCreatorId() {
		return originalCreatorId;
	}

	@Override
	public void setOriginalCreatorId(UUID originalCreatorId) {
		this.originalCreatorId = originalCreatorId;
	}

	@Override
	public UUID getModifierId() {
		return modifierId;
	}

	@Override
	public void setModifierId(UUID modifierId) {
		this.modifierId = modifierId;
	}

	@Override
	public UUID getOriginalModifierId() {
		return originalModifierId;
	}

	@Override
	public void setOriginalModifierId(UUID originalModifierId) {
		this.originalModifierId = originalModifierId;
	}
	
	public boolean isTrimmed() {
		return trimmed;
	}

	public void setTrimmed(boolean trimmed) {
		this.trimmed = trimmed;
	}	

	public Map<String, BaseDto> getEmbedded() {
		if(embedded == null){
			embedded = new HashMap<>();
		}
		return embedded;
	}

	public void setEmbedded(Map<String, BaseDto> emmbedded) {
		this.embedded = emmbedded;
	}

	@Override
	public UUID getTransactionId() {
		return transactionId;
	}
	
	@Override
	public void setTransactionId(UUID transactionId) {
		this.transactionId = transactionId;	
	}
	
	@Override
	public UUID getRealmId() {
		return realmId;
	}
	
	@Override
	public void setRealmId(UUID realmId) {
		this.realmId = realmId;
	}

	@Override
	public String toString() {
		return getClass().getCanonicalName() + "[ id=" + getId() + " ]";
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (getId() != null ? getId().hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || !object.getClass().equals(getClass())) {
			return false;
		}

		AbstractDto other = (AbstractDto) object;
		return !((this.getId() == null && other.getId() != null)
				|| (this.getId() != null && !this.getId().equals(other.getId()))
				|| (this.getId() == null && other.getId() == null && this != other));
	}
}
