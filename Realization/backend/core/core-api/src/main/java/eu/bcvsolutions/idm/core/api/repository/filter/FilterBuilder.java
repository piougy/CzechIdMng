package eu.bcvsolutions.idm.core.api.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.core.Ordered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.Plugin;

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
 * @param <F> {@link DataFilter} type
 */
public interface FilterBuilder<E extends BaseEntity, F extends DataFilter> extends Configurable, Plugin<FilterKey>, Ordered {
	
	@Override
	default String getConfigurableType() {
		return "filter";
	}
	
	/**
	 *  bean name / unique identifier (spring bean name)
	 *  
	 * @return
	 * @since 9.7.7
	 */
	String getId();
	
	/**
	 * Property in filter - filter will be applied, when property will be set in filtering parameters.
	 * FilterBuilder could read other properties from filter, 
	 * 
	 * @return
	 */
	@Override
	String getName();

	/**
	 * A domain type for which it is filter intended.
	 * 
	 * @return Class<E>
	 * @since 9.7.7
	 */
	Class<E> getEntityClass();

	/**
	 * Returns {@link BaseFilter} type class, which is controlled by this filter builder
	 * 
	 * @return
	 * @since 9.7.7
	 */
	Class<F> getFilterClass();
	
	/**
	 * Filter construct partial criteria where clause => {@link Predicate}, which will be appended to query for defined domain type.
	 * Returned Predicate could be {@code null}, if builder doesn't have all parameters in filter set.
	 * Can be used in both - in query and sub query too.
	 * 
	 * @param root
	 * @param query
	 * @param builder
	 * @return
	 * @since 9.7.0
	 */
	default Predicate getPredicate(Root<E> root, AbstractQuery<?> query, CriteriaBuilder builder, F filter) {
		return null;
	}
	
	/**
	 * Finds entities by this filter builder only
	 * 
	 * @param pageable
	 * @return
	 */
	Page<E> find(F filter, Pageable pageable);
}
