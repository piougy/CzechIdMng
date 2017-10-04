package eu.bcvsolutions.idm.vs.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import io.swagger.annotations.ApiModel;

/**
 * DTO for system-implementer in virtual system
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "implementers")
@ApiModel(description = "Relation between virtual system and identity or role")
public class VsSystemImplementerDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;

	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identity;
	
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

	public UUID getIdentity() {
		return identity;
	}

	public void setIdentity(UUID identity) {
		this.identity = identity;
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}
	
}
