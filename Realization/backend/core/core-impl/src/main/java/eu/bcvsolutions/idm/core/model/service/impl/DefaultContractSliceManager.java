package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.ContractSliceConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent.ContractSliceEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Manager for contract slices
 * 
 * @author svandav
 *
 */
@Service("contractSliceManager")
public class DefaultContractSliceManager implements ContractSliceManager {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultContractSliceManager.class);

	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmContractSliceService contractSliceService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmContractSliceGuaranteeService contractSliceGuaranteeService;
	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired
	private ContractSliceConfiguration contractSliceConfiguration;

	@Override
	@Transactional
	public IdmIdentityContractDto updateContractBySlice(IdmIdentityContractDto contract, IdmContractSliceDto slice,
			Map<String, Serializable> eventProperties) {

		Assert.notNull(contract);
		Assert.notNull(slice, "Contract slice cannot be null!");
		Assert.notNull(slice.getIdentity());
		Assert.isTrue(slice.isUsingAsContract());

		boolean isNew = contractService.isNew(contract);

		// Update all slice attributes
		convertSliceToContract(slice, contract);

		// Check if is protection interval activated and whether we need resolve him
		int protectionInterval = contractSliceConfiguration.getProtectionInterval();
		if (!isNew && protectionInterval > 0 && slice.getContractValidTill() != null) {
			resolveProtectionInterval(slice, contract, protectionInterval);
		}

		// Save contract
		IdmIdentityContractDto savedContract = contractService.publish(
				new IdentityContractEvent(isNew ? IdentityContractEventType.CREATE : IdentityContractEventType.UPDATE,
						contract, ImmutableMap.copyOf(eventProperties)))
				.getContent();
		// Copy values of extended attributes
		copyExtendedAttributes(slice, savedContract);

		// Copy guarantees
		copyGuarantees(slice, savedContract);

		return savedContract;
	}

	/**
	 * Protection mode is activate. Check if the next slice has valid from of
	 * contract lover then contract valid till (plus protection interval) on given
	 * slice, then contract will does not terminated (his valid till will be sets by
	 * valid till form next slice)
	 * 
	 * @param slice
	 * @param contract
	 * @param protectionInterval
	 */
	private void resolveProtectionInterval(IdmContractSliceDto slice, IdmIdentityContractDto contract,
			int protectionInterval) {
		Assert.notNull(contract);
		Assert.notNull(contract.getId());
		
		List<IdmContractSliceDto> slices = this.findAllSlices(contract.getId());
		IdmContractSliceDto nextSlice = this.findNextSlice(slice, slices);
		if (nextSlice == null) {
			// None next slice exists ... contract was not changed
			return;
		}

		if (nextSlice.getContractValidFrom() == null) {
			LOG.info(MessageFormat.format(
					"Update contract by slice [{0}] - Resloving of the protection interval - next slice has contract valid from sets to 'null' ... contract [{1}] was changed!",
					slice, contract));
			contract.setValidTill(null);
			return;

		}
		long diffInDays = ChronoUnit.DAYS //
				.between( //
						java.time.LocalDate.parse(slice.getContractValidTill().toString()), //
						java.time.LocalDate.parse(nextSlice.getContractValidFrom().toString()));

		if (diffInDays <= protectionInterval) {
			LOG.info(MessageFormat.format(
					"Update contract by slice [{0}] - Resloving of the protection interval - next slice has contract valid from sets to [{2}] ... contract [{1}] was changed!",
					slice, contract, nextSlice.getContractValidFrom()));

			contract.setValidTill(nextSlice.getContractValidFrom());
		}
	}

	@Override
	@Transactional
	public void updateValidTillOnPreviousSlice(IdmContractSliceDto slice, List<IdmContractSliceDto> slices) {
		Assert.notNull(slice, "Contract slice cannot be null!");
		Assert.notNull(slices);
		if (slice.getValidFrom() == null) {
			return;
		}

		IdmContractSliceDto previousSlice = this.findPreviousSlice(slice, slices);
		if (previousSlice == null) {
			return;
		}
		// Previous slice will be valid till starts of validity next slice
		previousSlice.setValidTill(slice.getValidFrom().minusDays(1));
		contractSliceService.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, previousSlice,
				ImmutableMap.of(IdmContractSliceService.SKIP_RECALCULATE_CONTRACT_SLICE, Boolean.TRUE)));
	}

	@Override
	@Transactional
	public IdmContractSliceDto findNextSlice(IdmContractSliceDto slice, List<IdmContractSliceDto> slices) {
		Assert.notNull(slice, "Contract slice cannot be null!");
		Assert.notNull(slices);
		Comparator<IdmContractSliceDto> comparatorValidFrom = Comparator.comparing(IdmContractSliceDto::getValidFrom);
		if (slice.getValidFrom() == null) {
			return slices.stream() //
					.filter(s -> !s.equals(slice) && s.getValidFrom() != null) //
					.min(comparatorValidFrom) //
					.orElse(null); //
		}

		return slices.stream() //
				.filter(s -> !s.equals(slice) && s.getValidFrom() != null
						&& s.getValidFrom().isAfter(slice.getValidFrom())) //
				.min(comparatorValidFrom) //
				.orElse(null); //
	}

	@Override
	@Transactional
	public IdmContractSliceDto findPreviousSlice(IdmContractSliceDto slice, List<IdmContractSliceDto> slices) {
		Assert.notNull(slice, "Contract slice cannot be null!");
		Assert.notNull(slices);
		if (slice.getValidFrom() == null) {
			return null;
		}

		Comparator<IdmContractSliceDto> comparatorValidFrom = Comparator.comparing(IdmContractSliceDto::getValidFrom);
		return slices.stream() //
				.filter(s -> !s.equals(slice) && s.getValidFrom() != null
						&& s.getValidFrom().isBefore(slice.getValidFrom())) //
				.max(comparatorValidFrom) //
				.orElse(null); //
	}

	@Override
	@Transactional
	public Page<IdmContractSliceDto> findUnvalidSlices(Pageable page) {
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setShouldBeUsingAsContract(Boolean.TRUE);
		sliceFilter.setUsingAsContract(Boolean.FALSE);

		return contractSliceService.find(sliceFilter, page);
	}

	@Override
	@Transactional
	public IdmContractSliceDto setSliceAsCurrentlyUsing(IdmContractSliceDto slice) {
		// Only one slice can be marked as 'is using as contract' (for one parent
		// contract)
		if (slice.getParentContract() != null) {
			// Find other slices with this contract and marked "is using as contract"
			// (usually should be returned only one)
			IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
			sliceFilter.setParentContract(slice.getParentContract());
			sliceFilter.setUsingAsContract(Boolean.TRUE);
			List<IdmContractSliceDto> otherSlices = contractSliceService.find(sliceFilter, null).getContent();

			// To all this slices (exclude itself) set "using as contract" on false
			otherSlices.stream() //
					.filter(s -> !s.equals(slice)) //
					.forEach(s -> { //
						s.setUsingAsContract(false);
						// We want only save data, not update contract by slice
						contractSliceService
								.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, s, ImmutableMap
										.of(IdmContractSliceService.SKIP_RECALCULATE_CONTRACT_SLICE, Boolean.TRUE)));
					});
		}
		slice.setUsingAsContract(true);
		// Copy to contract is ensures the save slice processor. We have to only set
		// attribute 'Is using as contract' to true.
		return contractSliceService.save(slice);
	}

	@Override
	@Transactional
	public IdmContractSliceDto findValidSlice(UUID contractId) {
		Assert.notNull(contractId, "Contract is mandatory!");

		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setShouldBeUsingAsContract(Boolean.TRUE);
		sliceFilter.setParentContract(contractId);

		// First try found valid slice
		List<IdmContractSliceDto> validSlices = contractSliceService.find(sliceFilter, null).getContent();
		if (!validSlices.isEmpty()) {
			return validSlices.get(0);
		}

		// None valid slice exists, now we found all slices
		sliceFilter.setShouldBeUsingAsContract(null);
		List<IdmContractSliceDto> slices = contractSliceService.find(sliceFilter, null).getContent();
		// We does not have any slices for this contract
		if (slices.isEmpty()) {
			return null;
		}

		LocalDate now = LocalDate.now();
		// Try to find the nearest slice in future
		IdmContractSliceDto resultSlice = slices.stream().filter(slice -> now.isBefore(slice.getValidFrom()))
				.min(Comparator.comparing(IdmContractSliceDto::getValidFrom)).orElse(null);
		if (resultSlice != null) {
			return resultSlice;
		}
		return null;
	}

	@Override
	@Transactional
	public List<IdmContractSliceDto> findAllSlices(UUID parentContract) {
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setParentContract(parentContract);
		List<IdmContractSliceDto> slices = contractSliceService.find(sliceFilter, null).getContent();
		return slices;
	}

	/**
	 * Convert slice to the contract (does not save changes)
	 * 
	 * @param slice
	 * @param contract
	 * @param validFrom
	 *            of whole contract
	 * @param validTill
	 *            of whole contract
	 */
	private void convertSliceToContract(IdmContractSliceDto slice, IdmIdentityContractDto contract) {
		contract.setIdentity(slice.getIdentity());
		contract.setMain(slice.isMain());
		contract.setPosition(slice.getPosition());
		contract.setWorkPosition(slice.getWorkPosition());
		contract.setRealmId(slice.getRealmId());
		contract.setState(slice.getState());
		contract.setTrimmed(slice.isTrimmed());
		contract.setExterne(slice.isExterne());
		contract.setDescription(slice.getDescription());
		contract.setValidFrom(slice.getContractValidFrom());
		contract.setValidTill(slice.getContractValidTill());
	}

	/**
	 * Copy (clone) of attribute values from slice to contract
	 * 
	 * @param slice
	 * @param contract
	 */
	private void copyExtendedAttributes(IdmContractSliceDto slice, IdmIdentityContractDto contract) {
		Assert.notNull(contract);
		Assert.notNull(contract.getId());
		Assert.notNull(slice);
		Assert.notNull(slice.getId());
		// TODO: all definitions should be copied - fix acc sync, i don't know why IdentityContractSyncTest fails ... (RT)
		IdmFormDefinitionDto definition = formService.getDefinition(contract.getClass());
		// delete all current values
		formService.deleteValues(contract, definition);
		// copy all values from slice to contract 
		// Load extended values for this slice
		List<IdmFormValueDto> sliceValues = formService.getValues(slice, definition);
		sliceValues.forEach(value -> {
			DtoUtils.clearAuditFields(value);
			value.setId(null);
		});
		formService.saveValues(contract, definition, sliceValues);
	}

	@Transactional
	@Override
	public void copyGuarantees(IdmContractSliceDto slice, IdmIdentityContractDto contract) {
		Assert.notNull(slice);
		Assert.notNull(slice.getId());
		Assert.notNull(contract);
		Assert.notNull(contract.getId());

		IdmContractSliceGuaranteeFilter guaranteeFilter = new IdmContractSliceGuaranteeFilter();
		guaranteeFilter.setContractSliceId(slice.getId());
		List<IdmContractSliceGuaranteeDto> guarantees = contractSliceGuaranteeService.find(guaranteeFilter, null)
				.getContent();
		List<IdmContractGuaranteeDto> resultGuarantees = new ArrayList<>();

		guarantees.forEach(guarantee -> {
			IdmContractGuaranteeDto result = this.cloneGuarante(guarantee);
			result.setIdentityContract(contract.getId());
			resultGuarantees.add(result);
		});

		IdmContractGuaranteeFilter contractGuaranteeFilter = new IdmContractGuaranteeFilter();
		contractGuaranteeFilter.setIdentityContractId(contract.getId());

		List<IdmContractGuaranteeDto> currentGuarantees = contractGuaranteeService.find(contractGuaranteeFilter, null)
				.getContent();

		// Find and create new guarantees
		resultGuarantees.stream().filter(guarantee -> { //
			return !currentGuarantees.stream() //
					.filter(cg -> guarantee.getGuarantee().equals(cg.getGuarantee())) //
					.findFirst() //
					.isPresent(); //
		}).forEach(guaranteeToAdd -> contractGuaranteeService.save(guaranteeToAdd));

		// Find and remove guarantees which missing in the current result set
		currentGuarantees.stream().filter(guarantee -> { //
			return !resultGuarantees.stream() //
					.filter(cg -> guarantee.getGuarantee().equals(cg.getGuarantee())) //
					.findFirst() //
					.isPresent(); //
		}).forEach(guaranteeToRemove -> contractGuaranteeService.delete(guaranteeToRemove));

	}

	@Transactional
	@Override
	public List<IdmContractSliceGuaranteeDto> findSliceGuarantees(UUID sliceId) {
		Assert.notNull(sliceId);

		IdmContractSliceGuaranteeFilter guaranteeFilter = new IdmContractSliceGuaranteeFilter();
		guaranteeFilter.setContractSliceId(sliceId);
		return contractSliceGuaranteeService.find(guaranteeFilter, null).getContent();

	}

	private IdmContractGuaranteeDto cloneGuarante(IdmContractSliceGuaranteeDto guarantee) {
		IdmContractGuaranteeDto result = new IdmContractGuaranteeDto();
		result.setGuarantee(guarantee.getGuarantee());
		return result;
	}

}
