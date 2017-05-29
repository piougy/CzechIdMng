package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.filter.UuidFilter;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * UuidFilterBuilder test
 * 
 * @author Radek Tomiška
 *
 */
public class UuidFilterBuilderIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityRepository repository;
	
	@Test
	public void testFindIdentityByUuid() {
		// prepare data
		IdmIdentityDto identityOne = helper.createIdentity();
		IdmIdentityDto identityTwo = helper.createIdentity();
		IdmRole roleOne = helper.createRole();
		UuidFilter<IdmIdentity> identityFilter = new FindableUuidFilter<>(repository); 
		//
		DataFilter dataFilter = new DataFilter(IdmIdentityDto.class);
		dataFilter.setId(identityOne.getId());
		List<IdmIdentity> identities = identityFilter.find(dataFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		dataFilter.setId(identityTwo.getId());
		identities = identityFilter.find(dataFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
		//
		dataFilter.setId(roleOne.getId());
		assertEquals(0, identityFilter.find(dataFilter, null).getTotalElements());
	}
	
	private class FindableUuidFilter<E extends AbstractEntity> extends UuidFilter<E> {
		
		private final AbstractEntityRepository<E, ?> repository;
		
		public FindableUuidFilter(AbstractEntityRepository<E, ?> repository) {
			this.repository = repository;
		}
		
		@Override
		public Page<E> find(DataFilter filter, Pageable pageable) {
			// transform filter to criteria
			Specification<E> criteria = new Specification<E>() {
				public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					Predicate predicate = FindableUuidFilter.this.getPredicate(root, query, builder, filter);
					return query.where(predicate).getRestriction();
				}
			};
			return repository.findAll(criteria, pageable);
		}
		
	}

}
