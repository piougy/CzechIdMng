package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * Assign role to catalogue. Role can be in more catalogue items.
 * 
 * @author Radek Tomiška
 * 
 */
@Relation(collectionRelation = "roleCatalogueRoles")
public class IdmRoleCatalogueRoleDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Embedded(dtoClass = IdmRoleCatalogueDto.class)
	private UUID roleCatalogue;
	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;

	public UUID getRoleCatalogue() {
		return roleCatalogue;
	}

	public void setRoleCatalogue(UUID roleCatalogue) {
		this.roleCatalogue = roleCatalogue;
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}
}
