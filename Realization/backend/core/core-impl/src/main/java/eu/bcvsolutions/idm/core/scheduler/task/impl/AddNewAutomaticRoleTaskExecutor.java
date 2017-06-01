package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;

/**
 * Long running task for add newly added automatic role to users.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Service
@Description("Add new automatic role from IdmRoleTreeNode.")
public class AddNewAutomaticRoleTaskExecutor extends AbstractLongRunningTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AddNewAutomaticRoleTaskExecutor.class);
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmRoleService roleService;
	//
	private IdmRoleTreeNodeDto roleTreeNode = null;
	
	public IdmRoleTreeNodeDto getRoleTreeNode() {
		return roleTreeNode;
	}

	public void setRoleTreeNode(IdmRoleTreeNodeDto roleTreeNode) {
		this.roleTreeNode = roleTreeNode;
	}

	@Override
	public Boolean process() {
		if (roleTreeNode == null) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_TASK_EMPTY);
		}
		IdmRole role = roleService.get(roleTreeNode.getRole());
		//
		// TODO: pageable?
		List<IdmIdentityContractDto> contracts = identityContractService.findAllByWorkPosition(roleTreeNode.getTreeNode(), roleTreeNode.getRecursionType());
		//
		counter = 0L;
		count = Long.valueOf(contracts.size());
		//
		LOG.debug("Start: Add new role [{}] by automatic role [{}]. Count: [{}]", role.getCode(), roleTreeNode.getId(), count);
		//
		boolean canContinue = true;
		for (IdmIdentityContractDto identityContract : contracts) {
			roleTreeNodeService.assignAutomaticRoles(identityContract, Sets.newHashSet(roleTreeNode), true);
			counter++;
			canContinue = updateState();
			if (!canContinue) {
				break;
			}
		}
		LOG.debug("End: Add new role [{}] by automatic role [{}]. Count: [{}]", role.getCode(), roleTreeNode.getId(), count);
		return Boolean.TRUE;
	}
}
