package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.event.ContractSliceGuaranteeEvent.ContractSliceGuaranteeEventType;

/**
 * Processor recount/sync guarantees on contract
 * 
 * @author svandav
 *
 */

@Component
@Description("Save/delete guarantee for contract slice. Recount/sync guarantees on contract")
public class ContractSliceGuaranteeRecountProcessor extends CoreEventProcessor<IdmContractSliceGuaranteeDto> {

	public static final String PROCESSOR_NAME = "contract-slice-guarantee-recount";
	
	@Autowired
	private ContractSliceManager contractSliceManager;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmContractSliceService contractSliceService;
	
	public ContractSliceGuaranteeRecountProcessor() {
		super(ContractSliceGuaranteeEventType.CREATE, ContractSliceGuaranteeEventType.UPDATE, ContractSliceGuaranteeEventType.DELETE);
	}

	@Override
	public EventResult<IdmContractSliceGuaranteeDto> process(EntityEvent<IdmContractSliceGuaranteeDto> event) {
		IdmContractSliceGuaranteeDto dto = event.getContent();
		Assert.notNull(dto.getContractSlice());
		IdmContractSliceDto slice = contractSliceService.get(dto.getContractSlice());
		UUID parentContract = slice.getParentContract();
		if(parentContract != null) {
			IdmIdentityContractDto contract = contractService.get(parentContract);
			// Copy guarantees to contract
			if (slice.isUsingAsContract()) {
				contractSliceManager.copyGuarantees(slice, contract);
			}
		}
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
	
	@Override
	public int getOrder() {
		// Evolve after save
		return super.getOrder()+10;
	}
}
