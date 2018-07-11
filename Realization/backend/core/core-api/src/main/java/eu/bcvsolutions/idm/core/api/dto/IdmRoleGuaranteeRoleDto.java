package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * Dto for role guarantee - role
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "roleGuaranteeRoles")
public class IdmRoleGuaranteeRoleDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role; // owner
	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID guaranteeRole; // guarantee as role

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
}
