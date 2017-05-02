package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.joda.time.LocalDate;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * IdentityRole from WF
 * 
 * @author svanda
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "identityRoles")
public class IdmIdentityRoleDto extends AbstractDto implements ValidableEntity {

	private static final long serialVersionUID = 1L;
	@NotNull
	@Embedded(dtoClass = IdmIdentityContractDto.class)
	private UUID identityContract;
	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;
	@Embedded(dtoClass = IdmRoleTreeNodeDto.class)
	private UUID roleTreeNode;
	private LocalDate validFrom;
	private LocalDate validTill;
	private boolean automaticRole;

	public IdmIdentityRoleDto() {
	}

	public IdmIdentityRoleDto(UUID id) {
		super(id);
	}

	@Override
	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	@Override
	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTo) {
		this.validTill = validTo;
	}

	public UUID getIdentityContract() {
		return identityContract;
	}

	public void setIdentityContract(UUID identityContract) {
		this.identityContract = identityContract;
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}

	public boolean isAutomaticRole() {
		return automaticRole;
	}

	public void setAutomaticRole(boolean automaticRole) {
		this.automaticRole = automaticRole;
	}

	public UUID getRoleTreeNode() {
		return roleTreeNode;
	}

	public void setRoleTreeNode(UUID roleTreeNode) {
		this.roleTreeNode = roleTreeNode;
	}
}