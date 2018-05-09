package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
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
	private IdmContractSliceService service;
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
		service.save(slice);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> results = service.find(filter, null).getContent();
		assertEquals(1, results.size());
		IdmContractSliceDto createdSlice = results.get(0);
		assertTrue(!createdSlice.isValid());
		assertEquals(LocalDate.now().minusDays(5), createdSlice.getValidTill());

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
		service.save(slice);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> results = service.find(filter, null).getContent();
		assertEquals(1, results.size());
		IdmContractSliceDto createdSlice = results.get(0);
		assertTrue(!createdSlice.isValid());
		assertEquals(LocalDate.now().plusDays(100), createdSlice.getValidTill());

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
		service.save(sliceFuture);
		
		
		IdmContractSliceDto sliceCurrent = helper.createContractSlice(identity, null, LocalDate.now(), null,
				LocalDate.now().plusDays(50));
		sliceCurrent.setContractCode(contractCode);
		service.save(sliceCurrent);

		IdmContractSliceDto slicePast = helper.createContractSlice(identity, null, LocalDate.now().minusDays(10), null,
				LocalDate.now().plusDays(200));
		slicePast.setContractCode(contractCode);
		service.save(slicePast);

		
		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> results = service.find(filter, null).getContent();
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
		service.deleteById(slice.getId());
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
		Page<IdmContractSliceDto> result = service.find(filter, null);
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
		service.save(slice);

		slice2.setExterne(false);
		service.save(slice2);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setExterne(true);
		Page<IdmContractSliceDto> result = service.find(filter, null);
		assertTrue(result.getContent().contains(slice));
		assertFalse(result.getContent().contains(slice2));

		filter.setExterne(false);
		result = service.find(filter, null);
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
		service.save(slice);

		slice2.setMain(false);
		service.save(slice2);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setMain(true);
		Page<IdmContractSliceDto> result = service.find(filter, null);
		assertTrue(result.getContent().contains(slice));
		assertFalse(result.getContent().contains(slice2));

		filter.setMain(false);
		result = service.find(filter, null);
		assertTrue(result.getContent().contains(slice2));
		assertFalse(result.getContent().contains(slice));
	}

}
