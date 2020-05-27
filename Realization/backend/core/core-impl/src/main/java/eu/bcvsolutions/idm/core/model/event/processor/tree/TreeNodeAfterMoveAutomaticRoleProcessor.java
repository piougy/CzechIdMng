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

import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
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
@Description("Recalculate automatic role, before tree node is moved.")
public class TreeNodeAfterMoveAutomaticRoleProcessor extends AbstractTreeNodeMoveAutomaticRoleProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TreeNodeAfterMoveAutomaticRoleProcessor.class);
	public static final String PROCESSOR_NAME = "core-tree-node-move-automatic-role-processor";
	//
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmTreeNodeDto> process(EntityEvent<IdmTreeNodeDto> event) {
		IdmTreeNodeDto content = event.getContent();
		//
		Set<UUID> automaticRoles = new LinkedHashSet<>(); // preserve order => new automatic roles first
		// find currently defined automatic roles
		Set<IdmRoleTreeNodeDto> newAutomaticRoles = getRoleTreeNodeService().getAutomaticRolesByTreeNode(content.getId());
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
					content.getId(), content.getParent());
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
