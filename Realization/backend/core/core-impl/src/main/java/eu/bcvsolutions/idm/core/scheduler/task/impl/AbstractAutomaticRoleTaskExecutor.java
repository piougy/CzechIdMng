package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;

/**
 * Long running task ford automatic roles
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractAutomaticRoleTaskExecutor extends AbstractLongRunningTaskExecutor<Boolean> {
	
	protected static final String PARAMETER_ROLE_TREE_NODE = "roleTreeNode";
	//
	private IdmRoleTreeNodeDto roleTreeNode = null;
	
	public void setRoleTreeNode(IdmRoleTreeNodeDto roleTreeNode) {
		this.roleTreeNode = roleTreeNode;
	}
	
	protected IdmRoleTreeNodeDto getRoleTreeNode() {
		return roleTreeNode;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties =  super.getProperties();
		properties.put(PARAMETER_ROLE_TREE_NODE, roleTreeNode == null ? null : roleTreeNode.getId());
		return properties;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> propertyNames = super.getPropertyNames();
		propertyNames.add(PARAMETER_ROLE_TREE_NODE);
		return propertyNames;
	}
}
