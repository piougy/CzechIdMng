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
public class IdentityFilter extends QuickFilter {
	
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
	private Object value;
	/**
	 * Identities for tree structure (by identity contract)
	 */
	private UUID treeNodeId;
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

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public void setTreeNodeId(UUID treeNodeId) {
		this.treeNodeId = treeNodeId;
	}
	
	public UUID getTreeNodeId() {
		return treeNodeId;
	}
	
	public UUID getTreeTypeId() {
		return treeTypeId;
	}
	
	public void setTreeTypeId(UUID treeTypeId) {
		this.treeTypeId = treeTypeId;
	}
}
