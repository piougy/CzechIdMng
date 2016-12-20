package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.Size;

import org.joda.time.DateTime;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Common dto
 * 
 * @author Radek Tomiška 
 *
 */
public abstract class AbstractDto implements Serializable, BaseDto, Auditable {
	
	private static final long serialVersionUID = 7512463222974374742L;
	//
	private UUID id;
	private DateTime created;
	private DateTime modified;
	@Size(max = DefaultFieldLengths.NAME)
	private String creator;
	private UUID creatorId;
	@Size(max = DefaultFieldLengths.NAME)
	private String modifier;
	private UUID modifierId;
	@Size(max = DefaultFieldLengths.NAME)
	private String originalCreator;
	private UUID originalCreatorId;
	@Size(max = DefaultFieldLengths.NAME)
	private String originalModifier;
	private UUID originalModifierId;

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
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(UUID	 id) {
		this.id = id;
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
