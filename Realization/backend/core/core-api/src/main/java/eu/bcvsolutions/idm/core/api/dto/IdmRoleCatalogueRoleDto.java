package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Requestable;

/**
 * Assign role to catalogue. Role can be in more catalogue items.
 * 
 * @author Radek Tomiška
 * 
 */
@Relation(collectionRelation = "roleCatalogueRoles")
public class IdmRoleCatalogueRoleDto extends AbstractDto implements Requestable {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Embedded(dtoClass = IdmRoleCatalogueDto.class)
	private UUID roleCatalogue;
	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;
	@Embedded(dtoClass = IdmRequestItemDto.class)
	private UUID requestItem; // Isn't persist in the entity
	@Embedded(dtoClass = IdmRequestDto.class)
	private UUID request; // Isn't persist in the entity

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
	
	@Override
	public UUID getRequestItem() {
		return requestItem;
	}

	@Override
	public void setRequestItem(UUID requestItem) {
		this.requestItem = requestItem;
	}

	@Override
	public UUID getRequest() {
		return request;
	}

	@Override
	public void setRequest(UUID request) {
		this.request = request;
	}
}
