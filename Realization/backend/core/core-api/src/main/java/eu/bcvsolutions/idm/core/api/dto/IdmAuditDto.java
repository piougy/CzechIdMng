package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

/**
 * Default DTO for audit detail.
 * All default values for IdmAudit + Map with revision values
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
@Relation(collectionRelation = "audits")
public class IdmAuditDto implements BaseDto {

	private static final long serialVersionUID = 6910043282740335765L;

	private Long id;

    private UUID entityId;

    private long timestamp;

    private String modification;

    private String type;

    private String changedAttributes;

    private UUID modifierId;

    private String modifier;
    
    private String originalModifier;
	
	private UUID originalModifierId;

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
        return new Date(timestamp);
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

    @Override
    public void setId(Serializable id) {
        this.id = Long.valueOf(id.toString());
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

	public String getOriginalModifier() {
		return originalModifier;
	}

	public void setOriginalModifier(String originalModifier) {
		this.originalModifier = originalModifier;
	}

	public UUID getOriginalModifierId() {
		return originalModifierId;
	}

	public void setOriginalModifierId(UUID originalModifierId) {
		this.originalModifierId = originalModifierId;
	}
    
}
