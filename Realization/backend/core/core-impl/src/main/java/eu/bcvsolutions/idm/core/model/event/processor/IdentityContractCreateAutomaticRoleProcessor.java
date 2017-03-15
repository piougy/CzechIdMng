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
 * TODO: integrate with role requests
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Automatic roles recount while enabled identity cotract is created.")
public class IdentityContractCreateAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityContract> {
	
	@Autowired
	private IdmRoleTreeNodeService roleTreeNodeService;
//	@Autowired
//	private IdmRoleRequestService roleRequestService;
//	@Autowired
//	private IdmConceptRoleRequestService conceptRoleRequestService;
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
	 * Add automatic roles by standard role request
	 * 
	 * @param contract
	 * @param automaticRoles
	 */
	protected void assignAutomaticRoles(IdmIdentityContract contract, Set<IdmRoleTreeNode> automaticRoles) {
		Assert.notNull(automaticRoles);
		//
		if (automaticRoles.isEmpty()) {
			return;
		}
		automaticRoles.forEach(roleTreeNode -> {
			IdmIdentityRole identityRole = new IdmIdentityRole(contract);
			identityRole.setRole(roleTreeNode.getRole());
			identityRole.setRoleTreeNode(roleTreeNode);
			identityRoleService.save(identityRole);
		});
		
//		// prepare request
//		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
//		roleRequest.setApplicant(contract.getIdentity().getId());
//		roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
//		roleRequest.setExecuteImmediately(true); // TODO: by configuration
//		roleRequest = roleRequestService.save(roleRequest);
//		//
//		for(IdmRoleTreeNode roleTreeNode : automaticRoles) {
//			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
//			conceptRoleRequest.setRoleRequest(roleRequest.getId());
//			conceptRoleRequest.setIdentityContract(contract.getId());
//			conceptRoleRequest.setValidFrom(contract.getValidFrom());
//			conceptRoleRequest.setValidTill(contract.getValidTill());
//			conceptRoleRequest.setRole(roleTreeNode.getRole().getId());
//			conceptRoleRequest.setRoleTreeNode(roleTreeNode.getId());
//			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
//			conceptRoleRequestService.save(conceptRoleRequest);
//		};
//		//
//		roleRequestService.startRequestInternal(roleRequest.getId(), false);
	}
	
	/**
	 * before save - wee need to check changes
	 */
	@Override
	public int getOrder() {
		return super.getOrder() + 1;
	}

}
