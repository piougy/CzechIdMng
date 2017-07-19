package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

/**
 * Long running task ford automatic roles
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractAutomaticRoleTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	protected static final String PARAMETER_ROLE_TREE_NODE = "roleTreeNode";
	//
	private UUID roleTreeNode = null;
	
	public void setRoleTreeNode(UUID roleTreeNode) {
		this.roleTreeNode = roleTreeNode;
	}
	
	protected UUID getRoleTreeNode() {
		return roleTreeNode;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties =  super.getProperties();
		properties.put(PARAMETER_ROLE_TREE_NODE, roleTreeNode == null ? null : roleTreeNode);
		return properties;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> propertyNames = super.getPropertyNames();
		propertyNames.add(PARAMETER_ROLE_TREE_NODE);
		return propertyNames;
	}
}
