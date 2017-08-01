package eu.bcvsolutions.idm.core.model.service.impl;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Created by marek on 1.8.17.
 */
public class DefaultIdmIdentityContractServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	TestHelper helper;
	@Autowired
	IdmTreeNodeService treeNodeService;
	@Autowired
	IdmIdentityContractService service;

	@Before
	public void logIn(){
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logOut(){
		super.logout();
	}

	@Test
	public void textFilterTest(){
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();
		IdmIdentityDto identity3 = helper.createIdentity();
		IdmIdentityDto identity4 = helper.createIdentity();

		IdmTreeNode node = helper.createTreeNode();
		node.setName("Position105");
		treeNodeService.save(node);

		IdmTreeNode node2 = helper.createTreeNode();
		node2.setName("Position006");
		treeNodeService.save(node2);

		IdmTreeNode node3 = helper.createTreeNode();
		node3.setCode("Position007");
		treeNodeService.save(node3);

		IdmTreeNode node4 = helper.createTreeNode();
		node4.setCode("Position108");
		treeNodeService.save(node4);

		IdmIdentityContractDto contract = helper.createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = helper.createIdentityContact(identity2,node2);
		IdmIdentityContractDto contract3 = helper.createIdentityContact(identity3,node3);
		IdmIdentityContractDto contract4 = helper.createIdentityContact(identity4,node4);

		contract.setPosition("Position001");
		contract = service.save(contract);

		contract2.setPosition("Position102");
		service.save(contract2);

		contract3.setPosition("Position103");
		service.save(contract3);

		contract4.setPosition("Position104");
		service.save(contract4);

		IdentityContractFilter filter = new IdentityContractFilter();
		filter.setText("Position00");
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertEquals("Wrong Text",3,result.getTotalElements());
		assertTrue(result.getContent().contains(contract));
		assertTrue(result.getContent().contains(contract2));
		assertTrue(result.getContent().contains(contract3));
	}

	@Test
	public void identityFilterTest(){
		IdmIdentityDto identity = helper.createIdentity();

		IdmTreeNode node = helper.createTreeNode();
		IdmTreeNode node2 = helper.createTreeNode();

		IdmIdentityContractDto contract = helper.createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = helper.createIdentityContact(identity,node2);

		IdentityContractFilter filter = new IdentityContractFilter();
		filter.setIdentity(identity.getId());
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertEquals("Wrong Identity",3,result.getTotalElements());
		assertTrue(result.getContent().contains(service.getPrimeContract(identity.getId())));
		assertTrue(result.getContent().contains(contract));
		assertTrue(result.getContent().contains(contract2));
	}

	@Test
	public void datesValidFilterTest(){
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();
		IdmIdentityDto identity3 = helper.createIdentity();
		IdmIdentityDto identity4 = helper.createIdentity();

		IdmTreeNode node = helper.createTreeNode();
		IdmTreeNode node2 = helper.createTreeNode();
		IdmTreeNode node3 = helper.createTreeNode();
		IdmTreeNode node4 = helper.createTreeNode();

		IdmIdentityContractDto contract = helper.createIdentityContact(identity,node, org.joda.time.LocalDate.now(),org.joda.time.LocalDate.parse("2021-06-05"));
		IdmIdentityContractDto contract2 = helper.createIdentityContact(identity2,node2,org.joda.time.LocalDate.now(),org.joda.time.LocalDate.parse("2020-05-05"));
		IdmIdentityContractDto contract3 = helper.createIdentityContact(identity3,node3,org.joda.time.LocalDate.now(),org.joda.time.LocalDate.parse("2016-05-05"));
		IdmIdentityContractDto contract4 = helper.createIdentityContact(identity4,node4,org.joda.time.LocalDate.parse("2018-05-05"),org.joda.time.LocalDate.parse("2025-05-05"));

		IdentityContractFilter filter = new IdentityContractFilter();
		filter.setValidFrom(contract.getValidFrom());
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));

		filter.setValidFrom(null);
		filter.setValidTill(contract2.getValidTill());
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract2));

		filter.setValidTill(null);
		filter.setValid(true);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(contract3));

		filter.setValid(null);
		filter.setValidNowOrInFuture(true);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract4));

		filter.setValidNowOrInFuture(false);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract3));
	}

	@Test
	public void externeFilterTest(){
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();

		IdmTreeNode node = helper.createTreeNode();
		IdmTreeNode node2 = helper.createTreeNode();

		IdmIdentityContractDto contract = helper.createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = helper.createIdentityContact(identity2,node2);

		contract.setExterne(true);
		service.save(contract);

		contract2.setExterne(false);
		service.save(contract2);

		IdentityContractFilter filter = new IdentityContractFilter();
		filter.setExterne(true);
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));
		assertFalse(result.getContent().contains(contract2));

		filter.setExterne(false);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(contract));
	}

	@Test
	public void mainFilterTest(){
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();

		IdmTreeNode node = helper.createTreeNode();
		IdmTreeNode node2 = helper.createTreeNode();

		IdmIdentityContractDto contract = helper.createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = helper.createIdentityContact(identity2,node2);

		contract.setMain(true);
		service.save(contract);

		contract2.setMain(false);
		service.save(contract2);

		IdentityContractFilter filter = new IdentityContractFilter();
		filter.setMain(true);
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));
		assertFalse(result.getContent().contains(contract2));

		filter.setMain(false);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(contract));
	}
}
