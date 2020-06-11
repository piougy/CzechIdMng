package eu.bcvsolutions.idm.core.model.event.processor.tree;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Prepare recalculate automatic role, before tree node is moved.
 * 
 * @author Radek Tomiška
 * @since 10.4.0
 */
@Component(TreeNodeBeforeMoveAutomaticRoleProcessor.PROCESSOR_NAME)
@Description("Prepare recalculate automatic role, before tree node is moved. We need to load previously defined automatic roles, which should be recounted too.")
public class TreeNodeBeforeMoveAutomaticRoleProcessor extends AbstractTreeNodeMoveAutomaticRoleProcessor {

	public static final String PROCESSOR_NAME = "core-tree-node-before-move-automatic-role-processor";
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmTreeNodeDto> process(EntityEvent<IdmTreeNodeDto> event) {
		IdmTreeNodeDto originalSource = event.getOriginalSource();
		//
		// find currently defined automatic roles
		Set<UUID> automaticRoles = getRoleTreeNodeService()
				.getAutomaticRolesByTreeNode(originalSource.getId())
				.stream()
				.map(IdmRoleTreeNodeDto::getId)
				.collect(Collectors.toSet());
		event.getProperties().put(PROPERTY_PREVIOUS_AUTOMATIC_ROLES, new HashSet<>(automaticRoles));
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// before tree node is saved => we need original forest index to find current automatic roles
		return -100;
	}
}
