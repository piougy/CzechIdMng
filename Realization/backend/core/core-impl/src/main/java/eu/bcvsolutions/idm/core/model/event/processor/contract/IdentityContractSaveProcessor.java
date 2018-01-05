package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Persists identity contract.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists identity contract.")
public class IdentityContractSaveProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {
	
	public static final String PROCESSOR_NAME = "identity-contract-save-processor";
	//
	@Autowired private IdmIdentityContractService service;
	@Autowired  private IdmIdentityService identityService;
	
	public IdentityContractSaveProcessor() {
		super(IdentityContractEventType.UPDATE, IdentityContractEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		contract = service.saveInternal(contract);
		event.setContent(contract);
		//
		// check identity state
		IdmIdentityContractDto previousContract = event.getOriginalSource();
		IdmIdentityDto identity = DtoUtils.getEmbedded(contract, IdmIdentityContract_.identity.getName(), IdmIdentityDto.class);
		if ((identity.getState() == IdentityState.CREATED || identity.isDisabled()) 
				&& contractChanged(previousContract, contract)) {
			// synchronize identity states, which has no effect on HR processes
			identity = identityService.get(contract.getIdentity());
			IdentityState newState = identityService.evaluateState(identity.getId());
			if (newState.isDisabled() && identity.getState() != newState) {
				identity.setState(newState);
				identity = identityService.save(identity);
			}					
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	private boolean contractChanged(IdmIdentityContractDto previousContract, IdmIdentityContractDto newContract) {
		if (previousContract == null) {
			if (!newContract.isValid()) {
				return true;
			}
			return false; // valid contract will be processed by HR processes 
		}
		if (previousContract.isValid() && newContract.isValid()) {
			return false;
		}
		// both contracts are invalid - i have to check contract validity
		if (previousContract.isValidNowOrInFuture() && !newContract.isValidNowOrInFuture()) {
			return true;
		}
		//
		if (!previousContract.isValidNowOrInFuture() && newContract.isValidNowOrInFuture()) {
			return true;
		}
		// both contracts are invalid
		return false;
	}

}
