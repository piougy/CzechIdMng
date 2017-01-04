package eu.bcvsolutions.idm.core.model.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.core.model.entity.IdmAudit;

/**
 * Default DTO for audit detail.
 * All default values for IdmAudit + Map with revision values
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * TODO: AbstractDto has UUID, IdmAudit has Long?
 */
public class IdmAuditDto {
	
	private Long id;
	
	private UUID entityId;
	
	private long timestamp;

    private String modification;

	private String type;

	private String changedAttributes;
	
	private UUID modifierId;
	
	private String modifier;
	
	public UUID getModifierId() {
		return modifierId;
	}

	public void setModifierId(UUID modifierId) {
		this.modifierId = modifierId;
	}

	private Map<String, Object> revisionValues;
	
	public IdmAuditDto() {
		super();
		revisionValues = new HashMap<>();
	}
	
	/**
	 * Clone constructor from IdmAudit
	 * 
	 * @param audit
	 */
	public IdmAuditDto(IdmAudit audit) {
		this.setId(Long.parseLong(audit.getId().toString()));
		this.setModifierId(audit.getModifierId());
		this.changedAttributes = audit.getChangedAttributes();
		this.entityId = audit.getEntityId();
		this.modification = audit.getModification();
		this.modifier = audit.getModifier();
		this.timestamp = audit.getTimestamp();
		this.type = audit.getType();
		revisionValues = new HashMap<>();
	}
	
	public UUID getEntityId() {
		return entityId;
	}

	public void setEntityId(UUID entityId) {
		this.entityId = entityId;
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

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Date getRevisionDate() {
		return new Date( timestamp );
	}

	public Map<String, Object> getRevisionValues() {
		return revisionValues;
	}

	public void setRevisionValues(Map<String, Object> revisionValues) {
		this.revisionValues = revisionValues;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
}
