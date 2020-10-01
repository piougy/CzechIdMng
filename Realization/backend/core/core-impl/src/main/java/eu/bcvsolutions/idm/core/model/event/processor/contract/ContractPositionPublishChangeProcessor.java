package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.api.event.processor.ContractPositionProcessor;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition_;
import eu.bcvsolutions.idm.core.model.event.ContractPositionEvent.ContractPositionEventType;

/**
 * Publish contract position change event
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
@Component(ContractPositionPublishChangeProcessor.PROCESSOR_NAME)
@Description("Publish contract other position change event.")
public class ContractPositionPublishChangeProcessor 
		extends AbstractPublishEntityChangeProcessor<IdmContractPositionDto>
		implements ContractPositionProcessor {

	public static final String PROCESSOR_NAME = "core-contract-position-publish-change-processor";
	//
	@Autowired private LookupService lookupService;
	
	public ContractPositionPublishChangeProcessor() {
		super(ContractPositionEventType.CREATE, ContractPositionEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	protected EntityEvent<IdmContractPositionDto> setAdditionalEventProperties(EntityEvent<IdmContractPositionDto> event) {
		event = super.setAdditionalEventProperties(event);
		// we need to set super entity owner - contract position should not be processed concurrently for given identity
		IdmIdentityContractDto contract = lookupService.lookupEmbeddedDto(event.getContent(), IdmContractPosition_.identityContract);
		event.setSuperOwnerId(contract.getIdentity());
		//
		return event;
	}
}
