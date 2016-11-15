package eu.bcvsolutions.idm.core.model.entity;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.*;
import javax.validation.constraints.Size;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.ModifiedEntityNames;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.config.domain.IdmAuditListener;

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
		@Index(name = "idx_idm_audit_changed_attributes", columnList = "changed_attributes") })
public class IdmAudit implements BaseEntity {

	private static final long serialVersionUID = -2762812245969363775L;

	public static final String DELIMITER = ",";
	
	@Id
	@GeneratedValue
    @JsonProperty(value = "id")
    @Column(name = "id")
	@RevisionNumber
	private Long id;
	
	@JsonIgnore
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
	
	@Column(name = "entity_id")
	private UUID entityId;

	public String getOriginalModifier() {
		return originalModifier;
	}

	public void setOriginalModifier(String originalModifier) {
		this.originalModifier = originalModifier;
	}

	public UUID getEntityId() {
		return entityId;
	}

	public void setEntityId(UUID entityId) {
		this.entityId = entityId;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
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
	
	public void addChanged(String changedColumns) {
		// TODO: use StringBuilder?
		if (this.changedAttributes == null || this.changedAttributes.isEmpty()) {
			this.changedAttributes = changedColumns;
		} else {
			this.changedAttributes += DELIMITER + " " + changedColumns;
		}
	}
	
	public void addChanged(List<String> changedColumns) {
		this.addChanged(String.join(DELIMITER, changedColumns));
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
		result = 31 * result + (modifiedEntityNames != null ? modifiedEntityNames.hashCode() : 0);
		return result;
	}
	
	@Override
	public String toString() {
		return "IdmAuditEntity("
				+ "id = " + serialVersionUID
				+ ", revisionDate = " + DateFormat.getDateTimeInstance().format( getRevisionDate() )
				+ ", modifiedEntityNames = " + modifiedEntityNames + ")";
	}

	@Override
	public Serializable getId() {
		return this.id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = (Long) id;
	}

}
