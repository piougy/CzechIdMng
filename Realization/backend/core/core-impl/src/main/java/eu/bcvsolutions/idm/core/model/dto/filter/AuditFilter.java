package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

public class AuditFilter extends QuickFilter {
	
	private Long id;
	
	private String type;
	
	private DateTime from;
	
	private DateTime till;
	
	private String modification;
	
	private String modifier;
	
	private String changedAttributes;
	
	private UUID entityId;

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
}
