package eu.bcvsolutions.idm.core.api.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository;

/**
 * Registrable filter - filters will be applied, when property with defined name will be found in filtering parameters.
 * Filter construct partial criteria where clause => {@link Predicate}, which will be appended to query for defined domain type.
 *
 * Adds default find feature with given repository.
 *
 * @author Radek Tomiška
 * @see DataFilter
 *
 * @param <E> {@link BaseEntity} type - this filter will be applied to this domain type
 * @param <F> {@link DataFilter} type
 */
public abstract class AbstractFilterBuilder<E extends BaseEntity, F extends DataFilter> extends BaseFilterBuilder<E, F> {

	private final BaseEntityRepository<E, ?> repository;

	public AbstractFilterBuilder(BaseEntityRepository<E, ?> repository) {
		Assert.notNull(repository, "Repository is required for filter builder construction.");
		//
		this.repository = repository;
	}

	protected BaseEntityRepository<E, ?> getRepository() {
		return repository;
	}

	/**
	 * Finds entities by this filter builder predicate only.
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<E> find(F filter, Pageable pageable) {
		// transform filter to criteria
		Specification<E> criteria = new Specification<E>() {
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				Predicate predicate = AbstractFilterBuilder.this.getPredicate(root, query, builder, filter);
				if (predicate == null) {
					return query.getRestriction();
				}
				return query.where(predicate).getRestriction();
			}
		};
		if (pageable == null) {
			pageable = PageRequest.of(0, Integer.MAX_VALUE);
		}
		return getRepository().findAll(criteria, pageable);
	}
}
