package eu.bcvsolutions.idm.core.audite.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;

/**
 * Default filter for IdmAuditIdentityService
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */

public class AuditIdentityFilter implements AuditEntityFilter{
	
	private String username;
	private DateTime till;
	private DateTime from;
	private UUID id;
	private String modifier;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public DateTime getTill() {
		return till;
	}
	public void setTill(DateTime validTill) {
		this.till = validTill;
	}
	public DateTime getFrom() {
		return from;
	}
	public void setFrom(DateTime validFrom) {
		this.from = validFrom;
	}
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
}
