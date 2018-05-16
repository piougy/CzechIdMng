package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent.ContractSliceEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.task.impl.SelectCurrentContractSliceTaskExecutor;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Integration tests with identity contracts slices
 * 
 * @author svandav
 *
 */
public class ContractSliceManagerTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private IdmContractSliceService contractSliceService;
	@Autowired
	private IdmContractSliceGuaranteeService contractGuaranteeService;
	@Autowired
	protected IdmLongRunningTaskService longRunningTaskService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private ContractSliceManager contractSliceManager;
	//

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
	}

	@After
	public void logout() {
		//
		super.logout();
	}

	@Test
	public void createSliceValidInPastTest() {
		IdmIdentityDto identity = helper.createIdentity();
		String contractCode = "contract-one";

		IdmContractSliceDto slice = helper.createContractSlice(identity, null, LocalDate.now().minusDays(10), null,
				LocalDate.now().minusDays(5));
		slice.setContractCode(contractCode);
		contractSliceService.save(slice);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> results = contractSliceService.find(filter, null).getContent();
		assertEquals(1, results.size());
		IdmContractSliceDto createdSlice = results.get(0);
		assertTrue(createdSlice.isValid());
		assertEquals(null, createdSlice.getValidTill());

		// Check created contract by that slice
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(identity.getId());
		List<IdmIdentityContractDto> resultsContract = contractService.find(filter, null).getContent().stream() //
				.filter(c -> contractService.get(c.getId()).getControlledBySlices()) //
				.collect(Collectors.toList());

		assertEquals(1, resultsContract.size()); //
		IdmIdentityContractDto contract = resultsContract.get(0);
		assertEquals(slice.getContractValidFrom(), contract.getValidFrom());
		assertEquals(slice.getContractValidTill(), contract.getValidTill());
		assertFalse(contract.isValidNowOrInFuture());
	}

	@Test
	public void createSliceValidInFutureTest() {
		IdmIdentityDto identity = helper.createIdentity();
		String contractCode = "contract-one";

		IdmContractSliceDto slice = helper.createContractSlice(identity, null, LocalDate.now().plusDays(10), null,
				LocalDate.now().plusDays(100));
		slice.setContractCode(contractCode);
		contractSliceService.save(slice);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> results = contractSliceService.find(filter, null).getContent();
		assertEquals(1, results.size());
		IdmContractSliceDto createdSlice = results.get(0);
		assertFalse(createdSlice.isValid());
		assertEquals(null, createdSlice.getValidTill());

		// Check created contract by that slice
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(identity.getId());
		List<IdmIdentityContractDto> resultsContract = contractService.find(filter, null).getContent().stream() //
				.filter(c -> contractService.get(c.getId()).getControlledBySlices()) //
				.collect(Collectors.toList());

		assertEquals(1, resultsContract.size());

		IdmIdentityContractDto contract = resultsContract.get(0);
		assertTrue(contract.isValidNowOrInFuture());
	}

	@Test
	public void createSlicesForOneContractTest() {
		IdmIdentityDto identity = helper.createIdentity();
		String contractCode = "contract-one";

		IdmContractSliceDto sliceFuture = helper.createContractSlice(identity, null, LocalDate.now().plusDays(10), null,
				LocalDate.now().plusDays(100));
		sliceFuture.setContractCode(contractCode);
		contractSliceService.save(sliceFuture);

		IdmContractSliceDto sliceCurrent = helper.createContractSlice(identity, null, LocalDate.now(), null,
				LocalDate.now().plusDays(50));
		sliceCurrent.setContractCode(contractCode);
		contractSliceService.save(sliceCurrent);

		IdmContractSliceDto slicePast = helper.createContractSlice(identity, null, LocalDate.now().minusDays(10), null,
				LocalDate.now().plusDays(200));
		slicePast.setContractCode(contractCode);
		contractSliceService.save(slicePast);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> results = contractSliceService.find(filter, null).getContent();
		assertEquals(3, results.size());
		UUID parentContract = results.get(0).getParentContract();
		List<IdmContractSliceDto> slices = contractSliceManager.findAllSlices(parentContract);
		assertEquals(3, slices.size());
		IdmContractSliceDto validSlice = contractSliceManager.findValidSlice(parentContract);
		// Valid slice should be currentSlice
		assertEquals(sliceCurrent, validSlice);

		IdmContractSliceDto nextSlice = contractSliceManager.findNextSlice(validSlice, slices);
		// Next slice should be futureSlice
		assertEquals(sliceFuture, nextSlice);

		IdmContractSliceDto previousSlice = contractSliceManager.findPreviousSlice(validSlice, slices);
		// Previous slice should be pasSlice
		assertEquals(slicePast, previousSlice);

		List<IdmContractSliceDto> unvalidSlices = contractSliceManager.findUnvalidSlices(null).getContent();
		assertEquals(0, unvalidSlices.size());

		// Check created contract by that slice
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(identity.getId());
		List<IdmIdentityContractDto> resultsContract = contractService.find(filter, null).getContent().stream() //
				.filter(c -> contractService.get(c.getId()).getControlledBySlices()) //
				.collect(Collectors.toList());

		assertEquals(1, resultsContract.size());
		// Current slice should be contract
		IdmIdentityContractDto contract = resultsContract.get(0);
		assertTrue(contract.isValid());
		assertEquals(sliceCurrent.getContractValidFrom(), contract.getValidFrom());
	}

	@Test
	public void deleteSliceTest() {
		IdmIdentityDto identity = helper.createIdentity();
		String contractCode = "contract-one";

		IdmContractSliceDto sliceFuture = helper.createContractSlice(identity, null, LocalDate.now().plusDays(10), null,
				LocalDate.now().plusDays(100));
		sliceFuture.setContractCode(contractCode);
		contractSliceService.save(sliceFuture);

		IdmContractSliceDto sliceCurrent = helper.createContractSlice(identity, null, LocalDate.now(), null,
				LocalDate.now().plusDays(50));
		sliceCurrent.setContractCode(contractCode);
		contractSliceService.save(sliceCurrent);

		IdmContractSliceDto slicePast = helper.createContractSlice(identity, null, LocalDate.now().minusDays(10), null,
				LocalDate.now().plusDays(200));
		slicePast.setContractCode(contractCode);
		contractSliceService.save(slicePast);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> results = contractSliceService.find(filter, null).getContent();
		assertEquals(3, results.size());
		UUID parentContract = results.get(0).getParentContract();
		List<IdmContractSliceDto> slices = contractSliceManager.findAllSlices(parentContract);
		assertEquals(3, slices.size());
		IdmContractSliceDto validSlice = contractSliceManager.findValidSlice(parentContract);
		// Valid slice should be currentSlice
		assertEquals(sliceCurrent, validSlice);

		// Check created contract by that slice
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(identity.getId());
		List<IdmIdentityContractDto> resultsContract = contractService.find(filter, null).getContent().stream() //
				.filter(c -> contractService.get(c.getId()).getControlledBySlices()) //
				.collect(Collectors.toList());

		assertEquals(1, resultsContract.size());
		// Current slice should be contract
		IdmIdentityContractDto contract = resultsContract.get(0);
		assertTrue(contract.isValid());
		assertEquals(sliceCurrent.getContractValidFrom(), contract.getValidFrom());

		// Delete the current slice
		contractSliceService.delete(contractSliceService.get(sliceCurrent.getId()));

		slices = contractSliceManager.findAllSlices(parentContract);
		assertEquals(2, slices.size());
		validSlice = contractSliceManager.findValidSlice(parentContract);
		// Valid slice should be slicePast now
		assertEquals(slicePast, validSlice);
		List<IdmContractSliceDto> unvalidSlices = contractSliceManager.findUnvalidSlices(null).getContent();
		assertEquals(0, unvalidSlices.size());
		// Reload the contract
		contract = contractService.get(contract.getId());
		assertTrue(contract.isValid());
		assertEquals(slicePast.getContractValidFrom(), contract.getValidFrom());
		assertEquals(slicePast.getContractValidTill(), contract.getValidTill());
	}

	@Test
	public void selectCurrentSliceAsContractLrtTest() {
		IdmIdentityDto identity = helper.createIdentity();
		String contractCode = "contract-one";

		IdmContractSliceDto sliceFuture = helper.createContractSlice(identity, null, LocalDate.now().plusDays(10), null,
				LocalDate.now().plusDays(100));
		sliceFuture.setContractCode(contractCode);
		contractSliceService.save(sliceFuture);

		IdmContractSliceDto sliceCurrent = helper.createContractSlice(identity, null, LocalDate.now(), null,
				LocalDate.now().plusDays(50));
		sliceCurrent.setContractCode(contractCode);
		contractSliceService.save(sliceCurrent);

		IdmContractSliceDto slicePast = helper.createContractSlice(identity, null, LocalDate.now().minusDays(10), null,
				LocalDate.now().plusDays(200));
		slicePast.setContractCode(contractCode);
		contractSliceService.save(slicePast);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> results = contractSliceService.find(filter, null).getContent();
		assertEquals(3, results.size());
		UUID parentContract = results.get(0).getParentContract();
		List<IdmContractSliceDto> slices = contractSliceManager.findAllSlices(parentContract);
		assertEquals(3, slices.size());
		IdmContractSliceDto validSlice = contractSliceManager.findValidSlice(parentContract);
		// Valid slice should be currentSlice
		assertEquals(sliceCurrent, validSlice);

		// Check created contract by that slice
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(identity.getId());
		List<IdmIdentityContractDto> resultsContract = contractService.find(filter, null).getContent().stream() //
				.filter(c -> contractService.get(c.getId()).getControlledBySlices()) //
				.collect(Collectors.toList());

		assertEquals(1, resultsContract.size());
		// Current slice should be contract
		IdmIdentityContractDto contract = resultsContract.get(0);
		assertTrue(contract.isValid());
		assertEquals(sliceCurrent.getContractValidFrom(), contract.getValidFrom());
		// None invalid slices
		List<IdmContractSliceDto> unvalidSlices = contractSliceManager.findUnvalidSlices(null).getContent();
		assertEquals(0, unvalidSlices.size());

		// Set current slice as not currently using
		sliceCurrent = contractSliceService.get(sliceCurrent.getId());
		sliceCurrent.setUsingAsContract(false);
		// Save without recalculation
		contractSliceService.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, sliceCurrent,
				ImmutableMap.of(IdmContractSliceService.SKIP_RECALCULATE_CONTRACT_SLICE, Boolean.TRUE)));

		// One invalid slice
		unvalidSlices = contractSliceManager.findUnvalidSlices(null).getContent();
		assertEquals(1, unvalidSlices.size());

		SelectCurrentContractSliceTaskExecutor lrt = new SelectCurrentContractSliceTaskExecutor();
		AutowireHelper.autowire(lrt);
		OperationResult result = lrt.process();
		assertEquals(OperationState.EXECUTED, result.getState());
	}

	@Test
	public void changeSlicesForTwoContractTest() {
		IdmIdentityDto identity = helper.createIdentity();
		String contractCodeOne = "contract-one";
		String contractCodeTwo = "contract-two";

		IdmContractSliceDto sliceFuture = helper.createContractSlice(identity, null, LocalDate.now().plusDays(10), null,
				LocalDate.now().plusDays(100));
		sliceFuture.setContractCode(contractCodeOne);
		contractSliceService.save(sliceFuture);

		IdmContractSliceDto sliceCurrentTwo = helper.createContractSlice(identity, null, LocalDate.now(), null,
				LocalDate.now().plusDays(50));
		sliceCurrentTwo.setContractCode(contractCodeTwo);
		contractSliceService.save(sliceCurrentTwo);

		IdmContractSliceDto slicePastTwo = helper.createContractSlice(identity, null, LocalDate.now().minusDays(10),
				null, LocalDate.now().plusDays(200));
		slicePastTwo.setContractCode(contractCodeTwo);
		contractSliceService.save(slicePastTwo);

		IdmContractSliceDto slicePast = helper.createContractSlice(identity, null, LocalDate.now().minusDays(10), null,
				LocalDate.now().plusDays(200));
		slicePast.setContractCode(contractCodeOne);
		contractSliceService.save(slicePast);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> results = contractSliceService.find(filter, null).getContent();
		assertEquals(4, results.size());

		// Get contract for contractCodeOne
		UUID parentContractOne = results.stream().filter(s -> s.getContractCode().equals(contractCodeOne)).findFirst()
				.get().getParentContract();
		List<IdmContractSliceDto> slices = contractSliceManager.findAllSlices(parentContractOne);
		assertEquals(2, slices.size());
		IdmContractSliceDto validSlice = contractSliceManager.findValidSlice(parentContractOne);
		// Valid slice should be slicePast now
		assertEquals(slicePast, validSlice);

		IdmContractSliceDto nextSlice = contractSliceManager.findNextSlice(validSlice, slices);
		// Next slice should be futureSlice
		assertEquals(sliceFuture, nextSlice);

		IdmContractSliceDto previousSlice = contractSliceManager.findPreviousSlice(validSlice, slices);
		// Previous slice should be null
		assertNull(previousSlice);

		List<IdmContractSliceDto> unvalidSlices = contractSliceManager.findUnvalidSlices(null).getContent();
		assertEquals(0, unvalidSlices.size());

		// Get contract for contractCodeTwo
		UUID parentContractTwo = results.stream().filter(s -> s.getContractCode().equals(contractCodeTwo)).findFirst()
				.get().getParentContract();
		List<IdmContractSliceDto> slicesTwo = contractSliceManager.findAllSlices(parentContractTwo);
		assertEquals(2, slicesTwo.size());
		IdmContractSliceDto validSliceTwo = contractSliceManager.findValidSlice(parentContractTwo);
		// Valid slice should be sliceCurrentTwo now
		assertEquals(sliceCurrentTwo, validSliceTwo);

		// Check created contract by that slice
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(identity.getId());
		List<IdmIdentityContractDto> resultsContract = contractService.find(filter, null).getContent().stream() //
				.filter(c -> contractService.get(c.getId()).getControlledBySlices()) //
				.collect(Collectors.toList());

		// Two contract controlled by slices must exists now
		assertEquals(2, resultsContract.size());
		// Past slice should be contract
		IdmIdentityContractDto contract = resultsContract.stream().filter(c -> c.getId().equals(parentContractOne))
				.findFirst().get();
		assertTrue(contract.isValid());
		assertEquals(slicePast.getContractValidFrom(), contract.getValidFrom());

		// Change parent contract from Two to One
		sliceCurrentTwo.setContractCode(contractCodeOne);
		contractSliceService.save(sliceCurrentTwo);

		// Check slice for contractCodeTwo
		slicesTwo = contractSliceManager.findAllSlices(parentContractTwo);
		assertEquals(1, slicesTwo.size());
		validSliceTwo = contractSliceManager.findValidSlice(parentContractTwo);
		// Valid slice should be sliceCurrentTwo now
		assertEquals(slicePastTwo, validSliceTwo);
		assertTrue(validSliceTwo.isUsingAsContract());

		// Check slice for contractCodeOne
		slices = contractSliceManager.findAllSlices(parentContractOne);
		assertEquals(3, slices.size());
		validSlice = contractSliceManager.findValidSlice(parentContractOne);
		// Valid slice should be currentSliceTwo now
		assertEquals(sliceCurrentTwo, validSlice);

		nextSlice = contractSliceManager.findNextSlice(validSlice, slices);
		// Next slice should be futureSlice
		assertEquals(sliceFuture, nextSlice);

		previousSlice = contractSliceManager.findPreviousSlice(validSlice, slices);
		// Previous slice should be pastSlice
		assertEquals(slicePast, previousSlice);

		unvalidSlices = contractSliceManager.findUnvalidSlices(null).getContent();
		assertEquals(0, unvalidSlices.size());

		// Check created contract by that slice
		resultsContract = contractService.find(filter, null).getContent().stream() //
				.filter(c -> contractService.get(c.getId()).getControlledBySlices()) //
				.collect(Collectors.toList());

		assertEquals(2, resultsContract.size());

		// Change parent contract from Two to One (slicePastTwo)
		slicePastTwo.setContractCode(contractCodeOne);
		contractSliceService.save(slicePastTwo);

		// Contract TWO was deleted
		resultsContract = contractService.find(filter, null).getContent().stream() //
				.filter(c -> contractService.get(c.getId()).getControlledBySlices()) //
				.collect(Collectors.toList());

		assertEquals(1, resultsContract.size());

		// Past slice should be contract
		contract = resultsContract.get(0);
		assertTrue(contract.isValid());
		assertEquals(sliceCurrentTwo.getContractValidFrom(), contract.getValidFrom());

	}

	@Test
	public void testReferentialIntegrityOnIdentityDelete() {
		// prepare data
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identityWithContract = helper.createIdentity();
		IdmContractSliceDto slice = helper.createContractSlice(identityWithContract);
		helper.createContractSliceGuarantee(slice.getId(), identity.getId());
		//
		IdmContractSliceGuaranteeFilter filter = new IdmContractSliceGuaranteeFilter();
		filter.setContractSliceId(slice.getId());
		List<IdmContractSliceGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(1, guarantees.size());
		//
		helper.deleteIdentity(identity.getId());
		//
		guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(0, guarantees.size());
	}

	@Test
	public void testReferentialIntegrityOnContractDelete() {
		// prepare data
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identityWithContract = helper.createIdentity();
		IdmContractSliceDto slice = helper.createContractSlice(identityWithContract);
		helper.createContractSliceGuarantee(slice.getId(), identity.getId());
		//
		IdmContractSliceGuaranteeFilter filter = new IdmContractSliceGuaranteeFilter();
		filter.setGuaranteeId(identity.getId());
		List<IdmContractSliceGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(1, guarantees.size());
		//
		contractSliceService.deleteById(slice.getId());
		//
		guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(0, guarantees.size());
	}

	@Test
	public void identityFilterTest() {
		IdmIdentityDto identity = helper.createIdentity();

		IdmTreeNodeDto node = helper.createTreeNode();
		IdmTreeNodeDto node2 = helper.createTreeNode();

		IdmContractSliceDto slice = helper.createContractSlice(identity, node, null, null, null);
		IdmContractSliceDto slice2 = helper.createContractSlice(identity, node2, null, null, null);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		Page<IdmContractSliceDto> result = contractSliceService.find(filter, null);
		assertEquals("Wrong Identity", 2, result.getTotalElements());
		assertTrue(result.getContent().contains(slice));
		assertTrue(result.getContent().contains(slice2));
	}

	@Test
	public void externeFilterTest() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();

		IdmTreeNodeDto node = helper.createTreeNode();
		IdmTreeNodeDto node2 = helper.createTreeNode();

		IdmContractSliceDto slice = helper.createContractSlice(identity, node, null, null, null);
		IdmContractSliceDto slice2 = helper.createContractSlice(identity2, node2, null, null, null);

		slice.setExterne(true);
		contractSliceService.save(slice);

		slice2.setExterne(false);
		contractSliceService.save(slice2);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setExterne(true);
		Page<IdmContractSliceDto> result = contractSliceService.find(filter, null);
		assertTrue(result.getContent().contains(slice));
		assertFalse(result.getContent().contains(slice2));

		filter.setExterne(false);
		result = contractSliceService.find(filter, null);
		assertTrue(result.getContent().contains(slice2));
		assertFalse(result.getContent().contains(slice));
	}

	@Test
	public void mainFilterTest() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();

		IdmTreeNodeDto node = helper.createTreeNode();
		IdmTreeNodeDto node2 = helper.createTreeNode();

		IdmContractSliceDto slice = helper.createContractSlice(identity, node, null, null, null);
		IdmContractSliceDto slice2 = helper.createContractSlice(identity2, node2, null, null, null);

		slice.setMain(true);
		contractSliceService.save(slice);

		slice2.setMain(false);
		contractSliceService.save(slice2);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setMain(true);
		Page<IdmContractSliceDto> result = contractSliceService.find(filter, null);
		assertTrue(result.getContent().contains(slice));
		assertFalse(result.getContent().contains(slice2));

		filter.setMain(false);
		result = contractSliceService.find(filter, null);
		assertTrue(result.getContent().contains(slice2));
		assertFalse(result.getContent().contains(slice));
	}

}
