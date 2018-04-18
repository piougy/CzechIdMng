package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractSliceProcessor;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent.ContractSliceEventType;

/**
 * Persists contract slice.
 * 
 * @author svandav
 *
 */
@Component
@Description("Persists contract slice.")
public class ContractSliceSaveProcessor extends CoreEventProcessor<IdmContractSliceDto>
		implements ContractSliceProcessor {

	public static final String PROCESSOR_NAME = "contract-slice-save-processor";
	//
	@Autowired
	private IdmContractSliceService service;
	@Autowired
	private ContractSliceManager sliceManager;

	public ContractSliceSaveProcessor() {
		super(ContractSliceEventType.UPDATE, ContractSliceEventType.CREATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmContractSliceDto> process(EntityEvent<IdmContractSliceDto> event) {
		IdmContractSliceDto slice = event.getContent();
		IdmContractSliceDto originalSlice = event.getOriginalSource();

		if (slice != null && slice.getIdentity() != null) {
			// TODO: Update of slice / contract / EAV

			// Check if was contractCode changed, if yes, then set parentContract to
			// null (will be recalculated)
			if (originalSlice != null && originalSlice.getContractCode() != slice.getContractCode()) {
				slice.setParentContract(null);
			}
			if (originalSlice != null && originalSlice.getParentContract() != slice.getParentContract()) {
				slice.setParentContract(null);
			}

			UUID parentContract = slice.getParentContract();
			if (parentContract == null) {
				createContract(slice);
			} else {
				updateContract(slice);
			}
		}
		slice = service.saveInternal(slice);
		event.setContent(slice);

		return new DefaultEventResult<>(event, this);
	}

	private void updateContract(IdmContractSliceDto slice) {
		Assert.notNull(slice.getId());
		Assert.notNull(slice.getParentContract());
		
		// Find other slices
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setParentContract(slice.getId());
		sliceFilter.setIdentity(slice.getIdentity());
		List<IdmContractSliceDto> slices = service.find(sliceFilter, null).getContent();
		
		IdmIdentityContractDto contract = sliceManager.updateContractBySlice(slice, slices);

	}

	/**
	 * Create contract from the slice or create relation on the exists contract
	 * 
	 * @param slice
	 */
	private void createContract(IdmContractSliceDto slice) {
		String contractCode = slice.getContractCode();
		if (Strings.isNullOrEmpty(contractCode)) {
			// Create new parent contract
			IdmIdentityContractDto contract = sliceManager.createContractBySlice(slice, ImmutableList.of(slice));
			slice.setParentContract(contract.getId());
		} else {
			// Find other slices
			IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
			sliceFilter.setContractCode(contractCode);
			sliceFilter.setIdentity(slice.getIdentity());
			List<IdmContractSliceDto> slices = service.find(sliceFilter, null).getContent();
			// Find contract sets on others slices
			UUID parentContractId = slices.stream().filter(s -> s.getParentContract() != null)//
					.findFirst()//
					.map(IdmContractSliceDto::getParentContract)//
					.orElse(null);//
			if (parentContractId == null) {
				// Other slices does not have sets contract
				// Create new parent contract
				IdmIdentityContractDto contract = sliceManager.createContractBySlice(slice, slices);
				slice.setParentContract(contract.getId());
			} else {
				slice.setParentContract(parentContractId);
			}
		}
	}

}
