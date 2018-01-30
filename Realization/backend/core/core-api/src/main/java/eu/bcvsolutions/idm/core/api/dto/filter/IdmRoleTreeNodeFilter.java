package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;

/**
 * Filter for automatic roles
 *
 * @author Radek Tomiška
 */
public class IdmRoleTreeNodeFilter extends IdmAutomaticRoleFilter {

    private UUID treeNodeId;
    
    private RecursionType recursionType;
    
    public IdmRoleTreeNodeFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmRoleTreeNodeFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleTreeNodeDto.class, data);
	}

    public UUID getTreeNodeId() {
        return treeNodeId;
    }

    public void setTreeNodeId(UUID treeNodeId) {
        this.treeNodeId = treeNodeId;
    }

	public RecursionType getRecursionType() {
		return recursionType;
	}

	public void setRecursionType(RecursionType recursionType) {
		this.recursionType = recursionType;
	}

}
