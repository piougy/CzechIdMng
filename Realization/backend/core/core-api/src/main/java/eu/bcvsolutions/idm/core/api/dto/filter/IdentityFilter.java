package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Filter for identities
 *
 * @author Radek Tomiška
 */
public class IdentityFilter extends QuickFilter implements CorrelationFilter {

    /**
     * Subordinates for given identity (username)
     */
    private String subordinatesFor;
    /**
     * Subordinates by given tree structure
     */
    private UUID subordinatesByTreeType;
    /**
     * Managers for given identity (username)
     */
    private String managersFor;
    /**
     * Managers by given tree structure
     */
    private UUID managersByTreeType;
    /**
     * Managers by given tree node
     */
    private UUID managersByTreeNode;
    /**
     * roles - OR
     */
    private List<UUID> roles;
    /**
     * Little dynamic search by identity property and value
     */
    private String property;
    private String value;
    /**
     * Identities for tree structure (by identity contract)
     */
    private UUID treeNode;
    /**
     * Identities for tree structure recursively down
     */
    private boolean recursively = true;
    /**
     * Identities for tree structure (by identity contract)
     */
    private UUID treeTypeId;
    private UUID managersByContractId;
    private Boolean includeGuarantees;

    public String getSubordinatesFor() {
        return subordinatesFor;
    }

    public void setSubordinatesFor(String subordinatesFor) {
        this.subordinatesFor = subordinatesFor;
    }

    public UUID getSubordinatesByTreeType() {
        return subordinatesByTreeType;
    }

    public void setSubordinatesByTreeType(UUID subordinatesByTreeType) {
        this.subordinatesByTreeType = subordinatesByTreeType;
    }

    public void setManagersFor(String managersFor) {
        this.managersFor = managersFor;
    }

    public String getManagersFor() {
        return managersFor;
    }

    public void setManagersByTreeType(UUID managersByTreeType) {
        this.managersByTreeType = managersByTreeType;
    }

    public UUID getManagersByTreeType() {
        return managersByTreeType;
    }

    public void setManagersByTreeNode(UUID managersByTreeNode) {
        this.managersByTreeNode = managersByTreeNode;
    }

    public UUID getManagersByTreeNode() {
        return managersByTreeNode;
    }

    public void setRoles(List<UUID> roles) {
        this.roles = roles;
    }

    public List<UUID> getRoles() {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        return roles;
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

    public UUID getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(UUID treeNode) {
        this.treeNode = treeNode;
    }

    public UUID getTreeTypeId() {
        return treeTypeId;
    }

    public void setTreeTypeId(UUID treeTypeId) {
        this.treeTypeId = treeTypeId;
    }

    public boolean isRecursively() {
        return recursively;
    }

    public void setRecursively(boolean recursively) {
        this.recursively = recursively;
    }

    public UUID getManagersByContractId() {
        return managersByContractId;
    }

    public void setManagersByContractId(UUID managersByContractId) {
        this.managersByContractId = managersByContractId;
    }

    public Boolean isIncludeGuarantees() {
        return includeGuarantees;
    }

    public void setIncludeGuarantees(Boolean includeGuarantees) {
        this.includeGuarantees = includeGuarantees;
    }
}
