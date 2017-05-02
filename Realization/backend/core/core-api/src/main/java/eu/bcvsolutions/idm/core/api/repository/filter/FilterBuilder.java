package eu.bcvsolutions.idm.core.api.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * Registrable filter - filters will be applied, when property with defined name will be found in filtering parameters.
 * Filter construct partial criteria where clause => {@link Predicate}, which will be appended to query for defined domain type.
 *  
 * @author Radek Tomiška
 * @see DataFilter
 *
 * @param <E> {@link BaseEntity} type - this filter will be applied to this domain type
 * @param <F> {@link BaseFilter} type
 */
public interface FilterBuilder<E extends BaseEntity, F extends BaseFilter> extends Configurable {

	@Override
	default String getConfigurableType() {
		return "filter";
	}
	
	/**
	 * Property in filter - filter will be applied, when property will be set in filtering parameters.
	 * FilterBuilder could read other properties from filter, 
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Filter construct partial criteria where clause => {@link Predicate}, which will be appended to query for defined domain type.
	 * 
	 * @param root
	 * @param query
	 * @param builder
	 * @return
	 */
	Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, F filter);
	
	/**
	 * Finds entities by this filter builder only
	 * 
	 * @param pageable
	 * @return
	 */
	Page<E> find(F filter, Pageable pageable);
}
