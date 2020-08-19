package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.ContractSliceConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.AutomaticRoleManager;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
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
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired 
	private EntityStateManager entityStateManager;

	@Override
	@Transactional
	public IdmIdentityContractDto updateContractBySlice(IdmIdentityContractDto contract, IdmContractSliceDto slice,
			Map<String, Serializable> eventProperties) {

		Assert.notNull(contract, "Contract is required.");
		Assert.notNull(slice, "Contract slice cannot be null!");
		Assert.notNull(slice.getIdentity(), "Contract slice identity is required.");
		Assert.isTrue(slice.isUsingAsContract(), "Contract slice has to be actualy used for contract.");

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
		// We need to flag recalculation for contract immediately to prevent e.g. synchronization ends before flag is created by NOTIFY event asynchronously.
		if (getBooleanProperty(AutomaticRoleManager.SKIP_RECALCULATION, eventProperties)) {
			entityStateManager.createState(savedContract, OperationState.BLOCKED, CoreResultCode.AUTOMATIC_ROLE_SKIPPED, null);
		}

		// Copy guarantees
		copyGuarantees(slice, savedContract);

		return savedContract;
	}
		
	/**
	 * Return true if event properties contains given property and this property is true.
	 * If event does not contains this property, then return false.
	 * 
	 * TODO: Move to utils
	 * @param property
	 * @param properties
	 * @return
	 */
	private boolean getBooleanProperty(String property, Map<String, Serializable> properties) {
		Assert.notNull(property, "Name of event property cannot be null!");
		if (properties == null) {
			return false;
		}

		Object propertyValue = properties.get(property);

		if (propertyValue == null) {
			return false;
		}
		if (propertyValue instanceof String) {
			return Boolean.parseBoolean((String) propertyValue);
        }
		//
		Assert.isInstanceOf(Boolean.class, propertyValue, MessageFormat
				.format("Property [{0}] must be Boolean, but is [{1}]!", property, propertyValue.getClass()));

		if ((Boolean) propertyValue) {
			return true;
		}

		return false;
	}

	/**
	 * Protection mode is activate. Check if the next slice has valid from of
	 * contract lover then contract valid till (plus protection interval) on given
	 * slice, then contract will does not terminated (his valid till will be sets by
	 * valid from on next slice)
	 *
	 * @param slice
	 * @param contract
	 * @param protectionInterval
	 */
	private void resolveProtectionInterval(IdmContractSliceDto slice, IdmIdentityContractDto contract,
			int protectionInterval) {
		Assert.notNull(contract, "Contract is required.");
		Assert.notNull(contract.getId(), "Contract identifier is required.");

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

		// Diff is not positive, it means valid from of contract in next slice is less then
		// till of contract in current slice. We will do nothing.
		if (diffInDays <= 0) {
			return;
		}

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
		Assert.notNull(slices, "Contract slices are required.");
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
		Assert.notNull(slices, "Contract slices are required.");
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
		Assert.notNull(slices, "Contract slices are required.");
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
	public IdmContractSliceDto setSliceAsCurrentlyUsing(IdmContractSliceDto slice, Map<String, Serializable> eventProperties) {
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
		if (eventProperties == null) {
			return contractSliceService.save(slice);
		}
		return contractSliceService.publish(
				new ContractSliceEvent(ContractSliceEventType.UPDATE, slice, ImmutableMap.copyOf(eventProperties)))
				.getContent();
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
	@Override
	public void convertSliceToContract(IdmContractSliceDto slice, IdmIdentityContractDto contract) {
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

		IdmFormDefinitionDto definition = formService.getDefinition(contract.getClass());
		formService.mergeValues(definition, slice, contract);
	}

	@Transactional
	@Override
	public void copyGuarantees(IdmContractSliceDto slice, IdmIdentityContractDto contract) {
		Assert.notNull(slice, "Contract slice is required.");
		Assert.notNull(slice.getId(), "Contract slice identifier is required.");
		Assert.notNull(contract, "Contract is required.");
		Assert.notNull(contract.getId(), "Contract identifier is required.");

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
		Assert.notNull(sliceId, "Contract slice identifier is required.");

		IdmContractSliceGuaranteeFilter guaranteeFilter = new IdmContractSliceGuaranteeFilter();
		guaranteeFilter.setContractSliceId(sliceId);
		return contractSliceGuaranteeService.find(guaranteeFilter, null).getContent();

	}

	private IdmContractGuaranteeDto cloneGuarante(IdmContractSliceGuaranteeDto guarantee) {
		IdmContractGuaranteeDto result = new IdmContractGuaranteeDto();
		result.setGuarantee(guarantee.getGuarantee());
		return result;
	}

	@Override
	public void recalculateContractSlice(IdmContractSliceDto slice, IdmContractSliceDto originalSlice, Map<String, Serializable> eventProperties) {

		boolean forceRecalculateCurrentUsingSlice = false;
		Object forceRecalculateCurrentUsingSliceAsObject = eventProperties.get(IdmContractSliceService.FORCE_RECALCULATE_CURRENT_USING_SLICE);
		if (forceRecalculateCurrentUsingSliceAsObject == null) {
			forceRecalculateCurrentUsingSlice = false;
		} else if (forceRecalculateCurrentUsingSliceAsObject instanceof Boolean) {
			forceRecalculateCurrentUsingSlice = (Boolean) forceRecalculateCurrentUsingSliceAsObject;
		} else {
			forceRecalculateCurrentUsingSlice = false;
		}

		boolean recalculateUsingAsContract = false;
		if (slice.getIdentity() != null) {
			UUID parentContract = slice.getParentContract();

			// Check if was contractCode changed, if yes, then set parentContract to
			// null (will be recalculated)
			if (originalSlice != null && !Objects.equal(originalSlice.getContractCode(), slice.getContractCode())) {
				slice.setParentContract(null);
				parentContract = null; // When external code changed, link or create new contract is required
			}
			if (originalSlice != null && !Objects.equal(originalSlice.getParentContract(), slice.getParentContract())) {
				slice.setParentContract(null);
				slice.setUsingAsContract(false);
			}

			if (parentContract == null) {
				slice = linkOrCreateContract(slice, eventProperties);
			} else {
				// Update contract by that slice
				if(slice.isUsingAsContract()) {
					// Validity of slice was changed, slice cannot be using for update the contract
					if (originalSlice != null && !Objects.equal(originalSlice.getValidFrom(), slice.getValidFrom())) {
						recalculateUsingAsContract = true;
					} else {
						IdmIdentityContractDto contract = contractService.get(parentContract);
						this.getBean().updateContractBySlice(contract, slice, eventProperties);
					}
				}
			}
		}

		UUID parentContract = slice.getParentContract();

		if (originalSlice == null) {
			// Slice is new, we want to recalculate "Is using as contract" field.
			recalculateUsingAsContract = true;
		}

		// Recalculate on change of 'parentContract' field
		boolean parentContractChanged = false;
		if (originalSlice != null && !Objects.equal(originalSlice.getParentContract(), slice.getParentContract())) {
			UUID originalParentContract = originalSlice.getParentContract();
			// Parent contract was changed ... we need recalculate parent contract for
			// original slice
			parentContractChanged = true;
			if (originalParentContract != null) {
				IdmIdentityContractDto originalContract = contractService.get(originalParentContract);
				// Find other slices for original contract
				IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
				sliceFilter.setParentContract(originalParentContract);
				List<IdmContractSliceDto> originalSlices = contractSliceService.find(sliceFilter, null).getContent();
				if (!originalSlices.isEmpty()) {
					IdmContractSliceDto originalNextSlice = this.getBean().findNextSlice(originalSlice, originalSlices);
					IdmContractSliceDto originalSliceToUpdate = originalNextSlice;
					if (originalNextSlice != null) {
						// Next slice exists, update valid-till on previous slice by that slice
						IdmContractSliceDto originalPreviousSlice = this.getBean().findPreviousSlice(originalNextSlice,
								originalSlices);
						if (originalPreviousSlice != null) {
							originalPreviousSlice.setValidTill(originalNextSlice.getValidFrom().minusDays(1));
							originalSliceToUpdate = originalPreviousSlice;
						}
					} else {
						// Next slice does not exists. I means original slice was last. Set valid-till
						// on previous slice to null.
						IdmContractSliceDto originalPreviousSlice = this.getBean().findPreviousSlice(originalSlice,
								originalSlices);
						if (originalPreviousSlice != null
								&& this.getBean().findNextSlice(originalPreviousSlice, originalSlices) == null) {
							originalPreviousSlice.setValidTill(null);
							originalSliceToUpdate = originalPreviousSlice;
						}
					}
					// Save with force recalculation
					contractSliceService.publish(
							new ContractSliceEvent(ContractSliceEventType.UPDATE, originalSliceToUpdate, ImmutableMap
									.of(IdmContractSliceService.FORCE_RECALCULATE_CURRENT_USING_SLICE, Boolean.TRUE)));
				} else {
					// Parent contract was changed and old contract does not have next slice, we
					// have to delete him.
					// Delete contract
					contractService.publish(
							new IdentityContractEvent(IdentityContractEventType.DELETE,
									originalContract, ImmutableMap.copyOf(eventProperties)))
							.getContent();
				}
			}
			// Parent contract was changed, want to recalculate "Is using as contract"
			// field.
			recalculateUsingAsContract = true;
		}

		// Recalculate the valid-till on previous slice
		// Evaluates on change of 'validFrom' field or new slice or change the parent
		// contract
		if (originalSlice == null || parentContractChanged
				|| (originalSlice != null && !Objects.equal(originalSlice.getValidFrom(), slice.getValidFrom()))) {
			// Valid from was changed ... we have to change of validity till on previous
			// slice
			if (parentContract != null) {
				// Find other slices for parent contract
				List<IdmContractSliceDto> slices = this.getBean().findAllSlices(parentContract);
				if (!slices.isEmpty()) {
					// Update validity till on this slice and on previous slice
					recalculateValidTill(slice, slices);
					// Update validity till on this original slice and on previous slice (to
					// original slice)
					if (originalSlice != null) {
						IdmContractSliceDto nextSliceForOriginalSlice = this.getBean().findNextSlice(originalSlice,
								slices);
						if (nextSliceForOriginalSlice == null) {
							// Next slice not exists, it means original slice was last
							IdmContractSliceDto previousSliceForOriginalSlice = this.getBean()
									.findPreviousSlice(originalSlice, slices);
							if (previousSliceForOriginalSlice != null
									&& this.getBean().findNextSlice(previousSliceForOriginalSlice, slices) == null) {
								previousSliceForOriginalSlice.setValidTill(null);
								// Save with skip this processor
								saveWithoutRecalculate(previousSliceForOriginalSlice);
							}
						}
					}
				}
			}
			// Validity from was changed, want to recalculate "Is using as contract" field.
			recalculateUsingAsContract = true;
		}

		if (recalculateUsingAsContract || forceRecalculateCurrentUsingSlice) {
			IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
			sliceFilter.setParentContract(parentContract);
			sliceFilter.setUsingAsContract(Boolean.TRUE);

			IdmContractSliceDto shouldBeSetAsUsing = this.getBean().findValidSlice(parentContract);

			if (shouldBeSetAsUsing != null) {
				Map<String, Serializable> clonedProperties = new HashMap<>(eventProperties);
				if (clonedProperties.containsKey(IdmContractSliceService.FORCE_RECALCULATE_CURRENT_USING_SLICE)){
					clonedProperties.remove(IdmContractSliceService.FORCE_RECALCULATE_CURRENT_USING_SLICE);
				}
				shouldBeSetAsUsing = this.getBean().setSliceAsCurrentlyUsing(shouldBeSetAsUsing, clonedProperties);
				if (slice.equals(shouldBeSetAsUsing)) {
					// If that slice should be using as contract, then we using returned instance
					// (instead the reload slice from DB)
					slice = shouldBeSetAsUsing;
				}
			}
		}

		// Check if is slice new or contract valid till field was changed.
		if (originalSlice == null
				|| (!Objects.equal(originalSlice.getContractValidTill(), slice.getContractValidTill()))) {
			// If is slice last, then will be to slice valid till copy date of contract
			// valid till
			boolean isSliceLast = this.getBean().findNextSlice(slice, this.getBean().findAllSlices(parentContract)) == null
					? true
					: false;
			if (isSliceLast) {
				slice.setValidTill(null);
				saveWithoutRecalculate(slice);
			}
		}
	}

	/**
	 * Recalculate valid till on given slice and on previous slice
	 *
	 * @param slice
	 * @param slices
	 */
	private void recalculateValidTill(IdmContractSliceDto slice, List<IdmContractSliceDto> slices) {
		this.getBean().updateValidTillOnPreviousSlice(slice, slices);
		IdmContractSliceDto nextSlice = this.getBean().findNextSlice(slice, slices);
		if (nextSlice != null) {
			LocalDate validTill = nextSlice.getValidFrom().minusDays(1);
			slice.setValidTill(validTill);
		} else {
			slice.setValidTill(null);
		}
		// Save with skip this processor
		saveWithoutRecalculate(slice);
	}

	/**
	 * Save slice without recalculate ... it means with skip this processor
	 *
	 * @param slice
	 */
	private void saveWithoutRecalculate(IdmContractSliceDto slice) {
		contractSliceService.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, slice,
				ImmutableMap.of(IdmContractSliceService.SKIP_RECALCULATE_CONTRACT_SLICE, Boolean.TRUE)));
	}

	/**
	 * Create or link contract from the slice or create relation on the exists
	 * contract
	 *
	 * @param slice
	 * @return
	 */
	private IdmContractSliceDto linkOrCreateContract(IdmContractSliceDto slice,
			Map<String, Serializable> eventProperties) {

		String contractCode = slice.getContractCode();
		if (Strings.isNullOrEmpty(contractCode)) {
			// Create new parent contract
			// When new contract is created, then this slice have to be sets as "Is using as
			// contract"
			slice.setUsingAsContract(true);
			IdmIdentityContractDto contract = this.getBean().updateContractBySlice(new IdmIdentityContractDto(), slice, eventProperties);
			slice.setParentContract(contract.getId());

			return contractSliceService.saveInternal(slice);
		} else {
			// Find other slices
			IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
			sliceFilter.setContractCode(contractCode);
			sliceFilter.setIdentity(slice.getIdentity());
			List<IdmContractSliceDto> slices = contractSliceService.find(sliceFilter, null).getContent();
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
				IdmIdentityContractDto contract = this.getBean().updateContractBySlice(new IdmIdentityContractDto(), slice, eventProperties);
				slice.setParentContract(contract.getId());

				return contractSliceService.saveInternal(slice);
			} else {
				// We found and link on existed contract. We have to do update this contract by
				// slice.
				slice.setParentContract(parentContractId);
				IdmContractSliceDto sliceSaved = contractSliceService.saveInternal(slice);

				// Update contract by that slice
				if(sliceSaved.isUsingAsContract()) {
					IdmIdentityContractDto contract = contractService.get(sliceSaved.getParentContract());
					this.getBean().updateContractBySlice(contract, sliceSaved, eventProperties);
				}

				return sliceSaved;
			}
		}
	}

	/**
	 * Return this bean for execute some method in new transaction
	 * @return
	 */
	private DefaultContractSliceManager getBean() {
		return applicationContext.getBean(this.getClass());
	}
}
