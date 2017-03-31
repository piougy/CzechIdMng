package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;

/**
 * Long running task for add newly added automatic role to users.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
@Description("Add new automatic role from IdmRoleTreeNode.")
public class AddNewAutomaticRoleTaskExecutor extends AbstractLongRunningTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AddNewAutomaticRoleTaskExecutor.class);
	
	@Autowired
	private IdmIdentityContractService identityContractService;
	
	@Autowired
	private IdmRoleTreeNodeService roleTreeNodeService;
	
	private IdmRoleTreeNode roleTreeNode = null;
	
	public IdmRoleTreeNode getRoleTreeNode() {
		return roleTreeNode;
	}

	public void setRoleTreeNode(IdmRoleTreeNode roleTreeNode) {
		this.roleTreeNode = roleTreeNode;
	}

	@Override
	public Boolean process() {
		if (roleTreeNode == null) {
			return Boolean.FALSE;
		}
		//
		// TODO: pageable?
		List<IdmIdentityContract> contracts = identityContractService.getContractsByWorkPosition(roleTreeNode.getTreeNode().getId(), roleTreeNode.getRecursionType());
		//
		counter = 0L;
		count = Long.valueOf(contracts.size());
		//
		LOG.debug("[AddNewAutomaticRoleTaskExecutor] Add new automatic roles. Count: [{}]", count);
		//
		boolean canContinue = true;
		for (IdmIdentityContract identityContract : contracts) {
			roleTreeNodeService.assignAutomaticRoles(identityContract, Sets.newHashSet(roleTreeNode), true);
			counter++;
			canContinue = updateState();
			if (!canContinue) {
				break;
			}
		}
		return Boolean.TRUE;
	}
}
