package eu.bcvsolutions.idm.core.eav.api.service;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * Abstract form attribute renderer - attribute face type with custom configuration.
 * 
 * @param <O> evaluated {@link PersistentType} type - renederer is registered to persistent type. 
 * @author Radek Tomiška
 * @since 10.8.0
 */
public abstract class AbstractFormAttributeRenderer implements 
		FormAttributeRenderer,
		BeanNameAware {

	private String beanName; // spring bean name - used as id
	//
	@Autowired private ConfigurationService configurationService; // checks for processor is enabled
	
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}
	
	@Override
	public String getId() {
		return beanName;
	}

	/**
	 * Could be used for ordering in select boxes.
	 */
	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean supports(PersistentType persistentType) {
		Assert.notNull(persistentType, "Persistent type is required.");
		//
		return getPersistentType() == persistentType;
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
}
