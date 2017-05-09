package eu.bcvsolutions.idm.core.api.dto;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import javax.validation.constraints.Size;
import java.util.UUID;

/**
 * Type of tree structure
 *
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "treeTypes")
public class IdmTreeTypeDto extends AbstractDto {

    private static final long serialVersionUID = 3883227192651419232L;
    @NotEmpty
    @Size(min = 0, max = DefaultFieldLengths.NAME)
    private String code;
    @NotEmpty
    @Size(min = 0, max = DefaultFieldLengths.NAME)
    private String name;
    private boolean defaultTreeType = false; // true, when this type is defined as default organization structure
    @Embedded(dtoClass = IdmTreeNodeDto.class, enabled = false)
    private UUID defaultTreeNode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultTreeType() {
        return defaultTreeType;
    }

    public void setDefaultTreeType(boolean defaultTreeType) {
        this.defaultTreeType = defaultTreeType;
    }

    public UUID getDefaultTreeNode() {
        return defaultTreeNode;
    }

    public void setDefaultTreeNode(UUID defaultTreeNode) {
        this.defaultTreeNode = defaultTreeNode;
    }
}
