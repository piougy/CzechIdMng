package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.AutomaticRoleManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Automatic roles by tree structure recount while enabled identity contract is created.
 * 
 * @author Radek Tomiška
 *
 */
@Component(IdentityContractCreateByAutomaticRoleProcessor.PROCESSOR_NAME)
@Description("Automatic roles by tree structure recount while enabled identity contract is created.")
public class IdentityContractCreateByAutomaticRoleProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {
	
	public static final String PROCESSOR_NAME = "identity-contract-create-by-automatic-role-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractCreateByAutomaticRoleProcessor.class);
	//
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private EntityStateManager entityStateManager;
	
	public IdentityContractCreateByAutomaticRoleProcessor() {
		super(IdentityContractEventType.NOTIFY);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		//
		return super.conditional(event) 
				&& IdentityContractEventType.CREATE.name().equals(event.getParentType())
				&& contract.isValidNowOrInFuture()  // invalid contracts cannot have roles (roles for disabled contracts are removed by different process)
				&& contract.getWorkPosition() != null; // automatic role is configured to work position
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		// when automatic role recalculation is skipped, then flag for contract position is created only
		// flag can be processed afterwards
		if (getBooleanProperty(AutomaticRoleManager.SKIP_RECALCULATION, event.getProperties())) {
			LOG.debug("Automatic roles are skipped for contract [{}], state [AUTOMATIC_ROLE_SKIPPED] for position will be created only.",
					contract.getId());
			// 
			entityStateManager.createState(contract, OperationState.BLOCKED, CoreResultCode.AUTOMATIC_ROLE_SKIPPED, null);
			//
			return new DefaultEventResult<>(event, this);
		}
		// get related automatic roles
		Set<IdmRoleTreeNodeDto> automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(contract.getWorkPosition());
		if (automaticRoles.isEmpty()) {
			return new DefaultEventResult<>(event, this);
		}
		// assign automatic roles by role request
		List<IdmConceptRoleRequestDto> concepts = automaticRoles
				.stream()
				.map(autoRole -> {
					IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
					conceptRoleRequest.setIdentityContract(contract.getId());
					conceptRoleRequest.setValidFrom(contract.getValidFrom());
					conceptRoleRequest.setValidTill(contract.getValidTill());
					conceptRoleRequest.setRole(autoRole.getRole());
					conceptRoleRequest.setAutomaticRole(autoRole.getId());
					conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
					//
					return conceptRoleRequest;
				})
				.collect(Collectors.toList());
		roleRequestService.executeConceptsImmediate(contract.getIdentity(), concepts);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 500;
	}

}
