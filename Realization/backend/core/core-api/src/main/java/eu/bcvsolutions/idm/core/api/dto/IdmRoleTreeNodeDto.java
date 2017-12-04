package eu.bcvsolutions.idm.core.api.dto;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import org.springframework.hateoas.core.Relation;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Automatic role
 *
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "roleTreeNodes")
public class IdmRoleTreeNodeDto extends AbstractIdmAutomaticRoleDto {

    private static final long serialVersionUID = 6360049218360559789L;

    @NotNull
    @Embedded(dtoClass = IdmTreeNodeDto.class)
    private UUID treeNode;
    @NotNull
    private RecursionType recursionType = RecursionType.NO;

    public UUID getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(UUID treeNode) {
        this.treeNode = treeNode;
    }

    public RecursionType getRecursionType() {
        return recursionType;
    }

    public void setRecursionType(RecursionType recursionType) {
        this.recursionType = recursionType;
    }
}
