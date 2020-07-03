package eu.bcvsolutions.idm.core.model.event.processor.tree;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.AutomaticRoleManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAutomaticRoleByTreeTaskExecutor;

/**
 * Recalculate automatic role, after tree node is moved.
 * 
 * @author Radek Tomiška
 * @since 10.4.0
 */
@Component(TreeNodeAfterMoveAutomaticRoleProcessor.PROCESSOR_NAME)
@Description("Recalculate automatic role, after tree node is moved.")
public class TreeNodeAfterMoveAutomaticRoleProcessor extends AbstractTreeNodeMoveAutomaticRoleProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TreeNodeAfterMoveAutomaticRoleProcessor.class);
	public static final String PROCESSOR_NAME = "core-tree-node-move-automatic-role-processor";
	//
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	@Autowired private EntityStateManager entityStateManager;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmTreeNodeDto> process(EntityEvent<IdmTreeNodeDto> event) {
		IdmTreeNodeDto treeNode = event.getContent();
		//
		Set<UUID> automaticRoles = new LinkedHashSet<>(); // preserve order => new automatic roles first
		// find currently defined automatic roles
		Set<IdmRoleTreeNodeDto> newAutomaticRoles = getRoleTreeNodeService().getAutomaticRolesByTreeNode(treeNode.getId());
		if (CollectionUtils.isNotEmpty(newAutomaticRoles)) {
			automaticRoles.addAll(
					newAutomaticRoles
						.stream()
						.map(IdmRoleTreeNodeDto::getId)
						.collect(Collectors.toSet())
			);
		}
		// previously defined automatic roles
		Set<UUID> previousAutomaticRoles = event.getSetProperty(
				PROPERTY_PREVIOUS_AUTOMATIC_ROLES,
				UUID.class
		);
		if (CollectionUtils.isNotEmpty(previousAutomaticRoles)) {
			automaticRoles.addAll(previousAutomaticRoles);
		}
		//
		if (CollectionUtils.isEmpty(automaticRoles)) {
			LOG.debug("Tree node [{}] was moved under new parent node [{}]. No automatic roles are affected.",
					treeNode.getId(), treeNode.getParent());
			//
			return new DefaultEventResult<>(event, this);
		}
		//
		// when automatic role recalculation is skipped, then flag for automatic role is created only
		// flag can be processed afterwards
		if (getBooleanProperty(AutomaticRoleManager.SKIP_RECALCULATION, event.getProperties())) {
			automaticRoles.forEach(automaticRole -> {
				LOG.debug("Automatic role [{}] recount is skipped after tree node [{}] was moved in tree structure. "
						+ "State [AUTOMATIC_ROLE_SKIPPED] for automatic role will be created only.",
						automaticRole, treeNode.getId());
				// 
				IdmEntityStateDto state = new IdmEntityStateDto();
				state.setOwnerId(automaticRole);
				state.setOwnerType(entityStateManager.getOwnerType(IdmRoleTreeNodeDto.class));
				state.setResult(
						new OperationResultDto
							.Builder(OperationState.BLOCKED)
							.setModel(new DefaultResultModel(CoreResultCode.AUTOMATIC_ROLE_SKIPPED))
							.build());
				
				entityStateManager.saveState(null, state);
			});
			//
			return new DefaultEventResult<>(event, this);
		}
		//
		// process all affected automatic roles
		ProcessAutomaticRoleByTreeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByTreeTaskExecutor.class);
		automaticRoleTask.setAutomaticRoles(Lists.newArrayList(automaticRoles));
		executeTask(event, automaticRoleTask);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after tree node is saved => previous automatic roles should be added into event properties
		return 100;
	}
	
	private void executeTask(EntityEvent<?> event, AbstractSchedulableStatefulExecutor<?> task) {
		if (event.getPriority() == PriorityType.IMMEDIATE) {
			longRunningTaskManager.executeSync(task);
		} else {
			task.setContinueOnException(true);
			if (longRunningTaskManager.isAsynchronous()) {
				task.setRequireNewTransaction(true);
			}
			longRunningTaskManager.execute(task);
		}
	}
}
