package eu.bcvsolutions.idm.ic.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.ic.api.IcConfigurationProperties;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;

/**
 * Keep configuration properties for IC connector
 * 
 * @author svandav
 *
 */
public class IcConfigurationPropertiesImpl implements IcConfigurationProperties {

	List<IcConfigurationProperty> properties;

	/**
	 * The list of properties {@link IcConfigurationProperty}.
	 */
	@Override
	public List<IcConfigurationProperty> getProperties() {
		if (properties == null) {
			properties = new ArrayList<>();
		}
		return properties;
	}

	public void setProperties(List<IcConfigurationProperty> properties) {
		this.properties = properties;
	}

	public IcConfigurationPropertiesImpl addProperty(String name, Object value, String type, String displayName,
			String helpMessage, boolean required) {
		Assert.notNull(name, "Property name cannot be null!");
		IcConfigurationPropertyImpl property = new IcConfigurationPropertyImpl();
		property.setName(name);
		property.setValue(value);
		property.setType(type);
		property.setDisplayName(displayName);
		property.setHelpMessage(helpMessage);
		property.setRequired(required);

		getProperties().add(property);
		return this;
	}
}
