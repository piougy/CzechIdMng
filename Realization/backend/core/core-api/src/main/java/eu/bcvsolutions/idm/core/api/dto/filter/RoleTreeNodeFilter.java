package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

/**
 * Filter for automatic roles
 *
 * @author Radek Tomiška
 */
public class RoleTreeNodeFilter implements BaseFilter {

    private UUID roleId;
    private UUID treeNodeId;

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
