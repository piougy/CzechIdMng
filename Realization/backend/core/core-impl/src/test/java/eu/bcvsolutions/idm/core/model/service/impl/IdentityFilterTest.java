package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for Identity filters
 *
 * @author Marek Klement
 *
 */
public class IdentityFilterTest extends AbstractIntegrationTest{

	@Autowired private IdmIdentityService idmIdentityService;
	@Autowired private TestHelper testHelper;

	@Before
	public void login(){
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout(){
		super.logout();
	}

	@Test
	public void testCreateIdentity(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName000","ThisIsTestName000","ThisIsTestName000@gemail.eu", "000000000", false);
		assertNotNull(person);
	}

	@Test
	public void testUsernameFilter(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName001","ThisIsTestName001","ThisIsTestName001@gemail.eu", "000000001", false);
		IdentityFilter filter = new IdentityFilter();
		filter.setUsername(person.getUsername());
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Username",1, result.getTotalElements());
		assertEquals("Wrong Username user ID",person.getId(),result.getContent().get(0).getId());
	}

	@Test
	public void testFirstnameFilter(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName002","ThisIsTestName002","ThisIsTestName002@gemail.eu", "000000002", false);
		IdentityFilter filter = new IdentityFilter();
		filter.setFirstName(person.getFirstName());
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Firstname",1, result.getTotalElements());
		assertEquals("Wrong Firstname user ID",person.getId(),result.getContent().get(0).getId());
	}

	@Test
	public void testLastnameFilter(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName003","ThisIsTestName003","ThisIsTestName003@gemail.eu", "000000003", false);
		IdentityFilter filter = new IdentityFilter();
		filter.setLastName(person.getLastName());
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Lastname",1, result.getTotalElements());
		assertEquals("Wrong Lastname user ID",person.getId(),result.getContent().get(0).getId());
	}

	@Test
	public void testDisabledFilter(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName004","ThisIsTestName004","ThisIsTestName004@gemail.eu", "000000004", false);
		IdentityFilter filter = new IdentityFilter();
		filter.setDisabled(false);
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		person.setDisabled(true);
		idmIdentityService.save(person);
		filter.setDisabled(false);
		Page<IdmIdentityDto> result2 = idmIdentityService.find(filter, null);
		int changed = (int) (result.getTotalElements() - result2.getTotalElements());
		assertEquals("Wrong Disabled",1, changed);
	}

	@Test
	public void testPropertyValueFilter(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName012","ThisIsTestName012","ThisIsTestName012@gemail.eu", "100000012", false);
		IdentityFilter filter = new IdentityFilter();
		filter.setProperty(IdmIdentity_.username.getName());
		filter.setValue(person.getUsername());
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Set Property and Value - username",1, result.getTotalElements());
		assertEquals("Wrong Property an Value - username user ID",person.getId(),result.getContent().get(0).getId());

		filter.setProperty(IdmIdentity_.firstName.getName());
		filter.setValue(person.getFirstName());
		result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Set Property and Value - firstname",1, result.getTotalElements());
		assertEquals("Wrong Property an Value - firstname user ID",person.getId(),result.getContent().get(0).getId());

		filter.setProperty(IdmIdentity_.lastName.getName());
		filter.setValue(person.getLastName());
		result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Set Property and Value - lastname",1, result.getTotalElements());
		assertEquals("Wrong Property an Value - lastname user ID",person.getId(),result.getContent().get(0).getId());

		filter.setProperty(IdmIdentity_.email.getName());
		filter.setValue(person.getEmail());
		result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Set Property and Value - email",1, result.getTotalElements());
		assertEquals("Wrong Property an Value - email user ID",person.getId(),result.getContent().get(0).getId());

	}

	@Test
	public void testRoleFilter(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName013","ThisIsTestName013","ThisIsTestName013@gemail.eu", "100000013", false);
		IdmTreeType type = testHelper.createTreeType("ThisIsTestType007");
		IdmTreeNode node = testHelper.createTreeNode(type,"ThisIsTestNode008",null);
		IdmIdentityContractDto contract = testHelper.createIdentityContact(person,node);
		IdmRoleDto somerole = testHelper.createRole();
		testHelper.createIdentityRole(contract,somerole);
		UUID roleId = somerole.getId();
		IdentityFilter filter = new IdentityFilter();
		filter.setRoles(Collections.singletonList(roleId));
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Roles",1, result.getTotalElements());
		assertEquals("Wrong Roles user ID",person.getId(),result.getContent().get(0).getId());
	}

	@Test
	public void testSubordinatesForFilter(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName005","ThisIsTestName005","ThisIsTestName005@gemail.eu", "000000005", false);
		IdmIdentityDto manager = getIdmIdentity("ThisIsTestName006","ThisIsTestName006","ThisIsTestName006@gemail.eu", "000000006", false);
		UUID manager_id = manager.getId();
		IdmTreeType type1 = testHelper.createTreeType("ThisIsTestType001");
		IdmTreeNode node2 = testHelper.createTreeNode(type1,"Somename002",null);
		IdmTreeNode node1 = testHelper.createTreeNode(type1,"ThisIsTestNode001",node2);
		testHelper.createIdentityContact(manager,node2);
		testHelper.createIdentityContact(person,node1);
		//contract.setGuarantee(manager_id);
		//IdmIdentityContractDto contract2 = idmIdentityContractService.save(contract);
		IdentityFilter filter = new IdentityFilter();
		filter.setSubordinatesFor(manager_id);
		filter.setSubordinatesByTreeType(type1.getId());
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong SubordinatesFor",1, result.getTotalElements());
		assertEquals("Wrong SuborganizedFor user ID",person.getId(),result.getContent().get(0).getId());

	}

	@Test
	public void testManagersForFilter(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName009","ThisIsTestName009","ThisIsTestName009@gemail.eu", "000000009", false);
		IdmIdentityDto manager = getIdmIdentity("ThisIsTestName010","ThisIsTestName010","ThisIsTestName010@gemail.eu", "000000010", false);
		UUID person_id = person.getId();
		manager.getId();
		IdmTreeType type1 = testHelper.createTreeType("ThisIsTestType004");
		IdmTreeNode node2 = testHelper.createTreeNode(type1,"Somename001",null);
		IdmTreeNode node1 = testHelper.createTreeNode(type1,"ThisIsTestNode004",node2);
		testHelper.createIdentityContact(manager,node2);
		testHelper.createIdentityContact(person,node1);
		//contract.setGuarantee(manager_id);
		//IdmIdentityContractDto contract2 = idmIdentityContractService.save(contract);
		IdentityFilter filter = new IdentityFilter();
		//filter.setIncludeGuarantees(true);
		filter.setManagersFor(person_id);
		filter.setManagersByTreeType(type1.getId());
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong ManagersFor",1, result.getTotalElements());
		assertEquals("Wrong ManagersFor user ID",manager.getId(),result.getContent().get(0).getId());

	}

	@Test
	public void testIncludeGuaranteesFilter(){
		IdmIdentityDto person = getIdmIdentity("aThisIsTestName009","ThisIsTestName009","ThisIsTestName009@gemail.eu", "000000009", false);
		IdmIdentityDto manager2 = getIdmIdentity("manager009","ThisIsTestName009","ThisIsTestName009@gemail.eu", "000000009", false);
		IdmIdentityDto manager = getIdmIdentity("ThisIsTestName010","ThisIsTestName010","ThisIsTestName010@gemail.eu", "000000010", false);
		IdmTreeType type1 = testHelper.createTreeType("ThisIsTestType0004");
		IdmTreeNode node1 = testHelper.createTreeNode(type1,"ThisIsTestNode0004",null);
		IdmTreeNode node2 = testHelper.createTreeNode(type1,"NextThisIsTestNode0004",node1);
		IdmIdentityContractDto contract = testHelper.createIdentityContact(person,node2);
		testHelper.createContractGuarantee(contract.getId(),manager2.getId());
		testHelper.createIdentityContact(manager,node1);
		IdentityFilter filter = new IdentityFilter();
		filter.setManagersFor(person.getId());
		filter.setIncludeGuarantees(true);
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Managers2For",2, result.getTotalElements());
		filter.setIncludeGuarantees(false);
		result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Managers2For test 2",1, result.getTotalElements());
	}

	@Test
	public void testRecursivelyFilter(){
		// setting employees
		IdmIdentityDto person1 = getIdmIdentity("Klement","Marek","ThisIsTestName011@gemail.eu", "000000011", false);
		IdmIdentityDto person2 = getIdmIdentity("Klement","Marek","ThisIsTestName014@gemail.eu", "000000014", false);
		IdmIdentityDto person3 = getIdmIdentity("Klement","Marek","ThisIsTestName015@gemail.eu", "000000015", false);
		IdmIdentityDto person4 = getIdmIdentity("Klement","Marek","ThisIsTestName016@gemail.eu", "000000016", false);
		// setting structure
		IdmTreeType type1 = testHelper.createTreeType("ThisIsTestType005x");
		IdmTreeType type2 = testHelper.createTreeType("ThisIsTestType006x");
		/*
		      r1  o
		         /
		     n1 o
		       /
	       n2 o
		 */
		IdmTreeNode node1 = testHelper.createTreeNode(type1,"ThisIsTestNode005",null);
		IdmTreeNode node11 = testHelper.createTreeNode(type1,"ThisIsTestNode006",node1);
		IdmTreeNode node12 = testHelper.createTreeNode(type2,"ThisIsTestNode007",node11);
		/*
		    r2  o
		 */
		IdmTreeNode node2 = testHelper.createTreeNode(type2,"ThisIsTestNode008",null);
		// contracts
		testHelper.createIdentityContact(person1,node1);
		testHelper.createIdentityContact(person2,node11);
		testHelper.createIdentityContact(person3,node12);
		testHelper.createIdentityContact(person4,node2);
		// node1 UUID
		UUID node1id = node1.getId();
		// test
		IdentityFilter filter = new IdentityFilter();
		filter.setFirstName(person1.getFirstName());
		filter.setRecursively(true);
		filter.setTreeNode(node1id);
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong Recursive firstname",3, result.getTotalElements());
		filter.setRecursively(false);
		result = idmIdentityService.find(filter, null);
		assertEquals("Wrong NonRecursive firstname",1, result.getTotalElements());
	}

	@Test
	public void testTreeTypeFilter(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName007","ThisIsTestName007","ThisIsTestName007@gemail.eu", "000000007", false);
		IdmTreeType type = testHelper.createTreeType("ThisIsTestType002");
		IdmTreeNode node = testHelper.createTreeNode(type,"ThisIsTestNode002",null);
		UUID typeUuid = type.getId();
		testHelper.createIdentityContact(person,node);
		IdentityFilter filter = new IdentityFilter();
		filter.setTreeType(typeUuid);
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong TreeType",1, result.getTotalElements());
	}

	@Test
	public void testTreeNodeFilter(){
		IdmIdentityDto person = getIdmIdentity("ThisIsTestName008","ThisIsTestName008","ThisIsTestName008@gemail.eu", "000000008", false);
		IdmTreeType type = testHelper.createTreeType("ThisIsTestType003");
		IdmTreeNode node = testHelper.createTreeNode(type,"ThisIsTestNode003",null);
		UUID nodeUuid = node.getId();
		testHelper.createIdentityContact(person,node);
		IdentityFilter filter = new IdentityFilter();
		filter.setTreeNode(nodeUuid);
		filter.setRecursively(false);
		Page<IdmIdentityDto> result = idmIdentityService.find(filter, null);
		assertEquals("Wrong TreeNode",1, result.getTotalElements());
	}

	@Test
	public void testNothingToFind(){
		IdentityFilter filter = new IdentityFilter();
		filter.setFirstName("Adolf");
		Page<IdmIdentityDto> result = idmIdentityService.find(filter,null);
		assertEquals("Wrong blank filter",0, result.getTotalElements());
	}

	@Test
	public void testMoreToFindInFilter(){
		IdmIdentityDto person1 = getIdmIdentity("tn00","tn00","tn01@a.eu", "000000001", false);
		getIdmIdentity("tn00","tn00","tn01@a.eu", "000000001", false);
		getIdmIdentity("tn00","tn00","tn01@a.eu", "000000001", false);
		getIdmIdentity("tn001","tn00","tn01@a.eu", "000000001", false);

		IdentityFilter filter = new IdentityFilter();
		filter.setFirstName(person1.getFirstName());
		Page<IdmIdentityDto> result = idmIdentityService.find(filter,null);
		assertEquals("Wrong more to find filter",3, result.getTotalElements());
	}

	@Test
	public void testMoreFilters(){
		IdmIdentityDto person1 = getIdmIdentity("tn01","tn01","tn02@a.eu", "000010001", false);
		IdmIdentityDto person2 = getIdmIdentity("tn01","tn01","tn02@a.eu", "000010001", false);
		IdmIdentityDto person3 = getIdmIdentity("tn01","tn01","tn02@a.eu", "000010001", false);
		getIdmIdentity("tn0001","tn01","tn02@a.eu", "000010001", false);
		IdentityFilter filter = new IdentityFilter();
		filter.setFirstName(person1.getFirstName());
		filter.setLastName(person2.getLastName());
		filter.setDisabled(false);
		Page<IdmIdentityDto> result = idmIdentityService.find(filter,null);
		assertEquals("Wrong more filter",3, result.getTotalElements());
		filter.setUsername(person3.getUsername());
		result = idmIdentityService.find(filter,null);
		assertEquals("Wrong more filter",1, result.getTotalElements());
		assertEquals("Wrong more filter user ID",person3.getId(),result.getContent().get(0).getId());
	}

	@Test
	public void testSameButDifferentTreeTypeFilter(){
		IdmIdentityDto person1 = getIdmIdentity("thistn01","thistn01","thistn02@a.eu", "000010001", false);
		getIdmIdentity("thistn01","thistn01","thistn02@a.eu", "000010001", false);
		IdmTreeType type1 = testHelper.createTreeType("typeName001");
		IdmTreeType type2 = testHelper.createTreeType("typeName002");
		IdmTreeNode node1 = testHelper.createTreeNode(type1,"nodeName001",null);
		testHelper.createTreeNode(type2,"nodeName002",null);
		testHelper.createIdentityContact(person1,node1);
		testHelper.createIdentityContact(person1,node1);
		IdentityFilter filter = new IdentityFilter();
		filter.setFirstName(person1.getFirstName());
		Page<IdmIdentityDto> result = idmIdentityService.find(filter,null);
		assertEquals("Wrong sameButDiff filter",2, result.getTotalElements());
		filter.setTreeType(type1.getId());
		result = idmIdentityService.find(filter,null);
		assertEquals("Wrong sameButDiff filter 2",1, result.getTotalElements());
	}

	@Test
	public void testTextFilter(){
		IdmIdentityDto person1 = getIdmIdentity("a1testuser","b1testuser","b1testuser@a.eu", "333010001", false);
		IdmIdentityDto person2 = getIdmIdentity("a1testuser","b2testuser","b2testuser@a.eu", "333010003", false);
		IdmIdentityDto person3 = getIdmIdentity("a1testuser","b2testuser","b3testuser@a.eu", "333010004", false);
		IdmIdentityDto person4 = getIdmIdentity("a2testuser","b3testuser","b4testuser@a.eu", "333010005", false);
		IdentityFilter filter = new IdentityFilter();
		filter.setText(person1.getFirstName());
		Page<IdmIdentityDto> result = idmIdentityService.find(filter,null);
		assertEquals("Wrong Text filter - firstname",3, result.getTotalElements());
		filter.setText(person2.getLastName());
		result = idmIdentityService.find(filter,null);
		assertEquals("Wrong Text filter - lastname",2, result.getTotalElements());
		filter.setText(person3.getEmail());
		result = idmIdentityService.find(filter,null);
		assertEquals("Wrong Text filter - email",1, result.getTotalElements());
		filter.setText(person4.getUsername());
		result = idmIdentityService.find(filter,null);
		assertEquals("Wrong Text filter - username",person4.getId(), result.getContent().get(0).getId());
		person1.setDescription("This is describtion");
		IdmIdentityDto person5 = idmIdentityService.save(person1);
		filter.setText(person5.getDescription());
		result = idmIdentityService.find(filter,null);
		assertEquals("Wrong Text filter - describtion",person1.getId(), result.getContent().get(0).getId());
	}

	private IdmIdentityDto getIdmIdentity(String firstName, String lastName, String email, String phone, boolean disabled){
		IdmIdentityDto identity2 = testHelper.createIdentity();
		identity2.setFirstName(firstName);
		identity2.setLastName(lastName);
		identity2.setEmail(email);
		identity2.setDisabled(disabled);
		identity2.setPhone(phone);
		return idmIdentityService.save(identity2);
	}

}
