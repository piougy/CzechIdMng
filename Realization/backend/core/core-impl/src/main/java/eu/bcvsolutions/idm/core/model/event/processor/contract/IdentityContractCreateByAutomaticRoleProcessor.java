package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Automatic roles recount while enabled identity cotract is created
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Automatic roles recount while enabled identity cotract is created.")
public class IdentityContractCreateByAutomaticRoleProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {
	
	public static final String PROCESSOR_NAME = "identity-contract-create-by-automatic-role-processor";
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	
	public IdentityContractCreateByAutomaticRoleProcessor() {
		super(IdentityContractEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		// contract is or could be valid in future
		if(contract.isValidNowOrInFuture() && contract.getWorkPosition() != null) {
			Set<IdmRoleTreeNodeDto> automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(contract.getWorkPosition());
			if (!automaticRoles.isEmpty()) {
				roleTreeNodeService.assignAutomaticRoles(contract, automaticRoles);
			}
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * before save - wee need to check changes
	 */
	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}

}
