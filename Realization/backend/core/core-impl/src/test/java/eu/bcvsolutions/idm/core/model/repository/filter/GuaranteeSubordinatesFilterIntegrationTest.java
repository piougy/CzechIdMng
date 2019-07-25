package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Find subordinates by direct managers.
 * 
 * @author Radek Tomiška
 *
 */
public class GuaranteeSubordinatesFilterIntegrationTest extends AbstractWorkingPositionFilterIntegrationTest {
	
	@Autowired private ApplicationContext context;
	//
	private GuaranteeSubordinatesFilter builder;
	
	@Before
	public void init() {
		super.init();
		//
		builder = context.getAutowireCapableBeanFactory().createBean(GuaranteeSubordinatesFilter.class);
	}
	
	@Test
	@Transactional
	public void testSubordinates() {
		prepareData();
		//
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setSubordinatesFor(managerOne.getId());
		filter.setIncludeGuarantees(false);
		List<IdmIdentity> subordinates = builder.find(filter, null).getContent();
		assertTrue(subordinates.isEmpty());
		//
		filter = new IdmIdentityFilter();
		filter.setSubordinatesFor(managerOne.getId());
		filter.setIncludeGuarantees(true);
		subordinates = builder.find(filter, null).getContent();
		assertTrue(contains(subordinates, subordinateThree));
		assertEquals(1, subordinates.size());
		//
		filter = new IdmIdentityFilter();
		filter.setSubordinatesFor(managerOne.getId());
		filter.setSubordinatesByTreeType(structureOne.getId());
		subordinates = builder.find(filter, null).getContent();
		assertTrue(subordinates.isEmpty());
		//
		filter = new IdmIdentityFilter();
		filter.setSubordinatesFor(invalidManagerExpiredContract.getId());
		subordinates = builder.find(filter, null).getContent();
		assertTrue(subordinates.isEmpty());		
	}

}
