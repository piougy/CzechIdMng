package eu.bcvsolutions.idm.core.audit.entity;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.ModifiedEntityNames;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.repository.listener.IdmAuditListener;

/**
 * Default audit entity.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Entity
@RevisionEntity(IdmAuditListener.class)
@Table(name = "idm_audit", indexes = {
		@Index(name = "idx_idm_audit_timestamp", columnList = "timestamp"),
		@Index(name = "idx_idm_audit_modification", columnList = "modification"),
		@Index(name = "idx_idm_audit_original_modifier", columnList = "original_modifier"),
		@Index(name = "idx_idm_audit_modifier", columnList = "modifier"),
		@Index(name = "idx_idm_audit_entity_id", columnList = "entity_id"),
		@Index(name = "idx_idm_audit_changed_attributes", columnList = "changed_attributes"),
		@Index(name = "idx_idm_audit_owner_id", columnList = "owner_id"),
		@Index(name = "idx_idm_audit_owner_code", columnList = "owner_code"),
		@Index(name = "idx_idm_audit_owner_type", columnList = "owner_type"),
		@Index(name = "idx_idm_audit_sub_owner_id", columnList = "sub_owner_id"),
		@Index(name = "idx_idm_audit_sub_owner_code", columnList = "sub_owner_code"),
		@Index(name = "idx_idm_audit_sub_owner_type", columnList = "sub_owner_type")})
public class IdmAudit implements BaseEntity {

	private static final long serialVersionUID = -2762812245969363775L;

	// use CHANGED_COLUMNS_DELIMITER from IdmAuditDto
	@Deprecated
	public static final String DELIMITER = ",";
	
	@Id
	@GeneratedValue
    @Column(name = "id")
	@RevisionNumber
	private Long id;
	
	@Column(name = "entity_id")
	private UUID entityId;
	
	@RevisionTimestamp
	private long timestamp;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "revchanges", joinColumns = @JoinColumn(name = "rev"))
	@Fetch(FetchMode.JOIN)
	@ModifiedEntityNames
	@Column(name = "modified_entity_names")
	private Set<String> modifiedEntityNames;
	
	@Column(name = "modification")
    private String modification;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "changed_attributes")
	private String changedAttributes;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "modifier", length = DefaultFieldLengths.NAME)
	private String modifier;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "original_modifier", length = DefaultFieldLengths.NAME)
	private String originalModifier;
	
	@Column(name = "modifier_id")
	private UUID modifierId;
	
	@Column(name = "original_modifier_id")
	private UUID originalModifierId;
	
	@Column(name = "realm_id")
	@JsonIgnore // TODO: remove after implementation
	private UUID realmId;
	
	@Column(name = "owner_id")
	private String ownerId;
	
	@Column(name = "owner_code")
	private String ownerCode;
	
	@Column(name = "owner_type")
	private String ownerType;
	
	@Column(name = "sub_owner_id")
	private String subOwnerId;
	
	@Column(name = "sub_owner_code")
	private String subOwnerCode;
	
	@Column(name = "sub_owner_type")
	private String subOwnerType;
	
	@Override
	public Serializable getId() {
		return this.id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = (Long) id;
	}
	
	public UUID getEntityId() {
		return entityId;
	}

	public void setEntityId(UUID entityId) {
		this.entityId = entityId;
	}

	public String getOriginalModifier() {
		return originalModifier;
	}

	public void setOriginalModifier(String originalModifier) {
		this.originalModifier = originalModifier;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	
	public UUID getModifierId() {
		return modifierId;
	}

	public void setModifierId(UUID modifierId) {
		this.modifierId = modifierId;
	}

	public UUID getOriginalModifierId() {
		return originalModifierId;
	}

	public void setOriginalModifierId(UUID originalModifierId) {
		this.originalModifierId = originalModifierId;
	}
	
	public UUID getRealmId() {
		return realmId;
	}
	
	public void setRealmId(UUID realmId) {
		this.realmId = realmId;
	}

	public String getChangedAttributes() {
		return changedAttributes;
	}

	public void setChangedAttributes(String changedAttributes) {
		this.changedAttributes = changedAttributes;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getModification() {
		return modification;
	}

	public void setModification(String modification) {
		this.modification = modification;
	}
	
	public void addChanged(String changedColumn) {
		if (StringUtils.isEmpty(changedAttributes)) {
			changedAttributes = changedColumn;
		} else {
			changedAttributes = String.format("%s%s%s", changedAttributes, IdmAuditDto.CHANGED_COLUMNS_DELIMITER, changedColumn);
		}
	}
	
	public void addChanged(List<String> changedColumns) {
		if (changedColumns == null) {
			// nothing to add
			return;
		}
		changedColumns.forEach(changedColumn -> {
			addChanged(changedColumn);
		});
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Set<String> getModifiedEntityNames() {
		return modifiedEntityNames;
	}

	public void setModifiedEntityNames(Set<String> modifiedEntityNames) {
		this.modifiedEntityNames = modifiedEntityNames;
	}
	
	@Transient
	public Date getRevisionDate() {
		return new Date( timestamp );
	}
	
	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( !(o instanceof IdmAudit) ) {
			return false;
		}

		final IdmAudit that = (IdmAudit) o;

		if ( modifiedEntityNames != null ? !modifiedEntityNames.equals( that.modifiedEntityNames )
				: that.modifiedEntityNames != null ) {
			return false;
		}
		
		if (timestamp == that.timestamp) {
			return true;
		} 

		return false;
	}
	
	@Override
	public int hashCode() {
		int result;
		result = (int) (timestamp ^ (timestamp >>> 32));
		return 31 * result + (modifiedEntityNames != null ? modifiedEntityNames.hashCode() : 0);
	}
	
	@Override
	public String toString() {
		return "IdmAuditEntity("
				+ "id = " + serialVersionUID
				+ ", revisionDate = " + DateFormat.getDateTimeInstance().format( getRevisionDate() )
				+ ", modifiedEntityNames = " + modifiedEntityNames + ")";
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerCode() {
		return ownerCode;
	}

	public void setOwnerCode(String ownerCode) {
		this.ownerCode = ownerCode;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public String getSubOwnerId() {
		return subOwnerId;
	}

	public void setSubOwnerId(String subOwnerId) {
		this.subOwnerId = subOwnerId;
	}

	public String getSubOwnerCode() {
		return subOwnerCode;
	}

	public void setSubOwnerCode(String subOwnerCode) {
		this.subOwnerCode = subOwnerCode;
	}

	public String getSubOwnerType() {
		return subOwnerType;
	}

	public void setSubOwnerType(String subOwnerType) {
		this.subOwnerType = subOwnerType;
	}
}
