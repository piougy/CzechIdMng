package eu.bcvsolutions.idm.acc.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * AssignedRoleDto is equivalent to IdmIdentityRoleDto with more simple
 * structure. Is using in provisioning.
 *
 * @author Vít Švanda
 */
@Relation(collectionRelation = "assignedRoles")
public class AssignedRoleDto extends AbstractDto implements ValidableEntity {

	private static final long serialVersionUID = 1L;

	private String externalId;
	private IdmIdentityContractDto identityContract;
	private IdmContractPositionDto contractPosition;
	private IdmRoleDto role;
	private LocalDate validFrom;
	private LocalDate validTill;
	private AbstractIdmAutomaticRoleDto roleTreeNode;
	private IdmIdentityRoleDto directRole; // direct identity role
	private IdmRoleCompositionDto roleComposition; // direct role
	private Map<String, List<Object>> attributes;
	public String getExternalId() {
		return externalId;
	}
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	public IdmIdentityContractDto getIdentityContract() {
		return identityContract;
	}
	public void setIdentityContract(IdmIdentityContractDto identityContract) {
		this.identityContract = identityContract;
	}
	public IdmContractPositionDto getContractPosition() {
		return contractPosition;
	}
	public void setContractPosition(IdmContractPositionDto contractPosition) {
		this.contractPosition = contractPosition;
	}
	public IdmRoleDto getRole() {
		return role;
	}
	public void setRole(IdmRoleDto role) {
		this.role = role;
	}
	public LocalDate getValidFrom() {
		return validFrom;
	}
	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}
	public LocalDate getValidTill() {
		return validTill;
	}
	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}
	public AbstractIdmAutomaticRoleDto getRoleTreeNode() {
		return roleTreeNode;
	}
	public void setRoleTreeNode(AbstractIdmAutomaticRoleDto roleTreeNode) {
		this.roleTreeNode = roleTreeNode;
	}
	public IdmIdentityRoleDto getDirectRole() {
		return directRole;
	}
	public void setDirectRole(IdmIdentityRoleDto directRole) {
		this.directRole = directRole;
	}
	public IdmRoleCompositionDto getRoleComposition() {
		return roleComposition;
	}
	public void setRoleComposition(IdmRoleCompositionDto roleComposition) {
		this.roleComposition = roleComposition;
	}
	public Map<String, List<Object>> getAttributes() {
		if (attributes == null) {
			attributes = new HashMap<>();
		}
		return attributes;
	}
	public void setAttributes(Map<String, List<Object>> attributes) {
		this.attributes = attributes;
	}
	@Override
	public String toString() {
		return "AssignedRoleDto [externalId=" + externalId + ", identityContract=" + identityContract
				+ ", contractPosition=" + contractPosition + ", role=" + role + ", validFrom=" + validFrom
				+ ", validTill=" + validTill + ", roleTreeNode=" + roleTreeNode + ", directRole=" + directRole
				+ ", roleComposition=" + roleComposition + ", attributes=" + attributes + "]";
	}

}