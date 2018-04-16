package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractSliceProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Deletes contract slice - ensures referential integrity.
 * 
 * @author svandav
 *
 */
@Component
@Description("Deletes contract slice.")
public class ContractSliceDeleteProcessor extends CoreEventProcessor<IdmContractSliceDto>
		implements ContractSliceProcessor {

	public static final String PROCESSOR_NAME = "contract-slice-delete-processor";
	private final IdmContractSliceService service;
	// private final IdmContractGuaranteeService contractGuaranteeService;

	@Autowired
	public ContractSliceDeleteProcessor(IdmContractSliceService service) {
		super(IdentityContractEventType.DELETE);
		//
		Assert.notNull(service);
		//
		this.service = service;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmContractSliceDto> process(EntityEvent<IdmContractSliceDto> event) {
		IdmContractSliceDto contract = event.getContent();

		// delete contract guarantees
		// IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		// filter.setIdentityContractId(contract.getId());
		// contractGuaranteeService.find(filter, null).forEach(guarantee -> {
		// contractGuaranteeService.delete(guarantee);
		// });
		// delete identity contract
		service.deleteInternal(contract);
		//
		return new DefaultEventResult<>(event, this);
	}
}
