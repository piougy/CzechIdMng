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
	
	private Boolean virtual;
	
	private UUID passwordPolicyValidationId;

	private UUID passwordPolicyGenerationId;

	public UUID getPasswordPolicyValidationId() {
		return passwordPolicyValidationId;
	}

	public void setPasswordPolicyValidationId(UUID passwordPolicyValidationId) {
		this.passwordPolicyValidationId = passwordPolicyValidationId;
	}

	public UUID getPasswordPolicyGenerationId() {
		return passwordPolicyGenerationId;
	}

	public void setPasswordPolicyGenerationId(UUID passwordPolicyGenerationId) {
		this.passwordPolicyGenerationId = passwordPolicyGenerationId;
	}

	public Boolean getVirtual() {
		return virtual;
	}

	public void setVirtual(Boolean virtual) {
		this.virtual = virtual;
	}
}
