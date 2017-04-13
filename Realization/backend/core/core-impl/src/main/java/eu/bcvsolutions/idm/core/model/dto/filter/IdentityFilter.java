package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Filter for identities
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityFilter extends QuickFilter implements CorrelationFilter {
	
	/**
	 * Subordinates for given identity
	 */
	private IdmIdentity subordinatesFor;
	/**
	 * Subordinates by given tree structure
	 */
	private IdmTreeType subordinatesByTreeType;
	/**
	 * Managers for given identity
	 */
	private IdmIdentity managersFor;
	/**
	 * Managers by given tree structure
	 */
	private IdmTreeType managersByTreeType;
	/**
	 * Managers by given tree node
	 */
	private IdmTreeNode managersByTreeNode;
	/**
	 * roles - OR
	 */
	private List<IdmRole> roles;	
	/**
	 * Little dynamic search by identity property and value
	 */
	private String property;
	private String value;
	/**
	 * Identities for tree structure (by identity contract)
	 */
	private IdmTreeNode treeNode;
	/**
	 * Identities for tree structure recursively down
	 */
	private boolean recursively = true;
	/**
	 * Identities for tree structure (by identity contract)
	 */
	private UUID treeTypeId;

	public IdmIdentity getSubordinatesFor() {
		return subordinatesFor;
	}

	public void setSubordinatesFor(IdmIdentity subordinatesFor) {
		this.subordinatesFor = subordinatesFor;
	}

	public IdmTreeType getSubordinatesByTreeType() {
		return subordinatesByTreeType;
	}

	public void setSubordinatesByTreeType(IdmTreeType subordinatesByTreeType) {
		this.subordinatesByTreeType = subordinatesByTreeType;
	}
	
	public void setManagersFor(IdmIdentity managersFor) {
		this.managersFor = managersFor;
	}
	
	public IdmIdentity getManagersFor() {
		return managersFor;
	}
	
	public void setManagersByTreeType(IdmTreeType managersByTreeType) {
		this.managersByTreeType = managersByTreeType;
	}
	
	public IdmTreeType getManagersByTreeType() {
		return managersByTreeType;
	}
	
	public void setManagersByTreeNode(IdmTreeNode managersByTreeNode) {
		this.managersByTreeNode = managersByTreeNode;
	}
	
	public IdmTreeNode getManagersByTreeNode() {
		return managersByTreeNode;
	}
	
	public void setRoles(List<IdmRole> roles) {
		this.roles = roles;
	}
	
	public List<IdmRole> getRoles() {
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
	
	public IdmTreeNode getTreeNode() {
		return treeNode;
	}
	
	public void setTreeNode(IdmTreeNode treeNode) {
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
}
