package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
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
public class IdentityFilter extends DataFilter implements CorrelationFilter {
	
	/**
	 * Subordinates for given identity
	 */
	public static final String PARAMETER_SUBORDINATES_FOR = "subordinatesFor";
	/**
	 * Subordinates by given tree structure
	 */
	public static final String PARAMETER_SUBORDINATES_BY_TREE_TYPE = "subordinatesByTreeType";
	/**
	 * Managers for given identity
	 */
	public static final String PARAMETER_MANAGERS_FOR = "managersFor";
	/**
	 * Managers by given tree structure
	 */
	public static final String PARAMETER_MANAGERS_BY_TREE_TYPE = "managersByTreeType";
	/**
	 * Returns managers by identity's contract working prosition 
	 */
	public static final String PARAMETER_MANAGERS_BY_CONTRACT_ID = "managersByContractId";
	
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
	/**
	 * managersByContractId with contract guarantees
	 */
	private boolean includeGuarantees;
	
	public IdentityFilter() {
		super(new LinkedMultiValueMap<>());
	}
	
	public IdentityFilter(MultiValueMap<String, Object> data) {
		super(data);
	}

	public IdmIdentity getSubordinatesFor() {
		return (IdmIdentity) data.getFirst(PARAMETER_SUBORDINATES_FOR);
	}

	public void setSubordinatesFor(IdmIdentity subordinatesFor) {
		data.set(PARAMETER_SUBORDINATES_FOR, subordinatesFor);
	}

	public IdmTreeType getSubordinatesByTreeType() {
		return (IdmTreeType) data.getFirst(PARAMETER_SUBORDINATES_BY_TREE_TYPE);
	}

	public void setSubordinatesByTreeType(IdmTreeType subordinatesByTreeType) {
		data.set(PARAMETER_SUBORDINATES_BY_TREE_TYPE, subordinatesByTreeType);
	}
	
	public void setManagersFor(IdmIdentity managersFor) {
		data.set(PARAMETER_MANAGERS_FOR, managersFor);
	}
	
	public IdmIdentity getManagersFor() {
		return (IdmIdentity) data.getFirst(PARAMETER_MANAGERS_FOR);
	}
	
	public void setManagersByTreeType(IdmTreeType managersByTreeType) {
		data.set(PARAMETER_MANAGERS_BY_TREE_TYPE, managersByTreeType);
	}
	
	public IdmTreeType getManagersByTreeType() {
		return (IdmTreeType) data.getFirst(PARAMETER_MANAGERS_BY_TREE_TYPE);
	}
	
	public UUID getManagersByContractId() {
		return (UUID) data.getFirst(PARAMETER_MANAGERS_BY_CONTRACT_ID);
	}
	
	public void setManagersByContractId(UUID managersByContractId) {
		data.set(PARAMETER_MANAGERS_BY_CONTRACT_ID, managersByContractId);
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
	
	public boolean isIncludeGuarantees() {
		return includeGuarantees;
	}
	
	public void setIncludeGuarantees(boolean includeGuarantees) {
		this.includeGuarantees = includeGuarantees;
	}
}
