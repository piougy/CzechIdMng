package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;

/**
 * Automatic roles recount while enabled identity cotract is created
 * 
 * TODO: integrate with role requests
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Automatic roles recount while enabled identity cotract is created.")
public class IdentityContractCreateByAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityContract> {
	
	public static final String PROCESSOR_NAME = "identity-contract-create-by-automatic-role-processor";
	@Autowired
	private IdmRoleTreeNodeService roleTreeNodeService;
	
	public IdentityContractCreateByAutomaticRoleProcessor() {
		super(IdentityContractEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContract> process(EntityEvent<IdmIdentityContract> event) {
		IdmIdentityContract contract = event.getContent();
		
		if(!contract.isDisabled() && contract.getWorkPosition() != null) {
			roleTreeNodeService.assignAutomaticRoles(contract, roleTreeNodeService.getAutomaticRoles(contract.getWorkPosition()), true);
		}
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * before save - wee need to check changes
	 */
	@Override
	public int getOrder() {
		return super.getOrder() + 1;
	}

}
