package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;

/**
 * Filter for automatic roles
 *
 * @author Radek Tomiška
 */
public class IdmRoleTreeNodeFilter extends DataFilter {

    private UUID roleId;
    private UUID treeNodeId;
    
    public IdmRoleTreeNodeFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmRoleTreeNodeFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleTreeNodeDto.class, data);
	}

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public UUID getTreeNodeId() {
        return treeNodeId;
    }

    public void setTreeNodeId(UUID treeNodeId) {
        this.treeNodeId = treeNodeId;
    }

}
