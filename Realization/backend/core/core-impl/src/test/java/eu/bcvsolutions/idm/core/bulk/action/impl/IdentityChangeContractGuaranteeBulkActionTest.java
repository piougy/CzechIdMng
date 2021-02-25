package eu.bcvsolutions.idm.core.bulk.action.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link IdentityChangeContractGuaranteeBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class IdentityChangeContractGuaranteeBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;

	@Before
	public void login() {
		/* TODO prava
		IdmIdentityDto identity = getHelper().createIdentity();
		
		IdmRoleDto createRole = getHelper().createRole();
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.READ, IdentityBasePermission.CHANGEPERMISSION);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITYCONTRACT, IdmIdentityContract.class, IdmBasePermission.AUTOCOMPLETE);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.ROLEREQUEST, IdmRoleRequest.class, IdmBasePermission.ADMIN);
		
		getHelper().createIdentityRole(identity, createRole);
		loginAsNoAdmin(identity.getUsername());
		*/
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void processByIdsChangeSelected() {
		List<IdmIdentityDto> oldGuarantees = this.createIdentities(2);
		List<IdmIdentityDto> newGuarantees = this.createIdentities(1);
		IdmIdentityDto employee = getHelper().createIdentity();
		
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(employee);
		IdmIdentityContractDto contract2 = getHelper().createContract(employee);
		
		createContractGuarantees(contract1, oldGuarantees);
		createContractGuarantees(contract2, oldGuarantees);
		Assert.assertTrue(isContractGuarantee(contract1, oldGuarantees).isEmpty());		
		Assert.assertTrue(isContractGuarantee(contract2, oldGuarantees).isEmpty());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityChangeContractGuaranteeBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(employee));
		bulkAction.setIdentifiers(ids);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityAddContractGuaranteeBulkAction.OLD_GUARANTEE, oldGuarantees.get(0).getId().toString());
		properties.put(IdentityAddContractGuaranteeBulkAction.NEW_GUARANTEE, newGuarantees.get(0).getId().toString());
		bulkAction.setProperties(properties);
		bulkActionManager.processAction(bulkAction);
		//checkResultLrt(processAction, 1l, null, null);  // has to be commented out because 

		// test that there remains on both contracts only one guarantee 
		// CONTRACT1
		List<IdmContractGuaranteeDto> assigned = getGuaranteesForContract(contract1.getId());
		Assert.assertEquals(2, assigned.size());
		List<IdmIdentityDto> expectedDto = new ArrayList<IdmIdentityDto>(oldGuarantees.subList(1, 2));
		expectedDto.addAll(newGuarantees);
		Assert.assertTrue(isContractGuarantee(contract1, expectedDto).isEmpty());
		
		
		// CONTRACT2
		assigned = getGuaranteesForContract(contract2.getId());
		Assert.assertEquals(2, assigned.size());
		expectedDto = new ArrayList<IdmIdentityDto>(oldGuarantees.subList(1, 2));
		expectedDto.addAll(newGuarantees);
		Assert.assertTrue(isContractGuarantee(contract2, expectedDto).isEmpty());
	}
	
	
	// Test that there are changed only contracts which contained at least one old guarantee.
	
	@Test
	public void changeWhereContractContainsOldGuarantee() {
		List<IdmIdentityDto> oldGuarantees = this.createIdentities(2);
		List<IdmIdentityDto> newGuarantees = this.createIdentities(1);
		IdmIdentityDto employee = getHelper().createIdentity();
		
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(employee);
		IdmIdentityContractDto contract2 = getHelper().createContract(employee);
		
		createContractGuarantees(contract1, oldGuarantees.subList(0, 1));
		createContractGuarantees(contract2, oldGuarantees.subList(1, oldGuarantees.size()));
		Assert.assertTrue(isContractGuarantee(contract1, oldGuarantees.subList(0, 1)).isEmpty());		
		Assert.assertTrue(isContractGuarantee(contract2, oldGuarantees.subList(1, oldGuarantees.size())).isEmpty());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityChangeContractGuaranteeBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(employee));
		bulkAction.setIdentifiers(ids);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityAddContractGuaranteeBulkAction.OLD_GUARANTEE, oldGuarantees.get(0).getId().toString());
		properties.put(IdentityAddContractGuaranteeBulkAction.NEW_GUARANTEE, newGuarantees.get(0).getId().toString());
		bulkAction.setProperties(properties);
		bulkActionManager.processAction(bulkAction);
		//checkResultLrt(processAction, 1l, null, null);

		// test that there remains on both contracts only one guarantee 
		// CONTRACT1
		List<IdmContractGuaranteeDto> assigned = getGuaranteesForContract(contract1.getId());
		Assert.assertEquals(1, assigned.size());
		List<IdmIdentityDto> expectedDto = new ArrayList<IdmIdentityDto>(newGuarantees);
		Assert.assertTrue(isContractGuarantee(contract1, expectedDto).isEmpty());
		
		
		// CONTRACT2
		assigned = getGuaranteesForContract(contract2.getId());
		Assert.assertEquals(1, assigned.size());
		// old guarantee stayed added
		expectedDto = new ArrayList<IdmIdentityDto>(oldGuarantees.subList(1, 2));
		Assert.assertTrue(isContractGuarantee(contract2, expectedDto).isEmpty());
		// newly set guarantee is not applied
		expectedDto = new ArrayList<IdmIdentityDto>(newGuarantees);
		Assert.assertEquals(1, isContractGuarantee(contract2, expectedDto).size());
	}
	
	
	@Test
	public void ifMultipleSameGuaranteesReplaceAll() {
		List<IdmIdentityDto> oldGuarantees = this.createIdentities(1);
		List<IdmIdentityDto> newGuarantees = this.createIdentities(1);
		IdmIdentityDto employee = getHelper().createIdentity();
		
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(employee);
		
		createContractGuarantees(contract1, oldGuarantees);
		createContractGuarantees(contract1, oldGuarantees); // repeated assignment
		Assert.assertTrue(isContractGuarantee(contract1, oldGuarantees).isEmpty());		
		Assert.assertEquals(2, getGuaranteesForContract(contract1.getId()).size());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityChangeContractGuaranteeBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(employee));
		bulkAction.setIdentifiers(ids);
		
		// selected identityDtos
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityAddContractGuaranteeBulkAction.OLD_GUARANTEE, String.valueOf(oldGuarantees.get(0).getId()));
		properties.put(IdentityAddContractGuaranteeBulkAction.NEW_GUARANTEE, String.valueOf(newGuarantees.get(0).getId()));
		bulkAction.setProperties(properties);
		bulkActionManager.processAction(bulkAction);
