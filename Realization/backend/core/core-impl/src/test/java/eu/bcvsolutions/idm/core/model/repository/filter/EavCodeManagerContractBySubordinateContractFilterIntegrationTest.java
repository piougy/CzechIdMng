package eu.bcvsolutions.idm.core.model.repository.filter;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

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
public class EavCodeManagerContractBySubordinateContractFilterIntegrationTest 
		extends DefaultManagerContractBySubordinateContractFilterIntegrationTest {
	
	@Autowired private IdmIdentityContractRepository repository;
	//
	private EavCodeManagerContractBySubordinateContractFilter builder;
	
	@Before
	public void init() {
		super.init();
		builder = AutowireHelper.autowireBean(new EavCodeManagerContractBySubordinateContractFilter(repository));
	}
	
	@Override
	protected FilterBuilder<IdmIdentityContract, IdmIdentityContractFilter> getBuilder() {
		return builder;
	}

}
