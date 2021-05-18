package eu.bcvsolutions.idm.core.rest.impl;

import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityContractControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmIdentityContractDto> {

	@Autowired private IdmIdentityContractController controller;
	@Autowired private IdmIdentityContractService contractService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIdentityContractDto, ?> getController() {
		return controller;
	}
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		//
		getHelper().setConfigurationValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, Boolean.FALSE);
	}

	@After
	@Override
	public void logout() {
		super.logout();
		//
		getHelper().setConfigurationValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, Boolean.TRUE);
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}

	@Override
	protected IdmIdentityContractDto prepareDto() {
		IdmIdentityContractDto dto = new IdmIdentityContractDto();
		dto.setIdentity(getHelper().createIdentity().getId());
		//
		return dto;
	}
	
	@Test
	public void testFindByRole() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractOne = getHelper().createContract(identity);
		IdmIdentityContractDto contractTwo = getHelper().createContract(identity);
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		getHelper().createIdentityRole(contractOne, roleOne);
		getHelper().createIdentityRole(contractTwo, roleTwo);
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIdentity(identity.getId());
		filter.setRoleId(roleOne.getId());
		List<IdmIdentityContractDto> contracts = find(filter);
		Assert.assertEquals(1, contracts.size());
		Assert.assertEquals(contractOne.getId(), contracts.get(0).getId());
		//
		filter.setRoleId(roleTwo.getId());
		contracts = find(filter);
		Assert.assertEquals(1, contracts.size());
		Assert.assertEquals(contractTwo.getId(), contracts.get(0).getId());
	}
	
	@Test
	public void testSortByPrimeContract() {
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityThree = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contractOther = getHelper().createContract(identityTwo); // other contract jus for sure
		//
		IdmIdentityContractDto contract = getHelper().createContract(identityOne);
		contract.setValidFrom(LocalDate.now().minusDays(5));
		contract.setState(ContractState.DISABLED);
		contract.setMain(true);
		IdmIdentityContractDto contractOne = contractService.save(contract);
		//
		contract = getHelper().createContract(identityOne);
		contract.setValidFrom(LocalDate.now().minusDays(4));
		contractService.save(contract); // two
		//
		contract = getHelper().createContract(identityOne);
		contract.setValidFrom(LocalDate.now().minusDays(3));
		contractService.save(contract); // three
		//
		contract = getHelper().createContract(identityOne);
		contract.setMain(true);
		contract.setValidFrom(LocalDate.now().minusDays(2));
		IdmIdentityContractDto primeContract = contractService.save(contract);
		// 
		List<IdmIdentityContractDto> contracts = controller.find((IdmIdentityContractFilter) null, PageRequest.of(0, 1), null).getContent();
		Assert.assertFalse(contracts.isEmpty());
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		contracts = controller.find((IdmIdentityContractFilter) null, PageRequest.of(0, 1), null).getContent();
		Assert.assertFalse(contracts.isEmpty());
		//
		filter.setIdentity(identityOne.getId());
		contracts = controller.find(filter, PageRequest.of(0, 5), null).getContent();
		Assert.assertEquals(4, contracts.size());
		Assert.assertTrue(contracts.stream().allMatch(c -> !c.getId().equals(contractOther.getId())));
		Assert.assertEquals(primeContract.getId(), contracts.get(0).getId());
		//
		filter.setIdentity(identityThree.getId());
		contracts = controller.find(filter, PageRequest.of(0, 5), null).getContent();
		Assert.assertTrue(contracts.isEmpty());
		//
		filter.setIdentity(identityOne.getId());
		contracts = controller.find(filter, PageRequest.of(0, 5, Sort.by(Direction.ASC, IdmIdentityContract_.validFrom.getName())), null).getContent();
		Assert.assertEquals(4, contracts.size());
		Assert.assertEquals(contractOne.getId(), contracts.get(0).getId());
	}
}
