package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;

/**
 * Automatic roles recount while enabled identity cotract is created
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Automatic roles recount while enabled identity cotract is created.")
public class IdentityContractCreateAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityContract> {
	
	@Autowired
	private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	
	public IdentityContractCreateAutomaticRoleProcessor() {
		super(IdentityContractEventType.CREATE);
	}

	@Override
	public EventResult<IdmIdentityContract> process(EntityEvent<IdmIdentityContract> event) {
		IdmIdentityContract contract = event.getContent();
		
		if(!contract.isDisabled() && contract.getWorkingPosition() != null) {
			assignAutomaticRoles(contract, roleTreeNodeService.getAutomaticRoles(contract.getWorkingPosition()));
		}
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Add automatic roles - without approving for now
	 * TODO: connect to role request API 
	 * 
	 * @param contract
	 * @param automaticRoles
	 */
	protected void assignAutomaticRoles(IdmIdentityContract contract, Set<IdmRoleTreeNode> automaticRoles) {
		Assert.notNull(automaticRoles);
		//
		automaticRoles.forEach(roleTreeNode -> {
				IdmIdentityRole identityRole = new IdmIdentityRole(contract);
				identityRole.setRole(roleTreeNode.getRole());
				identityRole.setRoleTreeNode(roleTreeNode);
				identityRoleService.save(identityRole);
			});
	}
	
	/**
	 * before save - wee need to check changes
	 */
	@Override
	public int getOrder() {
		return super.getOrder() + 1;
	}

}
