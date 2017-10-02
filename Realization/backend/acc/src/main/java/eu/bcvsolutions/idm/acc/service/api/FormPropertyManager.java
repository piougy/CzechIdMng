package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
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
	IdmFormAttributeDto toFormAttribute(IcConfigurationProperty propertyConfiguration);

	/**
	 * Returns filled connector property from eav
	 * 
	 * @param propertyConfiguration
	 * @param formValues
	 * @return
	 */
	IcConfigurationProperty toConnectorProperty(IcConfigurationProperty propertyConfiguration, List<IdmFormValueDto> formValues);
	
	/**
	 * Returns eav persistent type for given configurationPropertyType
	 * 
	 * @param configurationPropertyType
	 * @return
	 */
	PersistentType getPersistentType(String configurationPropertyType);
	
}
