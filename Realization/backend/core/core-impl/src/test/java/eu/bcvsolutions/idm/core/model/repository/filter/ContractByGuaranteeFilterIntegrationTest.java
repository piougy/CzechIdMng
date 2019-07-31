package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Find subordinate contract by managers by default alghoritm.
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Transactional
public class ContractByGuaranteeFilterIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmIdentityContractRepository repository;
	private ContractByGuaranteeFilter builder;
	
	@Before
	public void init() {
		builder = AutowireHelper.autowireBean(new ContractByGuaranteeFilter(repository));
	}
	
	protected FilterBuilder<IdmIdentityContract, IdmIdentityContractFilter> getBuilder() {
		return builder;
	}
	
	@Test
	public void testFindSubordinateContractsByDirectGuarantee() {
		// prepare data - one identity two contract but one manager
		IdmIdentityDto subordinate = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto managerOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto managerTwo = getHelper().createIdentity((GuardedString) null);
		// contracts
		IdmIdentityContractDto contractOne = getHelper().getPrimeContract(subordinate);
		IdmIdentityContractDto contractTwo = getHelper().createIdentityContact(subordinate);
		//
		getHelper().createContractGuarantee(contractOne.getId(), managerOne.getId());
		getHelper().createContractGuarantee(contractTwo.getId(), managerTwo.getId());
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIncludeGuarantees(true);
		filter.setSubordinatesFor(managerOne.getId());
		List<IdmIdentityContract> managedContracts = getBuilder().find(filter, null).getContent();
		Assert.assertEquals(1, managedContracts.size());
		Assert.assertEquals(contractOne.getId(), managedContracts.get(0).getId());
		//
		filter.setSubordinatesFor(managerTwo.getId());
		managedContracts = getBuilder().find(filter, null).getContent();
		Assert.assertEquals(1, managedContracts.size());
		Assert.assertEquals(contractTwo.getId(), managedContracts.get(0).getId());
		//
		filter.setIncludeGuarantees(false);
		managedContracts = getBuilder().find(filter, null).getContent();
		Assert.assertTrue(managedContracts.isEmpty());
	}
}
