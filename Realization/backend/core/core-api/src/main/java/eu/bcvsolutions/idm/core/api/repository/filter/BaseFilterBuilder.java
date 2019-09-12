package eu.bcvsolutions.idm.core.api.repository.filter;

import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

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
public abstract class BaseFilterBuilder<E extends BaseEntity, F extends DataFilter> 
		implements FilterBuilder<E, F>, BeanNameAware {

	private final Class<E> entityClass;
	private String beanName; // spring bean name - used as processor id
	@Autowired(required = false)
	private ConfigurationService configurationService; // optional internal dependency - checks for processor is enabled
	
	@SuppressWarnings("unchecked")
	public BaseFilterBuilder() {
		Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), FilterBuilder.class);
		//
		entityClass = (Class<E>) genericTypes[0];
	}

    @Override
	public Class<E> getEntityClass(){
		return entityClass;
	}
    
    @Override
	public void setBeanName(String name) {
		this.beanName = name;
	}
	
	@Override
	public String getId() {
		return beanName;
	}

	@Override
	public boolean supports(FilterKey delimiter) {
		return entityClass.isAssignableFrom(delimiter.getEntityClass())
				&& delimiter.getName().equals(this.getName());
	}
	
	@Override
	public int getOrder() {
		return ConfigurationService.DEFAULT_ORDER;
	}
	
	/**
	 * Returns prefix to configuration for this filter builder. 
	 * Under this prefix could be found all builder's properties.
	 * 
	 * Adds entityClass to standard configuration prefix
	 * 
	 * @return
	 */
	@Override
	public String getConfigurationPrefix() {
		return ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
				+ getModule()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ getConfigurableType()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ entityClass.getSimpleName()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ getName();
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
}
