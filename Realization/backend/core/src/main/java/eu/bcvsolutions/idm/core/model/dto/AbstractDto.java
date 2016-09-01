package eu.bcvsolutions.idm.core.model.dto;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.springframework.data.annotation.LastModifiedBy;

import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;

/**
 * Common dto
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public abstract class AbstractDto implements Serializable, BaseDto {
	
	private static final long serialVersionUID = 7512463222974374742L;
	//
	private Long id;
	private Date created;
	private Date modified;
	@Size(max = DefaultFieldLengths.NAME)
	private String creator;
	@Size(max = DefaultFieldLengths.NAME)
	private String modifier;
	@Size(max = DefaultFieldLengths.NAME)
	private String originalCreator;
	@Size(max = DefaultFieldLengths.NAME)
	private String originalModifier;

	public AbstractDto() {
	}

	public AbstractDto(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
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

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getOriginalCreator() {
		return originalCreator;
	}

	public void setOriginalCreator(String originalCreator) {
		this.originalCreator = originalCreator;
	}

	public String getOriginalModifier() {
		return originalModifier;
	}

	public void setOriginalModifier(String originalModifier) {
		this.originalModifier = originalModifier;
	}

}
