package eu.bcvsolutions.idm.core.model.repository.filter;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Find subordinates by default alghoritm
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSubordinatesFilterIntegrationTest extends AbstractWorkingPositionFilterIntegrationTest {
	
	@Autowired private IdmIdentityRepository repository;
	private DefaultSubordinatesFilter builder;
	
	@Before
	public void init() {
		super.init();
		builder = AutowireHelper.autowireBean(new DefaultSubordinatesFilter(repository));
	}
	
	@Test
	@Transactional
	public void testSubordinates() {
		prepareData();
		super.testSubordinatesBuilder(builder);
	}

}
