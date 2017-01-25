package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Filter for systems
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class SysSystemFilter extends QuickFilter {
	
	private UUID passwordPolicyId;

	public UUID getPasswordPolicyId() {
		return passwordPolicyId;
	}

	public void setPasswordPolicyId(UUID passwordPolicyId) {
		this.passwordPolicyId = passwordPolicyId;
	}
}
