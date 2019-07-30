package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;

/**
 * Find subordinate contract by managers by default alghoritm.
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Transactional
public class EavCodeContractByManagerFilterIntegrationTest extends AbstractWorkingPositionFilterIntegrationTest {
	
	@Autowired private IdmIdentityContractRepository repository;
	private EavCodeContractByManagerFilter builder;
	
	@Before
	public void init() {
		builder = AutowireHelper.autowireBean(new EavCodeContractByManagerFilter(repository));
	}
	
	@Test
	public void testSubordinates() {
		prepareData();
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setSubordinatesFor(managerOne.getId());
		filter.setIncludeGuarantees(false);
		List<IdmIdentityContract> subordinates = builder.find(filter, null).getContent();
		assertTrue(contains(subordinates, contractOne));
		assertEquals(1, subordinates.size());
		//
		filter = new IdmIdentityContractFilter();
		filter.setSubordinatesFor(managerOne.getId());
		filter.setIncludeGuarantees(true);
		subordinates = builder.find(filter, null).getContent();
		assertTrue(contains(subordinates, contractOne));
		assertTrue(contains(subordinates, contractThree));
		assertEquals(2, subordinates.size());
		//
		filter = new IdmIdentityContractFilter();
		filter.setSubordinatesFor(managerOne.getId());
		filter.setSubordinatesByTreeType(structureOne.getId());
		subordinates = builder.find(filter, null).getContent();
		assertEquals(1, subordinates.size());
		assertTrue(contains(subordinates, contractOne));
		//
		filter = new IdmIdentityContractFilter();
		filter.setSubordinatesFor(guaranteeFour.getId());
		subordinates = builder.find(filter, null).getContent();
		assertEquals(2, subordinates.size());
		assertTrue(contains(subordinates, contractTwo));
		assertTrue(contains(subordinates, contractSubordinateTwo));
		//
		filter = new IdmIdentityContractFilter();
		filter.setSubordinatesFor(invalidManagerExpiredContract.getId());
		subordinates = builder.find(filter, null).getContent();
		assertTrue(subordinates.isEmpty());
	}
}
