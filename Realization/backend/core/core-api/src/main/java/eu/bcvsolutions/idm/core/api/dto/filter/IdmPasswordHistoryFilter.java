package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

/**
 * Filter for password history
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdmPasswordHistoryFilter implements BaseFilter {
	
	private UUID identityId;

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

}
