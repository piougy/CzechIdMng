package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;

/**
 * Test filter for find managers' contracts for given subordinate contract.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultManagerContractBySubordinateContractFilterIntegrationTest extends AbstractWorkingPositionFilterIntegrationTest {
	
	@Autowired private IdmIdentityContractRepository repository;
	//
	private DefaultManagerContractBySubordinateContractFilter builder;
	
	@Before
	public void init() {
		super.init();
		builder = AutowireHelper.autowireBean(new DefaultManagerContractBySubordinateContractFilter(repository));
	}
	
	protected FilterBuilder<IdmIdentityContract, IdmIdentityContractFilter> getBuilder() {
		return builder;
	}
	
	@Test
	@Transactional
	public void testManagers() {
		prepareData();
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		//
		// tests
		// all managers - for subordinate one
		filter.setManagersByContract(contractTwo.getId());
		filter.setIncludeGuarantees(true);
		List<IdmIdentityContract> managers = getBuilder().find(filter, null).getContent();
		Assert.assertTrue(contains(managers, managerTwoContract));
		Assert.assertTrue(contains(managers, getHelper().getPrimeContract(guaranteeFour)));
		assertEquals(2, managers.size());
		//
		filter = new IdmIdentityContractFilter();
		filter.setManagersByContract(contractTwo.getId());
		filter.setIncludeGuarantees(false);
		managers = getBuilder().find(filter, null).getContent();
		Assert.assertTrue(contains(managers, managerTwoContract));
		assertEquals(1, managers.size());
		//
		filter = new IdmIdentityContractFilter();
		filter.setManagersByContract(contractFourDisabled.getId());
		managers = getBuilder().find(filter, null).getContent();
		Assert.assertTrue(contains(managers, managerThreeContract));
		Assert.assertTrue(contains(managers, getHelper().getPrimeContract(guaranteeFive)));
		Assert.assertEquals(2, managers.size());
		//
		filter = new IdmIdentityContractFilter();
		filter.setManagersByContract(contractFourDisabled.getId());
		filter.setValidContractManagers(Boolean.TRUE);
		managers = getBuilder().find(filter, null).getContent();
		Assert.assertTrue(managers.isEmpty());
	}

}
