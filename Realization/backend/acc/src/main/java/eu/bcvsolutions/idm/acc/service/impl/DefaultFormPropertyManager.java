package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.converter.FormPropertyConverter;
import eu.bcvsolutions.idm.acc.service.api.FormPropertyManager;
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
@Service
public class DefaultFormPropertyManager implements FormPropertyManager {
	
	private final PluginRegistry<FormPropertyConverter, IcConfigurationProperty> propertyConverters;
	
	@Autowired
	public DefaultFormPropertyManager(List<? extends FormPropertyConverter> propertyConverters) {
		Assert.notNull(propertyConverters);
		//
		this.propertyConverters = OrderAwarePluginRegistry.create(propertyConverters);
	}
	
	@Override
	public IdmFormAttributeDto toFormAttribute(IcConfigurationProperty propertyConfiguration) {
		FormPropertyConverter converter = getPropertyConverter(propertyConfiguration);
		Assert.notNull(converter);
		//
		return converter.toFormAttribute(propertyConfiguration);
	}

	@Override
	public IcConfigurationProperty toConnectorProperty(IcConfigurationProperty propertyConfiguration, List<IdmFormValueDto> formValues) {
		FormPropertyConverter converter = getPropertyConverter(propertyConfiguration);
		Assert.notNull(converter);
		//
		return converter.toConnectorProperty(propertyConfiguration, formValues);
	}
	
	@Override
	public PersistentType getPersistentType(String configurationPropertyType) {
		for(FormPropertyConverter converter : propertyConverters.getPlugins()) {
			if (converter.supports(configurationPropertyType)) {
				return converter.getFormPropertyType();
			}
		}
		return null;
	}
	
	private FormPropertyConverter getPropertyConverter(IcConfigurationProperty propertyConfiguration) {
		Assert.notNull(propertyConfiguration);
		//
		return propertyConverters.getPluginFor(propertyConfiguration);
	}
	
	

}
