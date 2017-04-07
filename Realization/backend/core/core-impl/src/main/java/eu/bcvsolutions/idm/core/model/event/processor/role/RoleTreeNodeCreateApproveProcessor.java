package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractApprovableEventProcessor;

/**
 * Approve automatic role.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Approve create automatic role.")
public class RoleTreeNodeCreateApproveProcessor extends AbstractApprovableEventProcessor<IdmRoleTreeNodeDto> {
	
	public static final String PROCESSOR_NAME = "role-tree-node-create-approve-processor";

	public RoleTreeNodeCreateApproveProcessor() {
		super(RoleTreeNodeEventType.CREATE); // update is not supported
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
