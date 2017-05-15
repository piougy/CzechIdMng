package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * Dto for role guarantee
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "roleGuarantee")
public class IdmRoleGuaranteeDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID guarantee;
	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
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
