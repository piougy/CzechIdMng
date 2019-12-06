package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Find subordinate contract by managers by default alghoritm.
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Transactional
public class DefaultContractByManagerFilterIntegrationTest extends ContractByGuaranteeFilterIntegrationTest {
	
	@Autowired private IdmIdentityContractRepository repository;
	private DefaultContractByManagerFilter builder;
	
	@Before
	public void init() {
		builder = AutowireHelper.autowireBean(new DefaultContractByManagerFilter(repository));
	}
	
	@Override
	protected FilterBuilder<IdmIdentityContract, IdmIdentityContractFilter> getBuilder() {
		return builder;
	}
	
	@Test
	public void testFindSubordinateContractsByTree() {
		// prepare data - one identity two contract but one manager
		IdmIdentityDto subordinate = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto managerOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto managerTwo = getHelper().createIdentity((GuardedString) null);
		// other contracts
		IdmIdentityContractDto contractOther = getHelper().getPrimeContract(subordinate);
		getHelper().createContractGuarantee(contractOther.getId(), managerOne.getId());
		//
		IdmTreeTypeDto structure = getHelper().createTreeType();
		IdmTreeNodeDto managerOnePosition = getHelper().createTreeNode(structure, null); 
		getHelper().createIdentityContact(managerOne, managerOnePosition);
		IdmTreeNodeDto managerTwoPosition = getHelper().createTreeNode(structure, null); 
		getHelper().createIdentityContact(managerTwo, managerTwoPosition);
		//
		IdmTreeNodeDto subordinateOnePositionOne = getHelper().createTreeNode(structure, managerOnePosition);
		IdmTreeNodeDto subordinateOnePositionTwo = getHelper().createTreeNode(structure, managerTwoPosition);
		IdmIdentityContractDto contractOne = getHelper().createIdentityContact(subordinate, subordinateOnePositionOne);
		IdmIdentityContractDto contractTwo = getHelper().createIdentityContact(subordinate, subordinateOnePositionTwo);
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIncludeGuarantees(false);
		filter.setSubordinatesFor(managerOne.getId());
		List<IdmIdentityContract> managedContracts = builder.find(filter, null).getContent();
		Assert.assertEquals(1, managedContracts.size());
		Assert.assertEquals(contractOne.getId(), managedContracts.get(0).getId());
		//
		filter.setSubordinatesFor(managerTwo.getId());
		managedContracts = builder.find(filter, null).getContent();
		Assert.assertEquals(1, managedContracts.size());
		Assert.assertEquals(contractTwo.getId(), managedContracts.get(0).getId());
		//
		filter.setIncludeGuarantees(true);
		filter.setSubordinatesFor(managerOne.getId());
		managedContracts = builder.find(filter, null).getContent();
		Assert.assertEquals(2, managedContracts.size());
		Assert.assertTrue(managedContracts.stream().anyMatch(c -> c.getId().equals(contractOther.getId())));
		Assert.assertTrue(managedContracts.stream().anyMatch(c -> c.getId().equals(contractOne.getId())));
	}
}
