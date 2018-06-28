package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Find managers by direct managers
 * 
 * @author Radek Tomiška
 *
 */
public class GuaranteeManagersFilterBuilderIntegrationTest extends AbstractWorkingPositionFilterIntegrationTest {
	
	@Autowired private IdmIdentityRepository repository;
	//
	private GuaranteeManagersFilter builder;
	
	@Before
	public void init() {
		super.init();
		builder = AutowireHelper.autowireBean(new GuaranteeManagersFilter(repository));
	}
	
	@Test
	@Transactional
	public void testManagers() {
		prepareData();
		//
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setManagersFor(subordinateOne.getId());
		filter.setIncludeGuarantees(true);
		List<IdmIdentity> managers = builder.find(filter, null).getContent();
		assertTrue(contains(managers, guaranteeThree));
		assertTrue(contains(managers, guaranteeFour));
		assertEquals(2, managers.size());
		//
		// find contract managers
		filter = new IdmIdentityFilter();
		filter.setManagersFor(subordinateOne.getId());
		filter.setManagersByContract(contractOne.getId());
		managers = builder.find(filter, null).getContent();
		assertEquals(1, managers.size());
		assertTrue(contains(managers, guaranteeThree));
		//
		// find contract managers by without guarantees
		filter = new IdmIdentityFilter();
		filter.setManagersFor(subordinateOne.getId());
		filter.setIncludeGuarantees(false);
		managers = builder.find(filter, null).getContent();
		assertTrue(managers.isEmpty());
		//
		// manager by tree structures
		filter = new IdmIdentityFilter();
		filter.setManagersFor(subordinateOne.getId());
		filter.setManagersByTreeType(structureOne.getId());
		assertTrue(managers.isEmpty());
		//
		// all manager - for subordinate one
		filter = new IdmIdentityFilter();
		filter.setManagersFor(subordinateTwo.getId());
		managers = builder.find(filter, null).getContent();
		assertEquals(1, managers.size());
		assertTrue(contains(managers, guaranteeFour));
	}
}
