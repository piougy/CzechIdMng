package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.model.event.ContractSliceGuaranteeEvent.ContractSliceGuaranteeEventType;

/**
 * Processor delete {@link IdmContractSliceGuaranteeDto}
 * 
 * @author svandav
 *
 */

@Component
@Description("Delete manually added gurantee for contract.")
public class ContractSliceGuaranteeDeleteProcessor extends CoreEventProcessor<IdmContractSliceGuaranteeDto> {

	public static final String PROCESSOR_NAME = "contract-slice-guarantee-delete";
	
	private final IdmContractSliceGuaranteeService contractSliceGuaranteeService;
	
	@Autowired
	public ContractSliceGuaranteeDeleteProcessor(IdmContractSliceGuaranteeService contractSliceGuaranteeService) {
		super(ContractSliceGuaranteeEventType.DELETE);
		//
		Assert.notNull(contractSliceGuaranteeService);
		//
		this.contractSliceGuaranteeService = contractSliceGuaranteeService;
	}

	@Override
	public EventResult<IdmContractSliceGuaranteeDto> process(EntityEvent<IdmContractSliceGuaranteeDto> event) {
		IdmContractSliceGuaranteeDto dto = event.getContent();
		//
		contractSliceGuaranteeService.deleteInternal(dto);
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
