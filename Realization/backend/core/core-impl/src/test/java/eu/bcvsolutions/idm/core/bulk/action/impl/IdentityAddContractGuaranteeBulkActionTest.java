package eu.bcvsolutions.idm.core.bulk.action.impl;


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
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link IdentityAddContractGuaranteeBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class IdentityAddContractGuaranteeBulkActionTest extends AbstractBulkActionTest {

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
	public void processByIdsNoGuaranteesMoreContracts() {
		List<IdmIdentityDto> guarantees = this.createIdentities(3);
		IdmIdentityDto employee = getHelper().createIdentity();
		
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(employee);
		IdmIdentityContractDto contract2 = getHelper().createContract(employee);
		List<IdmIdentityContractDto> contracts = Arrays.asList(contract1, contract2);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityAddContractGuaranteeBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(employee));
		bulkAction.setIdentifiers(ids);
		
		Map<String, Object> properties = new HashMap<>();
		List<String> uuidStrings = guarantees.stream().map(AbstractDto::getId).map(Object::toString).collect(Collectors.toList());
		properties.put(IdentityAddContractGuaranteeBulkAction.NEW_GUARANTEE, uuidStrings);
		bulkAction.setProperties(properties);
		bulkActionManager.processAction(bulkAction);
		//checkResultLrt(processAction, 1l, null, null);

		// test guarantes on all contracts
		for (IdmIdentityContractDto contract : contracts) {
			// expected number of guarantees
			List<IdmContractGuaranteeDto> assigned = getGuaranteesForContract(contract.getId());
			Assert.assertEquals(guarantees.size(), assigned.size());
			// none of expected guarantees are missing in created cintractGuarantee bindings
			Assert.assertTrue(isContractGuarantee(contract, guarantees).isEmpty());
		}
	}
	
	@Test
	public void processByIdsGuaranteeNotSetTwice() {
		List<IdmIdentityDto> guarantees = this.createIdentities(1);
		IdmIdentityDto employee = getHelper().createIdentity();
		IdmIdentityContractDto contract1 = getHelper().getPrimeContract(employee);
		// set guarantee before reassigned by BA
		createContractGuarantees(contract1, guarantees);
		Assert.assertTrue(isContractGuarantee(contract1, guarantees).isEmpty());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityAddContractGuaranteeBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(Arrays.asList(employee));
		bulkAction.setIdentifiers(ids);
		
		Map<String, Object> properties = new HashMap<>();
		List<String> uuidStrings = guarantees.stream().map(AbstractDto::getId).map(Object::toString).collect(Collectors.toList());
		properties.put(IdentityAddContractGuaranteeBulkAction.NEW_GUARANTEE, uuidStrings);
		bulkAction.setProperties(properties);
		bulkActionManager.processAction(bulkAction);
		//checkResultLrt(processAction, 1l, null, null);

		// test guarantes on all contracts
		List<IdmContractGuaranteeDto> assigned = getGuaranteesForContract(contract1.getId());
		// same guarantee is assigned only once
		Assert.assertEquals(guarantees.size(), assigned.size());
		Set<UUID> assignedGuarUUID = assigned.stream().map(IdmContractGuaranteeDto::getGuarantee).collect(Collectors.toSet());
		guarantees.forEach(guarantee -> {
			Assert.assertTrue(assignedGuarUUID.contains(guarantee.getId()));
		});
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
