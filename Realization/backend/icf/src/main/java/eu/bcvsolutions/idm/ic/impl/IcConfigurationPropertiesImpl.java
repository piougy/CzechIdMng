package eu.bcvsolutions.idm.icf.impl;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperties;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperty;

/**
 * Keep configuration properties for ICF connector
 * @author svandav
 *
 */
public class IcfConfigurationPropertiesImpl implements IcfConfigurationProperties {

    
    List<IcfConfigurationProperty> properties;

    /**
     * The list of properties {@link IcfConfigurationProperty}.
     */
	@Override
	public List<IcfConfigurationProperty> getProperties() {
		if(properties == null){
			properties = new ArrayList<>();
		}
		return properties;
	}

	public void setProperties(List<IcfConfigurationProperty> properties) {
		this.properties = properties;
	}
}
