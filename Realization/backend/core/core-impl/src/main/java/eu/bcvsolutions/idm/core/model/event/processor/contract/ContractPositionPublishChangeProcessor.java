package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.api.event.processor.ContractPositionProcessor;
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
	
	public ContractPositionPublishChangeProcessor() {
		super(ContractPositionEventType.CREATE, ContractPositionEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
