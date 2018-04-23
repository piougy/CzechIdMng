package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent.ContractSliceEventType;

/**
 * Update contract by slice
 * 
 * @author svandav
 *
 */
@Component
@Description("Update contract by slice.")
public class ContractSliceSaveContractProcessor extends CoreEventProcessor<IdmContractSliceDto>
		implements ContractSliceProcessor {

	public static final String PROCESSOR_NAME = "contract-slice-save-contract-processor";
	//
	@Autowired
	private IdmContractSliceService service;
	@Autowired
	private ContractSliceManager sliceManager;
	@Autowired
	private IdmIdentityContractService contractService;

	public ContractSliceSaveContractProcessor() {
		super(ContractSliceEventType.UPDATE, ContractSliceEventType.CREATE, ContractSliceEventType.EAV_SAVE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmContractSliceDto> process(EntityEvent<IdmContractSliceDto> event) {
		IdmContractSliceDto slice = event.getContent();
		IdmContractSliceDto originalSlice = event.getOriginalSource();

		if (slice.getIdentity() != null) {
			UUID parentContract = slice.getParentContract();
			// TODO: Update of contract EAV

			// Check if was contractCode changed, if yes, then set parentContract to
			// null (will be recalculated)
			if (originalSlice != null && !Objects.equal(originalSlice.getContractCode(), slice.getContractCode())) {
				slice.setParentContract(null);
			}
			if (originalSlice != null && !Objects.equal(originalSlice.getParentContract(), slice.getParentContract())) {
				slice.setParentContract(null);
			}

			if (parentContract == null) {
				slice = linkOrCreateContract(slice);
			} else {
				slice = updateContract(slice);
			}
		}

		boolean recalculateUsingAsContract = false;
		UUID parentContract = slice.getParentContract();

		if (originalSlice == null) {
			// Slice is new, we want to recalculate "Is using as contract" field.
			recalculateUsingAsContract = true;
		}

		// Recalculate on change of 'parentContract' field
		if (originalSlice != null && !Objects.equal(originalSlice.getParentContract(), slice.getParentContract())) {
			UUID originalParentContract = originalSlice.getParentContract();
			// Parent contract was changed ... we need recalculate parent contract for
			// original slice
			if (originalParentContract != null) {
				IdmIdentityContractDto contract = contractService.get(originalParentContract);
				// Find other slices for original contract
				IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
				sliceFilter.setParentContract(originalParentContract);
				List<IdmContractSliceDto> slices = service.find(sliceFilter, null).getContent();
				if (!slices.isEmpty()) {
					IdmContractSliceDto currentSlice = slices.stream().filter(s -> s.isUsingAsContract()).findFirst()
							.orElse(null);
					// Update original contract
					sliceManager.updateContractBySlice(contract, currentSlice != null ? currentSlice : slices.get(0),
							slices);
				}
			}
			// Parent contract was changed, want to recalculate "Is using as contract"
			// field.
			recalculateUsingAsContract = true;
		}

		// Recalculate on change of 'validFrom' field
		if (originalSlice != null && !Objects.equal(originalSlice.getValidFrom(), slice.getValidFrom())) {
			// Valid from was changed ... we have to change of validity till on previous
			// slice
			if (parentContract != null) {
				// Find other slices for parent contract
				List<IdmContractSliceDto> slices = findAllSlices(parentContract);
				if (!slices.isEmpty()) {
					// Update validity till on previous slice
					sliceManager.updateValidTillOnPreviousSlice(slice, slices);
					IdmContractSliceDto nextSlice = sliceManager.findNextSlice(slice, slices);
					if (nextSlice != null) {
						LocalDate validTill = nextSlice.getValidFrom().minusDays(1);
						// Save only if valid till is changed
						if(slice.getValidTill() == null || !validTill.isEqual(slice.getValidTill())) {
							slice.setValidTill(validTill);
							// Save with skip this processor
							service.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, slice,
									ImmutableMap.of(IdmContractSliceService.SKIP_CREATE_OR_UPDATE_PARENT_CONTRACT, true)));
						}
					}
				}
			}
			// Validity from was changed, want to recalculate "Is using as contract" field.
			recalculateUsingAsContract = true;
		}

		if (recalculateUsingAsContract) {
			IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
			sliceFilter.setParentContract(parentContract);
			sliceFilter.setUsingAsContract(Boolean.TRUE);

			List<IdmContractSliceDto> slicesMarkedAsUsing = service.find(sliceFilter, null).getContent();
			IdmContractSliceDto shouldBeSetAsUsing = sliceManager.findValidSlice(parentContract);

			// If is correct slice marked as "Is using as contract", then recalculate is not
			// called
			if (slicesMarkedAsUsing.isEmpty() || !slicesMarkedAsUsing.get(0).equals(shouldBeSetAsUsing)) {
				if (shouldBeSetAsUsing != null) {
					sliceManager.setSliceAsCurrentlyUsing(shouldBeSetAsUsing);
				}
			}
		}

		if (originalSlice == null) {
			// Slice is new, we want to set valid till by next slice
			slice = service.get(slice.getId());
			IdmContractSliceDto nextSlice = sliceManager.findNextSlice(slice, findAllSlices(parentContract));
			if (nextSlice != null) {
				slice.setValidTill(nextSlice.getValidFrom().minusDays(1));
				// Save with skip this processor
				service.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, slice,
						ImmutableMap.of(IdmContractSliceService.SKIP_CREATE_OR_UPDATE_PARENT_CONTRACT, true)));
			}
		}
		event.setContent(service.get(slice.getId()));
		return new DefaultEventResult<>(event, this);
	}

	/**
	 * @param parentContract
	 * @return
	 */
	private List<IdmContractSliceDto> findAllSlices(UUID parentContract) {
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setParentContract(parentContract);
		List<IdmContractSliceDto> slices = service.find(sliceFilter, null).getContent();
		return slices;
	}

	private IdmContractSliceDto updateContract(IdmContractSliceDto slice) {
		Assert.notNull(slice.getId());
		Assert.notNull(slice.getParentContract());

		// Find other slices
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setParentContract(slice.getParentContract());
		sliceFilter.setIdentity(slice.getIdentity());
		List<IdmContractSliceDto> slices = service.find(sliceFilter, null).getContent();

		IdmIdentityContractDto contract = contractService.get(slice.getParentContract());
		// Update contract by that slice
		sliceManager.updateContractBySlice(contract, slice, slices);
		slice.setParentContract(contract.getId());
		return service.saveInternal(slice);
	}

	/**
	 * Create or link contract from the slice or create relation on the exists
	 * contract
	 * 
	 * @param slice
	 * @return
	 */
	private IdmContractSliceDto linkOrCreateContract(IdmContractSliceDto slice) {

		String contractCode = slice.getContractCode();
		if (Strings.isNullOrEmpty(contractCode)) {
			// Create new parent contract
			// When new contract is created, then this slice have to be sets as "Is using as
			// contract"
			slice.setUsingAsContract(true);
			IdmIdentityContractDto contract = sliceManager.createContractBySlice(slice, ImmutableList.of(slice));
			slice.setParentContract(contract.getId());

			return service.saveInternal(slice);
		} else {
			// Find other slices
			IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
			sliceFilter.setContractCode(contractCode);
			sliceFilter.setIdentity(slice.getIdentity());
			List<IdmContractSliceDto> slices = service.find(sliceFilter, null).getContent();
			// Find contract sets on others slices
			UUID parentContractId = slices.stream()
					.filter(s -> s.getParentContract() != null && !s.getId().equals(slice.getId()))//
					.findFirst()//
					.map(IdmContractSliceDto::getParentContract)//
					.orElse(null);//
			if (parentContractId == null) {
				// Other slices does not have sets contract
				// Create new parent contract
				// When new contract is created, then this slice have to be sets as "Is using as
				// contract"
				slice.setUsingAsContract(true);
				IdmIdentityContractDto contract = sliceManager.createContractBySlice(slice, slices);
				slice.setParentContract(contract.getId());

				return service.saveInternal(slice);
			} else {
				// We found and link on existed contract. We have to do update this contract by
				// slice.
				slice.setParentContract(parentContractId);
				IdmContractSliceDto sliceSaved = service.saveInternal(slice);

				return this.updateContract(sliceSaved);
			}
		}
	}

	@Override
	public boolean conditional(EntityEvent<IdmContractSliceDto> event) {
		return !this.getBooleanProperty(IdmContractSliceService.SKIP_CREATE_OR_UPDATE_PARENT_CONTRACT,
				event.getProperties());
	}

	@Override
	public int getOrder() {
		// Execute after save
		return super.getOrder() + 1;
	}

}
