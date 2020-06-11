package eu.bcvsolutions.idm.core.api.repository.filter;

import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Builds filters for domain types.
 * 
 * @author Radek Tomiška
 */
public interface FilterManager {
	
	/**
	 * Enable / disable check filter is properly registered, when filter is used (by entity and property name). Throw exeption, when unrecognised filter is used.
	 * 
	 * @since 10.4.0
	 */
	String PROPERTY_CHECK_SUPPORTED_FILTER_ENABLED = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.filter.check.supported.enabled";
	boolean DEFAULT_CHECK_SUPPORTED_FILTER_ENABLED = true;
	
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

	/**
     * Returns all registered filter builders
     *
     * @param filter
     * @return
     * @since 9.7.7
     */
    List<FilterBuilderDto> find(FilterBuilderFilter filter);
    
    /**
     * Get filter builder by its id.
     * 
     * @param filterBuilderId
     * @return
     * @since 9.7.7
     */
    FilterBuilder<? extends BaseEntity, ? extends DataFilter> getFilterBuilder(String filterBuilderId);
    
    /**
     * Activate given filter builder. 
     * Filter can be activated (enabled) only => will be effective (old one active will not be used).
     * 
     * @param filterBuilderId
     * @since 9.7.7
     */
    void enable(String filterBuilderId);
}
