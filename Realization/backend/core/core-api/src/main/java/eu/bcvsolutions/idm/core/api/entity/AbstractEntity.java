package eu.bcvsolutions.idm.core.api.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.repository.listener.AuditableEntityListener;

/**
 * Common entity
 * 
 * @author Radek Tomiška 
 *
 */
@MappedSuperclass
@EntityListeners(AuditableEntityListener.class)
public abstract class AbstractEntity implements BaseEntity, Auditable, Disableable {

	private static final long serialVersionUID = 1969969154030951507L;

	@Id
	@JsonDeserialize(as = UUID.class)
	@GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "eu.bcvsolutions.idm.core.api.repository.generator.PreserveUuidGenerator")
	@Column(name = "id")
	private UUID id;

	@Audited
	@CreatedDate
	@Column(name = "created", nullable = false)
	@JsonProperty(access = Access.READ_ONLY)
	private DateTime created;

	@Audited
	@LastModifiedDate
	@Column(name = "modified")
	@JsonProperty(access = Access.READ_ONLY)
	private DateTime modified;

	@Audited
	@CreatedBy
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "creator", length = DefaultFieldLengths.NAME, nullable = false)
	@JsonProperty(access = Access.READ_ONLY)
	private String creator;
	
	@Audited
	@Column(name = "creator_id")
	@JsonProperty(access = Access.READ_ONLY)
	private UUID creatorId;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "original_creator", length = DefaultFieldLengths.NAME)
	@JsonProperty(access = Access.READ_ONLY)
	private String originalCreator;
	
	@Audited
	@Column(name = "original_creator_id")
	@JsonProperty(access = Access.READ_ONLY)
	private UUID originalCreatorId;

	@Audited
	@LastModifiedBy
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "modifier", length = DefaultFieldLengths.NAME)
	@JsonProperty(access = Access.READ_ONLY)
	private String modifier;
	
	@Audited
	@Column(name = "modifier_id")
	@JsonProperty(access = Access.READ_ONLY)
	private UUID modifierId;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "original_modifier", length = DefaultFieldLengths.NAME)
	@JsonProperty(access = Access.READ_ONLY)
	private String originalModifier;
	
	@Audited
	@Column(name = "original_modifier_id")
	@JsonProperty(access = Access.READ_ONLY)
	private UUID originalModifierId;
	
	@Audited
	@Column(name = "transaction_id")
	@JsonIgnore
	private UUID transactionId;
	
	@Audited
	@Column(name = "realm_id")
	@JsonIgnore // TODO: remove after implementation
	private UUID realmId;
	
	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;

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
	public void setId(Serializable id) {
		if (id != null) {
			Assert.isInstanceOf(UUID.class, id, "AbstractEntity supports only UUID identifier. For different identifier generalize BaseEntity.");
		}
		this.id = (UUID) id;
	}

	/**
	 * Created date
	 */
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
	public UUID getTransactionId() {
		return transactionId;
	}
	
	@Override
	public void setTransactionId(UUID transactionId) {
		this.transactionId = transactionId;
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	@Override
	public UUID getRealmId() {
		return realmId;
	}
	
	@Override
	public void setRealmId(UUID realmId) {
		this.realmId = realmId;
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
