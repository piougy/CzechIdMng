package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Filter for password history
 *
 * @author Ondrej Kopr
 *
 */
public class IdmPasswordHistoryFilter implements BaseFilter {
	
	private UUID identityId;
	private String identityUsername;
	private ZonedDateTime from;
    private ZonedDateTime till;
    private String creator;

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

	public ZonedDateTime getFrom() {
		return from;
	}

	public void setFrom(ZonedDateTime from) {
		this.from = from;
	}

	public ZonedDateTime getTill() {
		return till;
	}

	public void setTill(ZonedDateTime till) {
		this.till = till;
	}

	public String getIdentityUsername() {
		return identityUsername;
	}

	public void setIdentityUsername(String identityUsername) {
		this.identityUsername = identityUsername;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

}
