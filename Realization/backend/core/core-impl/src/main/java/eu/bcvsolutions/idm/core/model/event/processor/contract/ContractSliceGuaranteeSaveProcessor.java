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
 * Processor save {@link IdmContractSliceGuaranteeDto}
 * 
 * @author svandav
 *
 */

@Component
@Description("Save manually added gurantee for contract slice.")
public class ContractSliceGuaranteeSaveProcessor extends CoreEventProcessor<IdmContractSliceGuaranteeDto> {

	public static final String PROCESSOR_NAME = "contract-slice-guarantee-save";
	
	private final IdmContractSliceGuaranteeService contractSliceGuaranteeService;
	
	@Autowired
	public ContractSliceGuaranteeSaveProcessor(IdmContractSliceGuaranteeService contractSliceGuaranteeService) {
		super(ContractSliceGuaranteeEventType.CREATE, ContractSliceGuaranteeEventType.UPDATE);
		//
		Assert.notNull(contractSliceGuaranteeService);
		//
		this.contractSliceGuaranteeService = contractSliceGuaranteeService;
	}

	@Override
	public EventResult<IdmContractSliceGuaranteeDto> process(EntityEvent<IdmContractSliceGuaranteeDto> event) {
		IdmContractSliceGuaranteeDto dto = event.getContent();
		//
		dto = contractSliceGuaranteeService.saveInternal(dto);
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
