package eu.bcvsolutions.idm.core.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.Identifiable;

import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;

/**
 * Common entity
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public abstract class AbstractEntity implements BaseEntity, AuditableEntity, Identifiable<Long> {

	private static final long serialVersionUID = 1969969154030951507L;

	@Id
	@Column(name = "id", precision = 18, scale = 0)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Audited
	@CreatedDate
	@Column(name = "created", nullable = false)
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date created;

	@Audited
	@LastModifiedDate
	@Column(name = "modified", nullable = false)
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date modified;

	@Audited
	@CreatedBy
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "creator", length = DefaultFieldLengths.NAME, nullable = false)
	private String creator;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "original_creator", length = DefaultFieldLengths.NAME)
	private String originalCreator;

	@Audited
	@LastModifiedBy
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "modifier", length = DefaultFieldLengths.NAME)
	private String modifier;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "original_modifier", length = DefaultFieldLengths.NAME)
	private String originalModifier;

	public AbstractEntity() {
	}

	public AbstractEntity(Long id) {
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

	@Override
	public Date getCreated() {
		return created;
	}

	@Override
	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public Date getModified() {
		return modified;
	}

	@Override
	public void setModified(Date modified) {
		this.modified = modified;
	}

	@Override
	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public String getCreator() {
		return creator;
	}

	@Override
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	@Override
	public String getModifier() {
		return modifier;
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

		AbstractEntity other = (AbstractEntity) object;
		if ((this.getId() == null && other.getId() != null)
				|| (this.getId() != null && !this.getId().equals(other.getId()))
				|| (this.getId() == null && other.getId() == null && this != other)) {
			return false;
		}

		return true;
	}
}
