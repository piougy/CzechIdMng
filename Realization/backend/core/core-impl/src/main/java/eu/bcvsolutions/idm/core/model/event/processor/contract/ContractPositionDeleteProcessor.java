package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractPositionProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition_;
import eu.bcvsolutions.idm.core.model.event.ContractPositionEvent.ContractPositionEventType;

/**
 * Processor delete {@link IdmContractPositionDto}
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
@Component(ContractPositionDeleteProcessor.PROCESSOR_NAME)
@Description("Delete contract other position.")
public class ContractPositionDeleteProcessor 
		extends CoreEventProcessor<IdmContractPositionDto>
		implements ContractPositionProcessor {

	public static final String PROCESSOR_NAME = "core-contract-position-delete-processor";
	
	@Autowired private IdmContractPositionService contractPositionService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleRequestService roleRequestService;
	
	public ContractPositionDeleteProcessor() {
		super(ContractPositionEventType.DELETE);
	}

	@Override
	public EventResult<IdmContractPositionDto> process(EntityEvent<IdmContractPositionDto> event) {
		IdmContractPositionDto dto = event.getContent();
		Assert.notNull(dto.getId(), "Posistion id is required!");
		IdmIdentityContractDto contract = DtoUtils.getEmbedded(dto, IdmContractPosition_.identityContract);
		//
		// delete identity roles assigned by deleted position
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<>();
		identityRoleService
			.findAllByContractPosition(dto.getId())
			.forEach(identityRole -> {
				if (identityRole.getDirectRole() == null) { // sub roles are removed different way (processor on direct identity role)
					IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
					conceptRoleRequest.setIdentityRole(identityRole.getId());
					conceptRoleRequest.setRole(identityRole.getRole());
					conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
					conceptRoleRequest.setIdentityContract(dto.getIdentityContract());
					conceptRoleRequest.setContractPosition(dto.getId());
					//
					concepts.add(conceptRoleRequest);
				}
			});
		roleRequestService.executeConceptsImmediate(contract.getIdentity(), concepts);
		//
		contractPositionService.deleteInternal(dto);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}
