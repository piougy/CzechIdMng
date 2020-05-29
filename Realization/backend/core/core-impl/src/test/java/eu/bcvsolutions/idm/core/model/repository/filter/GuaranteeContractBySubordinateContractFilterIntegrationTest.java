package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;

/**
 * Test filter for find managers' contracts for given subordinate contract.
 * 
 * @author Radek Tomiška
 *
 */
public class GuaranteeContractBySubordinateContractFilterIntegrationTest extends AbstractWorkingPositionFilterIntegrationTest {
	
	@Autowired private IdmIdentityContractRepository repository;
	//
	private GuaranteeContractBySubordinateContractFilter builder;
	
	@Before
	public void init() {
		super.init();
		builder = AutowireHelper.autowireBean(new GuaranteeContractBySubordinateContractFilter(repository));
	}
	
	@Test
	@Transactional
	public void testManagers() {
		prepareData();
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setManagersByContract(contractTwo.getId());
		List<IdmIdentityContract> managers = builder.find(filter, null).getContent();
		Assert.assertTrue(contains(managers, getHelper().getPrimeContract(guaranteeFour)));
		Assert.assertEquals(1, managers.size());
		//
		filter = new IdmIdentityContractFilter();
		filter.setManagersByContract(contractFourDisabled.getId());
		managers = builder.find(filter, null).getContent();
		Assert.assertTrue(contains(managers, getHelper().getPrimeContract(guaranteeFive)));
		Assert.assertEquals(1, managers.size());
		//
		filter = new IdmIdentityContractFilter();
		filter.setManagersByContract(contractFourDisabled.getId());
		filter.setValidContractManagers(Boolean.TRUE);
		managers = builder.find(filter, null).getContent();
		Assert.assertTrue(managers.isEmpty());
	}
}
