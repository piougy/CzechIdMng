package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link IdentityRemoveContractGuaranteeBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class IdentityRemoveContractGuaranteeBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;

	@Before
	public void login() {
		IdmIdentityDto identity = getHelper().createIdentity();
		
		IdmRoleDto createRole = getHelper().createRole();
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.READ, IdmBasePermission.COUNT, IdmBasePermission.AUTOCOMPLETE);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.CONTRACTGUARANTEE, IdmContractGuarantee.class, IdmBasePermission.DELETE);
		
		getHelper().createIdentityRole(identity, createRole);
		loginAsNoAdmin(identity.getUsername());
		//loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void processByIdsRemoveSelected() {
		List<IdmIdentityDto> guarantees = this.createIdentities(4);
		IdmIdentityDto employee = getHelper().createIdentity();
		
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(employee);
		IdmIdentityContractDto contract2 = getHelper().createContract(employee);
		
		createContractGuarantees(contract1, guarantees.subList(0, guarantees.size()-1));
		createContractGuarantees(contract2, guarantees.subList(1, guarantees.size()));
		Assert.assertTrue(isContractGuarantee(contract1, guarantees.subList(0, guarantees.size()-1)).isEmpty());		
		Assert.assertTrue(isContractGuarantee(contract2, guarantees.subList(1, guarantees.size())).isEmpty());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRemoveContractGuaranteeBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(employee));
		bulkAction.setIdentifiers(ids);
		
		// selected identityDtos
		List<String> uuidStrings = guarantees.subList(1, 3).stream().map(AbstractDto::getId).map(Object::toString).collect(Collectors.toList());
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityAddContractGuaranteeBulkAction.OLD_GUARANTEE, uuidStrings);
		bulkAction.setProperties(properties);
		bulkActionManager.processAction(bulkAction);
		//checkResultLrt(processAction, 1l, null, null);

		// test that there remains on both contracts only one guarantee  
		List<IdmContractGuaranteeDto> assigned = getGuaranteesForContract(contract1.getId());
		Assert.assertEquals(1, assigned.size());
		Assert.assertEquals(guarantees.get(0).getId(), assigned.get(0).getGuarantee());
		
		assigned = getGuaranteesForContract(contract2.getId());
		Assert.assertEquals(1, assigned.size());
		Assert.assertEquals(guarantees.get(3).getId(), assigned.get(0).getGuarantee());
	}
	
	
	@Test
	public void ifMultipleSameGuaranteesRemoveAll() {
		List<IdmIdentityDto> guarantees = this.createIdentities(4);
		IdmIdentityDto employee = getHelper().createIdentity();
		
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(employee);
		
		createContractGuarantees(contract1, guarantees.subList(0, guarantees.size()));
		createContractGuarantees(contract1, guarantees.subList(0, 1)); // first guarantee assigned twice - possible from UI
		Assert.assertEquals(guarantees.size()+1,getGuaranteesForContract(contract1.getId()).size());		
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRemoveContractGuaranteeBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(employee));
		bulkAction.setIdentifiers(ids);
		
		// selected identityDtos
		List<String> uuidStrings = guarantees.subList(0, guarantees.size()-1).stream().map(AbstractDto::getId).map(Object::toString).collect(Collectors.toList());
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityAddContractGuaranteeBulkAction.OLD_GUARANTEE, uuidStrings);
		bulkAction.setProperties(properties);
		bulkActionManager.processAction(bulkAction);
		//checkResultLrt(processAction, 1l, null, null);

		// test that there remains only one guarantee
		// both instances of the twice added guarantee were removed
		List<IdmContractGuaranteeDto> assigned = getGuaranteesForContract(contract1.getId());
		Assert.assertEquals(1, assigned.size());
		Assert.assertEquals(guarantees.get(guarantees.size()-1).getId(), assigned.get(0).getGuarantee());
	}
	
	@Test
	@Transactional
	public void withoutPermissionDeleteGuarantee() {
		List<IdmIdentityDto> guarantees = this.createIdentities(1);
		IdmIdentityDto employee = getHelper().createIdentity();
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(employee);
		Assert.assertEquals(0, getGuaranteesForContract(contract1.getId()).size());

		// init guarantee to delete
		createContractGuarantees(contract1, guarantees);
		Assert.assertEquals(guarantees.size(), getGuaranteesForContract(contract1.getId()).size());
		Assert.assertTrue(isContractGuarantee(contract1, guarantees).isEmpty());
		
		// Log as user without delete permission
		IdmIdentityDto identityForLogin = getHelper().createIdentity();
		IdmRoleDto permissionRole = getHelper().createRole();
		getHelper().createBasePolicy(permissionRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.READ, IdmBasePermission.COUNT);
		//getHelper().createBasePolicy(permissionRole.getId(), CoreGroupPermission.CONTRACTGUARANTEE, IdmContractGuarantee.class, IdmBasePermission.DELETE);
		getHelper().createIdentityRole(identityForLogin, permissionRole);
		loginAsNoAdmin(identityForLogin.getUsername());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRemoveContractGuaranteeBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(employee));
		bulkAction.setIdentifiers(ids);
		
		Map<String, Object> properties = new HashMap<>();
		List<String> uuidStrings = guarantees.stream().map(AbstractDto::getId).map(Object::toString).collect(Collectors.toList());
		properties.put(IdentityAddContractGuaranteeBulkAction.OLD_GUARANTEE, uuidStrings);
		bulkAction.setProperties(properties);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, null, 0l, 1l);

		// test guarantes on all contracts
		List<IdmContractGuaranteeDto> assigned = getGuaranteesForContract(contract1.getId());
		Assert.assertEquals(guarantees.size(), assigned.size());
		Assert.assertTrue(isContractGuarantee(contract1, guarantees).isEmpty());
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
