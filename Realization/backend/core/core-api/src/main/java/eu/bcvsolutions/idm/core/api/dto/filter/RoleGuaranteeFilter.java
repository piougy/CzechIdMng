package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;


/**
 * Filter for {@link IdmRoleGuarantee}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class RoleGuaranteeFilter extends QuickFilter {

	private UUID guarantee;
	private UUID role;
	
	public UUID getGuarantee() {
		return guarantee;
	}
	public void setGuarantee(UUID guarantee) {
		this.guarantee = guarantee;
	}
	public UUID getRole() {
		return role;
	}
	public void setRole(UUID role) {
		this.role = role;
	}
	
	
}
