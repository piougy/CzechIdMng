package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
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
}
