package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice;
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
	private LookupService lookupService;
	@Autowired
	protected IdmLongRunningTaskService longRunningTaskService;
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
	public void testSetStateByDateValidInFuture() {
		IdmContractSliceDto slice = helper.createContractSlice(helper.createIdentity(), null,
				new LocalDate().plusDays(1), null);
		//
		Assert.assertNull(slice.getState());
		Assert.assertFalse(
				((IdmContractSlice) lookupService.lookupEntity(IdmContractSliceDto.class, slice.getId())).isDisabled());
	}

	@Test
	public void testSetStateByDateValidInPast() {
		IdmContractSliceDto slice = helper.createContractSlice(helper.createIdentity(), null,
				new LocalDate().plusDays(1), new LocalDate().minusDays(1));
		//
		Assert.assertNull(slice.getState());
		Assert.assertFalse(
				((IdmContractSlice) lookupService.lookupEntity(IdmContractSliceDto.class, slice.getId())).isDisabled());
	}

	@Test
	public void identityFilterTest() {
		IdmIdentityDto identity = helper.createIdentity();

		IdmTreeNodeDto node = helper.createTreeNode();
		IdmTreeNodeDto node2 = helper.createTreeNode();

		IdmContractSliceDto slice = helper.createContractSlice(identity, node, null, null);
		IdmContractSliceDto contract2 = helper.createContractSlice(identity, node2, null, null);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		Page<IdmContractSliceDto> result = service.find(filter, null);
		assertEquals("Wrong Identity", 2, result.getTotalElements());
		assertTrue(result.getContent().contains(slice));
		assertTrue(result.getContent().contains(contract2));
	}

	@Test
	public void externeFilterTest() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();

		IdmTreeNodeDto node = helper.createTreeNode();
		IdmTreeNodeDto node2 = helper.createTreeNode();

		IdmContractSliceDto slice = helper.createContractSlice(identity, node, null, null);
		IdmContractSliceDto contract2 = helper.createContractSlice(identity2, node2, null, null);

		slice.setExterne(true);
		service.save(slice);

		contract2.setExterne(false);
		service.save(contract2);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setExterne(true);
		Page<IdmContractSliceDto> result = service.find(filter, null);
		assertTrue(result.getContent().contains(slice));
		assertFalse(result.getContent().contains(contract2));

		filter.setExterne(false);
		result = service.find(filter, null);
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(slice));
	}

	@Test
	public void mainFilterTest() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();

		IdmTreeNodeDto node = helper.createTreeNode();
		IdmTreeNodeDto node2 = helper.createTreeNode();

		IdmContractSliceDto slice = helper.createContractSlice(identity, node, null, null);
		IdmContractSliceDto contract2 = helper.createContractSlice(identity2, node2, null, null);

		slice.setMain(true);
		service.save(slice);

		contract2.setMain(false);
		service.save(contract2);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setMain(true);
		Page<IdmContractSliceDto> result = service.find(filter, null);
		assertTrue(result.getContent().contains(slice));
		assertFalse(result.getContent().contains(contract2));

		filter.setMain(false);
		result = service.find(filter, null);
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(slice));
	}

}
