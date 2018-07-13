package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * Dto for role guarantee - identity
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "roleGuarantees")
public class IdmRoleGuaranteeDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role; // owner
	@NotNull
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID guarantee; // guarantee as identity

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
	 * Guarantee as identity
	 * 
	 * @return
	 */
	public UUID getGuarantee() {
		return guarantee;
	}

	/**
	 * Guarantee as identity
	 * 
	 * @param guarantee
	 */
	public void setGuarantee(UUID guarantee) {
		this.guarantee = guarantee;
	}
}
