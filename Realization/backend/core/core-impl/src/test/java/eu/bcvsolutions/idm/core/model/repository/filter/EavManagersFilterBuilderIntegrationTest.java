package eu.bcvsolutions.idm.core.model.repository.filter;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Find managers by eav
 * 
 * @author Radek Tomiška
 *
 */
public class EavManagersFilterBuilderIntegrationTest extends AbstractWorkingPositionFilterIntegrationTest {
	
	@Autowired private IdmIdentityRepository repository;
	private EavCodeManagersFilter builder;
	
	@Before
	public void init() {
		super.init();
		builder = AutowireHelper.autowireBean(new EavCodeManagersFilter(repository));
	}
	
	@Test
	@Transactional
	public void testManagers() {
		prepareData();
		super.testManagersBuilder(builder);
	}
	
	
}
