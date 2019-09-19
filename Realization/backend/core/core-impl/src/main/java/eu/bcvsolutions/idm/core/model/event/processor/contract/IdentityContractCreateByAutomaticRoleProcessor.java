package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Automatic roles by tree structure recount while enabled identity contract is created.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Automatic roles by tree structure recount while enabled identity contract is created.")
public class IdentityContractCreateByAutomaticRoleProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {
	
	public static final String PROCESSOR_NAME = "identity-contract-create-by-automatic-role-processor";
	//
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmRoleRequestService roleRequestService;
	
	public IdentityContractCreateByAutomaticRoleProcessor() {
		super(IdentityContractEventType.NOTIFY);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmIdentityContractDto> event) {
		return super.conditional(event) 
				&& IdentityContractEventType.CREATE.name().equals(event.getParentType())
				&& event.getContent().isValidNowOrInFuture(); // invalid contracts cannot have roles (roles for disabled contracts are removed by different process)
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		//
		if (contract.getWorkPosition() != null) {
			Set<IdmRoleTreeNodeDto> automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(contract.getWorkPosition());
			if (!automaticRoles.isEmpty()) {
				List<IdmConceptRoleRequestDto> concepts = new ArrayList<>(automaticRoles.size());
				for (AbstractIdmAutomaticRoleDto autoRole : automaticRoles) {
					IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
					conceptRoleRequest.setIdentityContract(contract.getId());
					conceptRoleRequest.setValidFrom(contract.getValidFrom());
					conceptRoleRequest.setValidTill(contract.getValidTill());
					conceptRoleRequest.setRole(autoRole.getRole());
					conceptRoleRequest.setAutomaticRole(autoRole.getId());
					conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
					//
					concepts.add(conceptRoleRequest);
				}
				//
				roleRequestService.executeConceptsImmediate(contract.getIdentity(), concepts);
			}
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 500;
	}

}
