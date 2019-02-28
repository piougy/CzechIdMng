package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;

/**
 * Filter for password history
 *
 * @author Ondrej Kopr
 *
 */
public class IdmPasswordHistoryFilter implements BaseFilter {
	
	private UUID identityId;
	private String identityUsername;
	private DateTime from;
    private DateTime till;
    private String creator;

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
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
