package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;

/**
 * Filter for concept role request
 *
 * @author svandav
 */
public class ConceptRoleRequestFilter extends DataFilter {
    private UUID roleRequestId;
    private RoleRequestState state;
    private UUID identityRoleId;
    private UUID roleId;
    private UUID identityContractId;
    private UUID roleTreeNodeId;
    private ConceptRoleRequestOperation operation;
    
    public ConceptRoleRequestFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public ConceptRoleRequestFilter(MultiValueMap<String, Object> data) {
		super(IdmConceptRoleRequestDto.class, data);
	}

    public UUID getRoleRequestId() {
        return roleRequestId;
    }

    public void setRoleRequestId(UUID roleRequestId) {
        this.roleRequestId = roleRequestId;
    }

    public RoleRequestState getState() {
        return state;
    }

    public void setState(RoleRequestState state) {
        this.state = state;
    }

    public UUID getIdentityRoleId() {
        return identityRoleId;
    }

    public void setIdentityRoleId(UUID identityRoleId) {
        this.identityRoleId = identityRoleId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public UUID getIdentityContractId() {
        return identityContractId;
    }

    public void setIdentityContractId(UUID identityContractId) {
        this.identityContractId = identityContractId;
    }

    public UUID getRoleTreeNodeId() {
        return roleTreeNodeId;
    }

    public void setRoleTreeNodeId(UUID roleTreeNodeId) {
        this.roleTreeNodeId = roleTreeNodeId;
    }

    public ConceptRoleRequestOperation getOperation() {
        return operation;
    }

    public void setOperation(ConceptRoleRequestOperation operation) {
        this.operation = operation;
    }

}
