package eu.bcvsolutions.idm.ic.czechidm.domain;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfigurationClass;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.annotation.IcConfigurationClassProperty;
import eu.bcvsolutions.idm.ic.api.annotation.IcConnectorClass;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertyImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorKeyImpl;

/**
 * Convert utility for CzechIdM implementation
 * 
 * @author svandav
 *
 */
public class CzechIdMIcConvertUtil {

	public static IcConfigurationProperty convertConfigurationProperty(IcConfigurationClassProperty property) {
		if (property == null) {
			return null;
		}
		IcConfigurationPropertyImpl icProperty = new IcConfigurationPropertyImpl();
		icProperty.setConfidential(property.confidential());
		icProperty.setDisplayName(property.displayName());
		icProperty.setHelpMessage(property.helpMessage());
		icProperty.setRequired(property.required());
		icProperty.setOrder(property.order());

		return icProperty;
	}

	public static IcConnectorInfo convertConnectorClass(IcConnectorClass connectorAnnotation,
			Class<? extends IcConnector> clazz) {
		IcConnectorKey key = new IcConnectorKeyImpl(connectorAnnotation.framework(), clazz.getName(),
				connectorAnnotation.version(), connectorAnnotation.name());
		IcConnectorInfo info = new IcConnectorInfoImpl(
				MessageFormat.format("{0} {1}", connectorAnnotation.displayName(), connectorAnnotation.version()),
				connectorAnnotation.framework(), key);
		return info;
	}

	public static IcConnectorConfigurationClass convertIcConnectorConfiguration(IcConnectorConfiguration configuration,
			Class<? extends IcConnectorConfigurationClass> configurationClass) {
		if (configuration == null || configuration.getConfigurationProperties() == null
				|| configuration.getConfigurationProperties().getProperties() == null) {
			return null;
		}
		List<IcConfigurationProperty> properties = configuration.getConfigurationProperties().getProperties();

		try {
			IcConnectorConfigurationClass configurationClassInstance = configurationClass.newInstance();

			PropertyDescriptor[] descriptors;
			descriptors = Introspector.getBeanInfo(configurationClass).getPropertyDescriptors();
			Lists.newArrayList(descriptors).stream().forEach(descriptor -> {

				String propertyName = descriptor.getName();
				Method writeMethod = descriptor.getWriteMethod();
				IcConfigurationProperty propertyToConvert = properties.stream()
						.filter(property -> propertyName.equals(property.getName())).findFirst().orElse(null);
				if (propertyToConvert == null) {
					return;
				}
				try {
					writeMethod.invoke(configurationClassInstance, propertyToConvert.getValue());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new IcException(e);
				}
			});
			return configurationClassInstance;
		} catch (IntrospectionException | InstantiationException | IllegalAccessException e) {
			throw new IcException(e);
		}
	}

}
