package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;

/**
 * Filter for tree node
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
public class TreeNodeFilter extends DataFilter implements CorrelationFilter {

    private UUID treeTypeId;
    private UUID treeNode;
    private Boolean defaultTreeType; // Search for tree nodes within the default tree type
    private String property; // Attribute name to search for, like 'code' or 'name'
    private String value; // Value of the attribute defined in property to search for
    /**
     * Tree nodes by tree structure recursively down
     */
    private boolean recursively = true;

    public TreeNodeFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public TreeNodeFilter(MultiValueMap<String, Object> data) {
        super(IdmTreeNodeDto.class, data);
    }

    public UUID getTreeTypeId() {
        return treeTypeId;
    }

    public void setTreeTypeId(UUID treeTypeId) {
        this.treeTypeId = treeTypeId;
    }

    public void setTreeNode(UUID treeNode) {
        this.treeNode = treeNode;
    }

    public UUID getTreeNode() {
        return treeNode;
    }

    public Boolean getDefaultTreeType() {
        return defaultTreeType;
    }

    public void setDefaultTreeType(Boolean defaultTreeType) {
        this.defaultTreeType = defaultTreeType;
    }

    public boolean isRecursively() {
        return recursively;
    }

    public void setRecursively(boolean recursively) {
        this.recursively = recursively;
    }

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

}
