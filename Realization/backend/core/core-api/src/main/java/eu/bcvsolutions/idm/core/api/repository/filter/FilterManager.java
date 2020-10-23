package eu.bcvsolutions.idm.core.api.repository.filter;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.FilterSizeExceededException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Builds filters for domain types.
 * 
 * @author Radek Tomiška
 */
public interface FilterManager {
	
	/**
	 * Enable / disable check filter is properly registered, when filter is used (by entity and property name). 
	 * Throw exeption, when unrecognised filter is used.
	 * 
	 * @since 10.4.0
	 */
	String PROPERTY_CHECK_SUPPORTED_FILTER_ENABLED = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.filter.check.supported.enabled";
	boolean DEFAULT_CHECK_SUPPORTED_FILTER_ENABLED = true;
	
	/**
	 * Check count of values exceeded given maximum. 
	 * Related to database count of query parameters (e.g. Oracle = {@code 1000}, MSSql = {@code 2100}).
	 * Throw exception, when size is exceeded. Set to {@code -1} to disable this check.
	 * 
	 * @since 10.6.0
	 */
	String PROPERTY_CHECK_FILTER_SIZE_MAXIMUM = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.filter.check.size.maximum";
	int DEFAULT_CHECK_FILTER_SIZE_MAXIMUM = 500;
	
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
    
    /**
     * Check count of values exceeded given maximum.
     * 
     * @param filterKey filter
     * @param filterValues values
     * @return values
     * @throws FilterSizeExceededException when value size exceeded configured maximum
     * @see FilterManager#PROPERTY_CHECK_FILTER_SIZE_MAXIMUM
     * @since 10.6.0
     */
    <T> List<T> checkFilterSizeExceeded(FilterKey filterKey, List<T> filterValues);
}
