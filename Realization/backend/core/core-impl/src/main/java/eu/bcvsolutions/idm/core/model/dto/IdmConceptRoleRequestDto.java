package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.model.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;

/**
 * Dto for concept role request
 * 
 * @author svandav
 *
 */
@Relation(collectionRelation = "conceptRoleRequests")
public class IdmConceptRoleRequestDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = IdmRoleRequestDto.class)
	private UUID roleRequest;
	@Embedded(dtoClass = IdmIdentityContractDto.class)
	private UUID identityContract;
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;
	@Embedded(dtoClass = IdmIdentityRoleDto.class)
	private UUID identityRole; // For update and delete operations
	private LocalDate validFrom;
	private LocalDate validTill;
	private ConceptRoleRequestOperation operation;
	private RoleRequestState state;
	private String wfProcessId;

	public UUID getRoleRequest() {
		return roleRequest;
	}

	public void setRoleRequest(UUID roleRequest) {
		this.roleRequest = roleRequest;
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

	public ConceptRoleRequestOperation getOperation() {
		return operation;
	}

	public void setOperation(ConceptRoleRequestOperation operation) {
		this.operation = operation;
	}

	public RoleRequestState getState() {
		return state;
	}

	public void setState(RoleRequestState state) {
		this.state = state;
	}

	public String getWfProcessId() {
		return wfProcessId;
	}

	public void setWfProcessId(String wfProcessId) {
		this.wfProcessId = wfProcessId;
	}

	public UUID getIdentityRole() {
		return identityRole;
	}

	public void setIdentityRole(UUID identityRole) {
		this.identityRole = identityRole;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((identityContract == null) ? 0 : identityContract.hashCode());
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result + ((roleRequest == null) ? 0 : roleRequest.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((validFrom == null) ? 0 : validFrom.hashCode());
		result = prime * result + ((validTill == null) ? 0 : validTill.hashCode());
		result = prime * result + ((wfProcessId == null) ? 0 : wfProcessId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IdmConceptRoleRequestDto)) {
			return false;
		}
		IdmConceptRoleRequestDto other = (IdmConceptRoleRequestDto) obj;
		if (identityContract == null) {
			if (other.identityContract != null) {
				return false;
			}
		} else if (!identityContract.equals(other.identityContract)) {
			return false;
		}
		if (identityRole == null) {
			if (other.identityRole != null) {
				return false;
			}
		} else if (!identityRole.equals(other.identityRole)) {
			return false;
		}
		if (operation != other.operation) {
			return false;
		}
		if (role == null) {
			if (other.role != null) {
				return false;
			}
		} else if (!role.equals(other.role)) {
			return false;
		}
		if (state != other.state) {
			return false;
		}
		if (validFrom == null) {
			if (other.validFrom != null) {
				return false;
			}
		} else if (!validFrom.equals(other.validFrom)) {
			return false;
		}
		if (validTill == null) {
			if (other.validTill != null) {
				return false;
			}
		} else if (!validTill.equals(other.validTill)) {
			return false;
		}
		if (wfProcessId == null) {
			if (other.wfProcessId != null) {
				return false;
			}
		} else if (!wfProcessId.equals(other.wfProcessId)) {
			return false;
		}
		return true;
	}

}