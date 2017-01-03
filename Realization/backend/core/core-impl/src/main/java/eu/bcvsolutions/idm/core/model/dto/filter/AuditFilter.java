package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

public class AuditFilter extends QuickFilter {
	
	private Long id;
	
	private String type;
	
	private DateTime from;
	
	private DateTime to;
	
	private String modification;
	
	private String modifier;
	
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

	public Long getFrom() {
		return from == null ? null : from.getMillis();
	}

	public void setFrom(DateTime from) {
		this.from = from;
	}

	public Long getTo() {
		return to == null ? null : to.getMillis();
	}

	public void setTo(DateTime to) {
		this.to = to;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
}
