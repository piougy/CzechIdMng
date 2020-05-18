package eu.bcvsolutions.idm.core.eav.api.service;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Abstract form projection route - projection configuration.
 * 
 * @param <O> evaluated {@link Identifiable} type - route is designed for owner type. 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public abstract class AbstractFormProjectionRoute<O extends Identifiable> implements 
		FormProjectionRoute<O>,
		BeanNameAware {

	private final Class<O> ownerType;
	private String beanName; // spring bean name - used as id
	//
	@Autowired private ConfigurationService configurationService; // checks for processor is enabled

	@SuppressWarnings({ "unchecked" })
	public AbstractFormProjectionRoute() {
		this.ownerType = (Class<O>) GenericTypeResolver.resolveTypeArgument(getClass(), FormProjectionRoute.class);
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
	public Class<O> getOwnerType() {
		return ownerType;
	}

	/**
	 * Could be used for ordering in select boxes.
	 */
	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean supports(Class<?> ownerType) {
		Assert.notNull(ownerType, "Owner type is required.");
		//
		return ownerType.isAssignableFrom(ownerType);
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
}
