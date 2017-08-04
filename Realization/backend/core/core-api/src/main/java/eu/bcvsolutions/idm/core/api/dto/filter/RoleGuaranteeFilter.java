package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;


/**
 * Filter for {@link IdmRoleGuarantee}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class RoleGuaranteeFilter extends DataFilter {

	private UUID guarantee;
	private UUID role;
	
	public RoleGuaranteeFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public RoleGuaranteeFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleGuaranteeDto.class, data);
	}
	
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
