package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Long running task for automatic roles
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractAutomaticRoleTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	public static final String PARAMETER_ROLE_TREE_NODE = "roleTreeNode";
	//
	private UUID roleTreeNodeId = null;
	
	@Deprecated
	public void setRoleTreeNodeId(UUID roleTreeNodeId) {
		this.setAutomaticRoleId(roleTreeNodeId);
	}
	
	@Deprecated
	protected UUID getRoleTreeNodeId() {
		return this.getAutomaticRoleId();
	}
	
	public void setAutomaticRoleId(UUID automaticRoleId) {
		this.roleTreeNodeId = automaticRoleId;
	}
	
	protected UUID getAutomaticRoleId() {
		return this.roleTreeNodeId;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties =  super.getProperties();
		properties.put(PARAMETER_ROLE_TREE_NODE, roleTreeNodeId == null ? null : roleTreeNodeId);
		return properties;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> propertyNames = super.getPropertyNames();
		propertyNames.add(PARAMETER_ROLE_TREE_NODE);
		return propertyNames;
	}
}
