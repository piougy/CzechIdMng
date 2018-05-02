package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractSliceProcessor;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
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
	@Autowired
	private ContractSliceManager contractSliceManager;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmContractSliceGuaranteeService contractGuaranteeService;

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
		IdmContractSliceDto slice = event.getContent();

		// delete contract slice guarantees
		IdmContractSliceGuaranteeFilter filter = new IdmContractSliceGuaranteeFilter();
		filter.setContractSliceId(slice.getId());
		contractGuaranteeService.find(filter, null).forEach(guarantee -> {
			contractGuaranteeService.delete(guarantee);
		});

		UUID contractId = slice.getParentContract();
		if (contractId != null) {
			List<IdmContractSliceDto> slices = contractSliceManager.findAllSlices(contractId);
			if (slices.size() == 1) {
				// This slice is last for parent contract. We will also deleted the parent
				// contract;
				contractService.deleteById(contractId);
				// Internal delete of slice
				service.deleteInternal(slice);
				return new DefaultEventResult<>(event, this);
			}

			// Find next slice
			IdmContractSliceDto nextSlice = contractSliceManager.findNextSlice(slice, slices);

			// Internal delete of slice
			service.deleteInternal(slice);

			// If exists next slice, then update valid till on previous slice
			if (nextSlice != null) {
				contractSliceManager.updateValidTillOnPreviousSlice(nextSlice,
						contractSliceManager.findAllSlices(contractId));
			}

			IdmContractSliceDto validSlice = contractSliceManager.findValidSlice(contractId);
			if (validSlice != null) {
				// Set next slice as is currently using in contract
				contractSliceManager.setSliceAsCurrentlyUsing(validSlice);
			}
		} else {
			service.deleteInternal(slice);
		}

		//
		return new DefaultEventResult<>(event, this);
	}

}
