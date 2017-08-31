package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterKey;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Filter builder test:
 * - supports test
 * 
 * @author Radek Tomiška
 *
 */
public class FilterBuilderUnitTest extends AbstractUnitTest {
	
	private static final String FILTER_NAME = "test-one";

	@Test
	public void testSupportIdentityFilterBuilder() {
		FilterBuilder<?, ?> filterBuilder = new TestIdentityFilterBuilder();
		//
		assertTrue(filterBuilder.supports(new FilterKey(IdmIdentity.class, FILTER_NAME)));
		assertFalse(filterBuilder.supports(new FilterKey(IdmIdentity.class, "hoho")));
		assertFalse(filterBuilder.supports(new FilterKey(IdmRole.class, FILTER_NAME)));
	}
	
	@Test
	public void testSupportRoleFilterBuilder() {
		FilterBuilder<?, ?> filterBuilder = new TestRoleFilterBuilder();
		//
		assertFalse(filterBuilder.supports(new FilterKey(IdmIdentity.class, FILTER_NAME)));
		assertFalse(filterBuilder.supports(new FilterKey(IdmRole.class, "hoho")));
		assertTrue(filterBuilder.supports(new FilterKey(IdmRole.class, FILTER_NAME)));
	}
	
	@Test
	public void testSupportUniversalFilterBuilder() {
		FilterBuilder<?, ?> filterBuilder = new TestUniversalFilterBuilder();
		//
		assertTrue(filterBuilder.supports(new FilterKey(IdmIdentity.class, FILTER_NAME)));
		assertFalse(filterBuilder.supports(new FilterKey(IdmIdentity.class, "hoho")));
		assertFalse(filterBuilder.supports(new FilterKey(IdmRole.class, "hoho")));
		assertTrue(filterBuilder.supports(new FilterKey(IdmRole.class, FILTER_NAME)));
	}
	
	private class TestIdentityFilterBuilder extends BaseFilterBuilder<IdmIdentity, IdentityFilter> {
		
		@Override
		public String getName() {
			return FILTER_NAME;
		}

		@Override
		public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder,
				IdentityFilter filter) {
			return null;
		}

		@Override
		public Page<IdmIdentity> find(IdentityFilter filter, Pageable pageable) {
			return null;
		}
		
	}
	
	private class TestRoleFilterBuilder extends BaseFilterBuilder<IdmRole, IdmRoleFilter> {
		
		@Override
		public String getName() {
			return FILTER_NAME;
		}

		@Override
		public Predicate getPredicate(Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder,
				IdmRoleFilter filter) {
			return null;
		}

		@Override
		public Page<IdmRole> find(IdmRoleFilter filter, Pageable pageable) {
			return null;
		}
		
	}
	
	private class TestUniversalFilterBuilder extends BaseFilterBuilder<AbstractEntity, DataFilter> {		
		
		@Override
		public String getName() {
			return FILTER_NAME;
		}

		@Override
		public Predicate getPredicate(Root<AbstractEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder,
				DataFilter filter) {
			return null;
		}

		@Override
		public Page<AbstractEntity> find(DataFilter filter, Pageable pageable) {
			return null;
		}
		
	}
	
}
