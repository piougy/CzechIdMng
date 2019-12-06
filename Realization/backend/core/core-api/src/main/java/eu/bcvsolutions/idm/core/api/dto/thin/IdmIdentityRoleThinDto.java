package eu.bcvsolutions.idm.core.api.dto.thin;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;

/**
 * IdentityRole DTO - thin variant.
 * - purpose - disable embedded annotations only.
 *
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Relation(collectionRelation = "identityRoles")
public class IdmIdentityRoleThinDto extends IdmIdentityRoleDto {
	
	private static final long serialVersionUID = 1L;
	//
    @Embedded(enabled = false, dtoClass = IdmIdentityContractDto.class)
    private UUID identityContract;
    @Embedded(enabled = false, dtoClass = IdmContractPositionDto.class)
    private UUID contractPosition;
    @Embedded(enabled = false, dtoClass = AbstractIdmAutomaticRoleDto.class)
    private UUID roleTreeNode; // this attribute can't be renamed (backward compatibility) - AutomaticRole reference
    @Embedded(enabled = false, dtoClass = IdmIdentityRoleThinDto.class)
    private UUID directRole; // direct identity role
    @Embedded(enabled = false, dtoClass = IdmRoleCompositionDto.class)
    private UUID roleComposition; // direct role
	
    public UUID getIdentityContract() {
		return identityContract;
	}
	
	public void setIdentityContract(UUID identityContract) {
		this.identityContract = identityContract;
	}
	
	public UUID getContractPosition() {
		return contractPosition;
	}
	
	public void setContractPosition(UUID contractPosition) {
		this.contractPosition = contractPosition;
	}
	
	public UUID getRoleTreeNode() {
		return roleTreeNode;
	}
	
	public void setRoleTreeNode(UUID roleTreeNode) {
		this.roleTreeNode = roleTreeNode;
	}
	
	public UUID getDirectRole() {
		return directRole;
	}
	
	public void setDirectRole(UUID directRole) {
		this.directRole = directRole;
	}
	
	public UUID getRoleComposition() {
		return roleComposition;
	}
	
	public void setRoleComposition(UUID roleComposition) {
		this.roleComposition = roleComposition;
	}
}