package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.security.domain.DefaultGrantedAuthority;

/**
 * Dto for authority role
 * 
 * @author svandav
 *
 */
public class IdmRoleAuthorityDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;
	private String target;
	private String action;

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public String getAuthority() {
		return new DefaultGrantedAuthority(target, action).getAuthority();
	}

}