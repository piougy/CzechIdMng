package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractPositionProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.model.event.ContractPositionEvent.ContractPositionEventType;

/**
 * Processor save {@link IdmContractPositionDto}
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
@Component(ContractPositionSaveProcessor.PROCESSOR_NAME)
@Description("Save contract other position.")
public class ContractPositionSaveProcessor 
		extends CoreEventProcessor<IdmContractPositionDto>
		implements ContractPositionProcessor {

	public static final String PROCESSOR_NAME = "core-contract-position-save-processor";
	
	private final IdmContractPositionService contractPositionService;
	
	@Autowired
	public ContractPositionSaveProcessor(IdmContractPositionService contractPositionService) {
		super(ContractPositionEventType.CREATE, ContractPositionEventType.UPDATE);
		//
		Assert.notNull(contractPositionService, "Service is required.");
		//
		this.contractPositionService = contractPositionService;
	}

	@Override
	public EventResult<IdmContractPositionDto> process(EntityEvent<IdmContractPositionDto> event) {
		IdmContractPositionDto dto = event.getContent();
		//
		dto = contractPositionService.saveInternal(dto);
		//
		event.setContent(dto);
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