//		checkResultLrt(processAction, 1l, null, null);

		// test that there remains only one guarantee
		// both instances of the twice added guarantee were removed
		List<IdmContractGuaranteeDto> assigned = getGuaranteesForContract(contract1.getId());
		Assert.assertEquals(1, assigned.size());
		Assert.assertEquals(newGuarantees.get(0).getId(), assigned.get(0).getGuarantee());
	}
	

	
	/**
	 * Assigns all identities as guarantees for specified contract
	 * 
	 * @param contract
	 * @param guarantees
	 */
	private void createContractGuarantees(IdmIdentityContractDto contract, List<IdmIdentityDto> guarantees) {
		guarantees.forEach(guarantee -> {
			getHelper().createContractGuarantee(contract, guarantee);
		});
	}
	
	/**
	 * Gets all contractGuarantees for contract 
	 * @param contract
	 * @return
	 */
	private List<IdmContractGuaranteeDto> getGuaranteesForContract(UUID contract) {
		IdmContractGuaranteeFilter filt = new IdmContractGuaranteeFilter();
		filt.setIdentityContractId(contract);
		return contractGuaranteeService.find(filt, null).getContent();
	}
	
	/**
	 * Tests if there are all identities contract guarantees
	 * If some of them are not contract guarantees, they are returned
	 * Empty returned List means that there are all identities contract guarantees 
	 * 
	 * @param contract
	 * @param guarantees
	 * @return
	 */
	private List<IdmIdentityDto> isContractGuarantee(IdmIdentityContractDto contract, List<IdmIdentityDto> guarantees) {
		IdmContractGuaranteeFilter filt = new IdmContractGuaranteeFilter();
		filt.setIdentityContractId(contract.getId());
		Set<UUID> cgUUIDs = contractGuaranteeService.find(filt, null)
				.getContent().stream()
				.map(IdmContractGuaranteeDto::getGuarantee)
				.collect(Collectors.toSet());
		return guarantees.stream()
				.filter(ident -> !cgUUIDs.contains(ident.getId()))
				.collect(Collectors.toList());
	}
	
}
