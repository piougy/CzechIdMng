package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Dto for role guarantee - role
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
@Relation(collectionRelation = "roleGuaranteeRoles")
public class IdmRoleGuaranteeRoleDto extends AbstractDto implements ExternalIdentifiable, Requestable {

	private static final long serialVersionUID = 1L;

	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role; // owner
	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID guaranteeRole; // guarantee as role
	@Embedded(dtoClass = IdmRequestItemDto.class)
	private UUID requestItem; // Isn't persist in the entity
	@Embedded(dtoClass = IdmRequestDto.class)
	private UUID request; // Isn't persist in the entity

	/**
	 * Owner
	 * 
	 * @return
	 */
	public UUID getRole() {
		return role;
	}

	/**
	 * Owner
	 * 
	 * @param role
	 */
	public void setRole(UUID role) {
		this.role = role;
	}
	
	/**
	 * Guarantee as role
	 * 
	 * @return
	 */
	public UUID getGuaranteeRole() {
		return guaranteeRole;
	}
	
	/**
	 * Guarantee as role
	 * 
	 * @param guaranteeRole
	 */
	public void setGuaranteeRole(UUID guaranteeRole) {
		this.guaranteeRole = guaranteeRole;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
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
