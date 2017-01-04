package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;

/**
 * Connector property type vs. eav type mapping
 * 
 * @author Radek Tomiška
 *
 */
public interface FormPropertyManager {

	/**
	 *  Returns eav form attribute from given connector property
	 *  
	 * @param propertyConfiguration
	 * @return
	 */
	IdmFormAttribute toFormAttribute(IcConfigurationProperty propertyConfiguration);

	/**
	 * Returns filled connector property from eav
	 * 
	 * @param propertyConfiguration
	 * @param formValues
	 * @return
	 */
	IcConfigurationProperty toConnectorProperty(IcConfigurationProperty propertyConfiguration, List<AbstractFormValue<SysSystem>> formValues);
	
	/**
	 * Returns eav persistent type for given configurationPropertyType
	 * 
	 * @param configurationPropertyType
	 * @return
	 */
	PersistentType getPersistentType(String configurationPropertyType);
	
}
