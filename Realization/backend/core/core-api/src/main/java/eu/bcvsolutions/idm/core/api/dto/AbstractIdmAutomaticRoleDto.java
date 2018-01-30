package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * DTO for automatic roles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public abstract class AbstractIdmAutomaticRoleDto extends AbstractDto {

	private static final long serialVersionUID = -5520693984256464570L;
	
	@NotNull
	private String name;
	
	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
    private UUID role;

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}