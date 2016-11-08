package eu.bcvsolutions.idm.core.model.dto;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.domain.AuditClassMapping;

public class AuditFilter extends QuickFilter {
	
	private AuditClassMapping entity;
	
	private DateTime from;
	
	private DateTime to;
	
	private String modification;

	public String getModification() {
		return modification;
	}

	public void setModification(String modification) {
		this.modification = modification;
	}

	public AuditClassMapping getEntity() {
		return entity;
	}

	public void setEntity(AuditClassMapping entity) {
		this.entity = entity;
	}

	public DateTime getFrom() {
		return from;
	}

	public void setFrom(DateTime from) {
		this.from = from;
	}

	public DateTime getTo() {
		return to;
	}

	public void setTo(DateTime to) {
		this.to = to;
	}
	
	
}
