package eu.bcvsolutions.idm.core.model.repository.filter;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Find managers by default alghoritm
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultManagersFilterBuilderIntegrationTest extends AbstractWorkingPositionFilterIntegrationTest {
	
	@Autowired private IdmIdentityRepository repository;
	private DefaultManagersFilter builder;
	
	@Before
	public void init() {
		super.init();
		builder = AutowireHelper.autowireBean(new DefaultManagersFilter(repository));
	}
	
	@Test
	@Transactional
	public void testManagers() {
		prepareData();
		super.testManagersBuilder(builder);
	}
}
