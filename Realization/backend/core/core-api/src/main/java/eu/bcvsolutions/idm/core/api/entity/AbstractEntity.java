package eu.bcvsolutions.idm.core.api.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Common entity
 * 
 * @author Radek Tomiška 
 *
 */
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public abstract class AbstractEntity implements BaseEntity, AuditableEntity {

	private static final long serialVersionUID = 1969969154030951507L;

	@Id
	@GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(name = "id")
	private UUID id;

	@Audited
	@CreatedDate
	@Column(name = "created", nullable = false)
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@JsonProperty(access = Access.READ_ONLY)
	private Date created;

	@Audited
	@LastModifiedDate
	@Column(name = "modified", nullable = false)
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@JsonProperty(access = Access.READ_ONLY)
	private Date modified;

	@Audited
	@CreatedBy
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "creator", length = DefaultFieldLengths.NAME, nullable = false)
	@JsonProperty(access = Access.READ_ONLY)
	private String creator;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "original_creator", length = DefaultFieldLengths.NAME)
	@JsonProperty(access = Access.READ_ONLY)
	private String originalCreator;

	@Audited
	@LastModifiedBy
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "modifier", length = DefaultFieldLengths.NAME)
	@JsonProperty(access = Access.READ_ONLY)
	private String modifier;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "original_modifier", length = DefaultFieldLengths.NAME)
	@JsonProperty(access = Access.READ_ONLY)
	private String originalModifier;

	public AbstractEntity() {
	}

	public AbstractEntity(UUID uuid) {
		this.id = uuid;
	}

	/**
	 * Entity identifier
	 */
	@Override
	public UUID getId() {
		return id;
	}
	
	@Override
	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * Created date
	 */
	@Override
	public Date getCreated() {
		return created;
	}

	@Override
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * Last modified date
	 */
	@Override
	public Date getModified() {
		return modified;
	}

	@Override
	public void setModified(Date modified) {
		this.modified = modified;
	}
	
	/**
	 * Currently logged user, when record was created
	 */
	@Override
	public String getCreator() {
		return creator;
	}

	@Override
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * Currently logged user, when record was modified
	 */
	@Override
	public String getModifier() {
		return modifier;
	}
	
	@Override
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	/**
	 * Currently logged user, when record was modified
	 */
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
	
	/**
	 * Class + entity identifier
	 */
	@Override
	public String toString() {
		return getClass().getCanonicalName() + "[ id=" + getId() + " ]";
	}

	/**
	 * Based on entity identifier
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		hash += (getId() != null ? getId().hashCode() : 0);
		return hash;
	}

	/**
	 * Based on entity identifier
	 */
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
