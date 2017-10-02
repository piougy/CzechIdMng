package eu.bcvsolutions.idm.core.api.audit.dto.filter;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for audit
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdmAuditFilter implements BaseFilter {

    private Long id;
    private String text;
    private String type;
    private DateTime from;
    private DateTime till;
    private String modification;
    private String modifier;
    private String changedAttributes;
    private UUID entityId;
    
    // owner + sub owner attributes
    private String ownerId;
    private List<String> ownerIds;
	private String ownerCode;
	private String ownerType;
	private String subOwnerId;
	private String subOwnerCode;
	private String subOwnerType;

	public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getModification() {
        return modification;
    }

    public void setModification(String modification) {
        this.modification = modification;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DateTime getFrom() {
        return from;
    }

    public void setFrom(DateTime from) {
        this.from = from;
    }

    public DateTime getTill() {
        return till;
    }

    public void setTill(DateTime till) {
        this.till = till;
    }

    public String getChangedAttributes() {
        return changedAttributes;
    }

    public void setChangedAttributes(String changedAttributes) {
        this.changedAttributes = changedAttributes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

	public List<String> getOwnerIds() {
		return ownerIds;
	}

	public void setOwnerIds(List<String> ownerIds) {
		this.ownerIds = ownerIds;
	}
}
