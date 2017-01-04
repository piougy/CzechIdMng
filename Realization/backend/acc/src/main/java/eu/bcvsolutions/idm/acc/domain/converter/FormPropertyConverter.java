package eu.bcvsolutions.idm.acc.domain.converter;

import java.util.List;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;

/**
 * Connector property type vs. eav type conversion
 * 
 * @author Radek Tomiška
 *
 */
public interface FormPropertyConverter extends Plugin<IcConfigurationProperty> {

	/**
	 * Returns persistent type for given connector property types
	 * 
	 * @return
	 */
	PersistentType getFormPropertyType();
	
	/**
	 *  Returns eav form attribute from given connector property.
	 *  
	 * @param propertyConfiguration
	 * @return
	 */
	IdmFormAttribute toFormAttribute(IcConfigurationProperty propertyConfiguration);
	
	/**
	 * Returns filled connector property from eav.
	 * 
	 * @param propertyConfiguration
	 * @param formValues
	 * @return
	 */
	IcConfigurationProperty toConnectorProperty(IcConfigurationProperty propertyConfiguration, List<AbstractFormValue<SysSystem>> formValues);
	
	/**
	 * Converts default value by property data type. If property supports
	 * multiple values, then return multi lines string.
	 * 
	 * @param property
	 * @return
	 */
	String convertDefaultValue(IcConfigurationProperty property);
	
	/**
	 * Returns true, when this converter supports given configurationPropertyType.
	 * 
	 * @param configurationPropertyType
	 * @return
	 */
	boolean supports(String configurationPropertyType);
}
