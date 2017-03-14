package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;

/**
 * Approve automatic role.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Approve detele automatic role.")
public class RoleTreeNodeDeleteApproveProcessor extends AbstractApprovableEventProcessor<IdmRoleTreeNodeDto> {
	
	public static final String PROCESSOR_NAME = "role-tree-node-delete-approve-processor";
	
	public RoleTreeNodeDeleteApproveProcessor() {
		super(RoleTreeNodeEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	/**
	 * Before standard save
	 * 
	 * @return
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 1000;
	}

}
