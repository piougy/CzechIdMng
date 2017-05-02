package eu.bcvsolutions.idm.core.api.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Registrable filter - filters will be applied, when property with defined name will be found in filtering parameters.
 * Filter construct partial criteria where clause => {@link Predicate}, which will be appended to query for defined domain type.
 * 
 * TODO: move @Autowire to @Configuration bean post processor
 *  
 * @author Radek Tomiška
 * @see DataFilter
 *
 * @param <E> {@link BaseEntity} type - this filter will be applied to this domain type
 * @param <F> {@link BaseFilter} type
 */
public abstract class AbstractFilterBuilder<E extends BaseEntity, F extends BaseFilter> implements FilterBuilder<E, F> {

	private final BaseEntityRepository<E, ?, F> repository;
	
	@Autowired(required = false)
	private ConfigurationService configurationService; // optional internal dependency - checks for processor is enabled
	
	public AbstractFilterBuilder(BaseEntityRepository<E, ?, F> repository) {
		Assert.notNull(repository);
		//
		this.repository = repository;
	}
	
	protected BaseEntityRepository<E, ?, F> getRepository() {
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
			public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				Predicate predicate = AbstractFilterBuilder.this.getPredicate(root, query, builder, filter);
				return query.where(predicate).getRestriction();
			}
		};
		return getRepository().findAll(criteria, pageable);
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
}
