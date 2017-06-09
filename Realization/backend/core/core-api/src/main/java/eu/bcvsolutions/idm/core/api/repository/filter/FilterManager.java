package eu.bcvsolutions.idm.core.api.repository.filter;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Builds filters for domain types.
 * 
 * @author Radek Tomiška
 *
 */
public interface FilterManager {
	
	/**
	 * Return filter registered / configured for given property name
	 * 
	 * @param entityClass
	 * @param propertyName
	 * @return
	 */
	<E extends BaseEntity> FilterBuilder<E, DataFilter> getBuilder(Class<E> entityClass, String propertyName);

	/**
	 * Returns predicates by registered filters for given BaseEntity root.
	 * 
	 * @param root
	 * @param query
	 * @param builder
	 * @param filter
	 * @return
	 */
	List<Predicate> toPredicates(Root<? extends BaseEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder, DataFilter filter);
}
