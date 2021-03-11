package eu.bcvsolutions.idm.core.bulk.action.impl.contract;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link IdentityChangeContractTreeNodeAndValidityBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class IdentityChangeContractTreeNodeAndValidityBulkActionTest extends AbstractBulkActionTest {
	

	@Autowired
	private IdmIdentityContractService contractService;

	@Before
	public void login() {
		IdmIdentityDto identity = getHelper().createIdentity();
		
		IdmRoleDto createRole = getHelper().createRole();
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITYCONTRACT, IdmIdentityContract.class, IdmBasePermission.UPDATE);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.TREENODE, IdmTreeNode.class, IdmBasePermission.AUTOCOMPLETE);
		
		getHelper().createIdentityRole(identity, createRole);
		loginAsNoAdmin(identity.getUsername());
		//loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testChangeTreeNodeOnly() {
		IdmIdentityDto user = getHelper().createIdentity();
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(user);
		IdmIdentityContractDto contract2 = getHelper().createContract(user);
		LocalDate testFromDate = LocalDate.now().minusMonths(1);
		LocalDate testTillDate = LocalDate.now().plusMonths(1);
		
		IdmTreeNodeDto rootNode = getHelper().createTreeNode(getHelper().createName(), null);
		IdmTreeNodeDto childNode1 = getHelper().createTreeNode(getHelper().createName(), rootNode);
		IdmTreeNodeDto childNode2 = getHelper().createTreeNode(getHelper().createName(), rootNode);
		
		prepareContractState(contract1, childNode1, testFromDate, testTillDate);
		prepareContractState(contract2, childNode2, testFromDate, testTillDate);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityChangeContractTreeNodeAndValidityBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(user));
		bulkAction.setIdentifiers(ids);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityChangeContractTreeNodeAndValidityBulkAction.PARAMETER_TREE_NODE_NAME, rootNode.getId().toString());
		bulkAction.setProperties(properties);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 2l, null, null);
		
		UUID expectedTreeNodeId = rootNode.getId(); 
		IdmIdentityContractDto changedContract = contractService.get(contract1);
		Assert.assertNotNull(changedContract);
		Assert.assertEquals(testFromDate, changedContract.getValidFrom());
		Assert.assertEquals(testTillDate, changedContract.getValidTill());
		Assert.assertEquals(expectedTreeNodeId, changedContract.getWorkPosition());

		changedContract = contractService.get(contract2);
		Assert.assertNotNull(changedContract);
		Assert.assertEquals(testFromDate, changedContract.getValidFrom());
		Assert.assertEquals(testTillDate, changedContract.getValidTill());
		Assert.assertEquals(expectedTreeNodeId, changedContract.getWorkPosition());
	}
	
	@Test
	public void testChangeValidityOnly() {
		IdmIdentityDto user = getHelper().createIdentity();
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(user);
		IdmIdentityContractDto contract2 = getHelper().createContract(user);
		LocalDate testFromDate = LocalDate.now().minusMonths(1);
		LocalDate testTillDate = LocalDate.now().plusMonths(1);
		IdmTreeNodeDto rootNode = getHelper().createTreeNode(getHelper().createName(), null);
		IdmTreeNodeDto childNode1 = getHelper().createTreeNode(getHelper().createName(), rootNode);
		IdmTreeNodeDto childNode2 = getHelper().createTreeNode(getHelper().createName(), rootNode);
		
		prepareContractState(contract1, childNode1, testFromDate, testTillDate);
		prepareContractState(contract2, childNode2, testFromDate, testTillDate);
		LocalDate newFromDate = LocalDate.now().minusYears(1);
		LocalDate newTillDate = LocalDate.now().plusYears(1);
		
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityChangeContractTreeNodeAndValidityBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(user));
		bulkAction.setIdentifiers(ids);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityChangeContractTreeNodeAndValidityBulkAction.PARAMETER_VALID_FROM_NAME, newFromDate.toString());
		properties.put(IdentityChangeContractTreeNodeAndValidityBulkAction.PARAMETER_VALID_TILL_NAME, newTillDate.toString());
		bulkAction.setProperties(properties);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 2l, null, null);
		
		 
		IdmIdentityContractDto changedContract = contractService.get(contract1);
		Assert.assertNotNull(changedContract);
		Assert.assertEquals(newFromDate, changedContract.getValidFrom());
		Assert.assertEquals(newTillDate, changedContract.getValidTill());
		Assert.assertEquals(childNode1.getId(), changedContract.getWorkPosition());

		changedContract = contractService.get(contract2);
		Assert.assertNotNull(changedContract);
		Assert.assertEquals(newFromDate, changedContract.getValidFrom());
		Assert.assertEquals(newTillDate, changedContract.getValidTill());
		Assert.assertEquals(childNode2.getId(), changedContract.getWorkPosition());
	}
	
	@Test
	@Transactional
	public void testChangeWithoutPermissions() {
		IdmIdentityDto user = getHelper().createIdentity();
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(user);
		IdmIdentityContractDto contract2 = getHelper().createContract(user);
		LocalDate testFromDate = LocalDate.now().minusMonths(1);
		LocalDate testTillDate = LocalDate.now().plusMonths(1);
		IdmTreeNodeDto rootNode = getHelper().createTreeNode(getHelper().createName(), null);
		IdmTreeNodeDto childNode1 = getHelper().createTreeNode(getHelper().createName(), rootNode);
		IdmTreeNodeDto childNode2 = getHelper().createTreeNode(getHelper().createName(), rootNode);
		
		prepareContractState(contract1, childNode1, testFromDate, testTillDate);
		prepareContractState(contract2, childNode2, testFromDate, testTillDate);
		LocalDate newFromDate = LocalDate.now().minusYears(1);
		LocalDate newTillDate = LocalDate.now().plusYears(1);
		
		// log as a user without permissions
		IdmIdentityDto identityForLogin = getHelper().createIdentity();
		IdmRoleDto permissionRole = getHelper().createRole();
		//getHelper().createBasePolicy(permissionRole.getId(), CoreGroupPermission.IDENTITYCONTRACT, IdmIdentityContract.class, IdmBasePermission.UPDATE);
		getHelper().createBasePolicy(permissionRole.getId(), CoreGroupPermission.TREENODE, IdmTreeNode.class, IdmBasePermission.AUTOCOMPLETE);
		getHelper().createIdentityRole(identityForLogin, permissionRole);
		loginAsNoAdmin(identityForLogin.getUsername());
		
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityChangeContractTreeNodeAndValidityBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(user));
		bulkAction.setIdentifiers(ids);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityChangeContractTreeNodeAndValidityBulkAction.PARAMETER_VALID_FROM_NAME, newFromDate.toString());
		properties.put(IdentityChangeContractTreeNodeAndValidityBulkAction.PARAMETER_VALID_TILL_NAME, newTillDate.toString());
		bulkAction.setProperties(properties);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, null, null, 2l);
				 
		IdmIdentityContractDto changedContract = contractService.get(contract1);
		Assert.assertNotNull(changedContract);
		Assert.assertEquals(testFromDate, changedContract.getValidFrom());
		Assert.assertEquals(testTillDate, changedContract.getValidTill());
		Assert.assertEquals(childNode1.getId(), changedContract.getWorkPosition());

		changedContract = contractService.get(contract2);
		Assert.assertNotNull(changedContract);
		Assert.assertEquals(testFromDate, changedContract.getValidFrom());
		Assert.assertEquals(testTillDate, changedContract.getValidTill());
		Assert.assertEquals(childNode2.getId(), changedContract.getWorkPosition());
	}
	
	private void prepareContractState(IdmIdentityContractDto contract, IdmTreeNodeDto treeNode, LocalDate fromDate, LocalDate tillDate) {
		contract.setWorkPosition(treeNode.getId());
		contract.setValidFrom(fromDate);
		contract.setValidTill(tillDate);
		contract = contractService.save(contract);
		Assert.assertEquals(treeNode.getId(), contract.getWorkPosition());
		Assert.assertEquals(fromDate, contract.getValidFrom());
		Assert.assertEquals(tillDate, contract.getValidTill());
	}

}
