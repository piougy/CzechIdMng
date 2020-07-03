package eu.bcvsolutions.idm.core.model.event.processor.tree;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;

/**
 * Recalculate automatic roles, when tree node is moved under different parent.
 * 
 * Note" automatic roles are recount for the updated node only. When new tree node is created,
 * then recount automatic roles is not needed in this case (contract or position cannot exist before).
 * 
 * @author Radek Tomiška
 * @since 10.4.0
 */
public abstract class AbstractTreeNodeMoveAutomaticRoleProcessor extends CoreEventProcessor<IdmTreeNodeDto> {
	
	public static final String PROPERTY_PREVIOUS_AUTOMATIC_ROLES = "core:previous-automatic-roles";
	//
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;

	public AbstractTreeNodeMoveAutomaticRoleProcessor() {
		// Update only. When new tree node is created, then recount automatic roles is not needed in this case (contract or position cannot exist before).
		super(TreeNodeEventType.UPDATE);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmTreeNodeDto> event) {
		IdmTreeNodeDto originalSource = event.getOriginalSource();
		IdmTreeNodeDto content = event.getContent();
		//
		return super.conditional(event)
				&& !Objects.equals(originalSource.getParent(), content.getParent());
	}
	
	protected IdmRoleTreeNodeService getRoleTreeNodeService() {
		return roleTreeNodeService;
	}
}
