package eu.bcvsolutions.idm.ic.czechidm.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfigurationClass;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.api.annotation.IcConfigurationClassProperty;
import eu.bcvsolutions.idm.ic.api.annotation.IcConnectorClass;
import eu.bcvsolutions.idm.ic.connid.service.impl.ConnIdIcConfigurationService;
import eu.bcvsolutions.idm.ic.czechidm.domain.CzechIdMIcConvertUtil;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertyImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorConfigurationImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorKeyImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationService;

/**
 * CzechIdM connector framework implementation
 * 
 * @author svandav
 *
 */
@Service
public class CzechIdMIcConfigurationService implements IcConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(ConnIdIcConfigurationService.class);

	// Cached local connectorInfos
	private Set<IcConnectorInfo> connectorInfos;
	// Cached local default connector configurations
	private Map<String, IcConnectorConfiguration> connectorsConfigurations;
	@Value("#{'${ic.localconnector.packages}'.split(',')}")
	private List<String> localConnectorsPackages;

	@Autowired
	public CzechIdMIcConfigurationService(IcConfigurationFacade icConfigurationAggregator) {
		if (icConfigurationAggregator.getIcConfigs() == null) {
			throw new IcException("Map of IC implementations is not defined!");
		}
		if (icConfigurationAggregator.getIcConfigs().containsKey(IMPLEMENTATION_TYPE)) {
			throw new IcException(
					MessageFormat.format("IC implementation duplicity for key: {0}", IMPLEMENTATION_TYPE));
		}
		// Disable for now
		icConfigurationAggregator.getIcConfigs().put(IMPLEMENTATION_TYPE, this);
	}

	final private static String IMPLEMENTATION_TYPE = "czechidm";

	/**
	 * Return key defined IC implementation
	 * 
	 * @return
	 */
	@Override
	public String getFramework() {
		return IMPLEMENTATION_TYPE;
	}

	/**
	 * Return available local connectors for this IC implementation
	 * 
	 * @return
	 */
	@Override
	public Set<IcConnectorInfo> getAvailableLocalConnectors() {
		connectorInfos = null;
		if (connectorInfos == null) {
			connectorInfos = new HashSet<>();
			connectorsConfigurations = new HashMap<>();
			List<Class<?>> annotated = new ArrayList<>();
			// Find all class with annotation IcConnectorClass under specific
			// packages
			localConnectorsPackages.forEach(packageWithConnectors -> {
				Reflections reflections = new Reflections(packageWithConnectors);
				annotated.addAll(reflections.getTypesAnnotatedWith(IcConnectorClass.class));
			});

			LOG.info(MessageFormat.format("Found annotated classes with IcConnectorClass [{0}]", annotated));

			for (Class<?> clazz : annotated) {
				IcConnectorClass connectorAnnotation = clazz.getAnnotation(IcConnectorClass.class);
				if (!this.getFramework().equals(connectorAnnotation.framework())) {
					continue;
				}

				IcConnectorKey key = new IcConnectorKeyImpl(connectorAnnotation.framework(), clazz.getName(),
						connectorAnnotation.version(), connectorAnnotation.name());
				IcConnectorInfo info = new IcConnectorInfoImpl(connectorAnnotation.displayName(),
						connectorAnnotation.framework(), key);
				Class<? extends IcConnectorConfigurationClass> configurationClass = connectorAnnotation
						.configurationClass();
				connectorInfos.add(info);
				if (!IcConnector.class.isAssignableFrom(clazz)) {
					throw new IcException(MessageFormat.format(
							"Cannot create instance of CzechIdM connector [{0}]! Connector class must be child of [0]!",
							IcConnector.class.getSimpleName()));
				}
				
				IcConnectorConfiguration configuration = initDefaultConfiguration(configurationClass);
				// Put default configuration to cache
				connectorsConfigurations.put(info.getConnectorKey().getFullName(), configuration);
			}
			LOG.info(MessageFormat.format("Found all local connector connectorInfos [{0}]", connectorInfos.toString()));
		}
		return connectorInfos;
	}


	/**
	 * Return find connector default configuration by connector key
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public IcConnectorConfiguration getConnectorConfiguration(IcConnectorInstance connectorInstance) {
		Assert.notNull(connectorInstance.getConnectorKey());
		//
		if(this.connectorsConfigurations == null){
			return null;
		}
		return this.connectorsConfigurations.get(connectorInstance.getConnectorKey().getFullName());

	}

	@Override
	public IcSchema getSchema(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		return null;
	}

	@Override
	public IcConnectorConfiguration getRemoteConnectorConfiguration(IcConnectorInstance connectorInstance) {
		return null;
	}

	@Override
	public Set<IcConnectorInfo> getAvailableRemoteConnectors(IcConnectorServer server) {
		return null;
	}

	public void validate(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void test(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Create instance of default connector configuration
	 * @param configurationClass
	 * @return
	 */
	private IcConnectorConfiguration initDefaultConfiguration(Class<? extends IcConnectorConfigurationClass> configurationClass) {
		try {
			IcConnectorConfigurationClass configurationClassInstance = configurationClass.newInstance();
			List<IcConfigurationProperty> properties = new ArrayList<>();

			PropertyDescriptor[] descriptors = Introspector.getBeanInfo(configurationClass)
					.getPropertyDescriptors();

			Lists.newArrayList(descriptors).stream().forEach(descriptor -> {
				Method readMethod = descriptor.getReadMethod();
				IcConfigurationClassProperty property = readMethod
						.getAnnotation(IcConfigurationClassProperty.class);
				if (property != null) {
					IcConfigurationPropertyImpl icProperty = (IcConfigurationPropertyImpl) CzechIdMIcConvertUtil
							.convertConfigurationProperty(property);
					icProperty.setType(readMethod.getReturnType().getName());
					try {
						icProperty.setValue(readMethod.invoke(configurationClassInstance));
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new CoreException("Cannot read value of connector configuration property!", e);
					}

					properties.add(icProperty);
				}
			});

			// Sort by order
			properties.sort(Comparator.comparing(IcConfigurationProperty::getOrder));
			IcConfigurationPropertiesImpl icProperties = new IcConfigurationPropertiesImpl();
			icProperties.setProperties(properties);

			IcConnectorConfigurationImpl configuration = new IcConnectorConfigurationImpl();
			configuration.setConnectorPoolingSupported(false);
			configuration.setConfigurationProperties(icProperties);
			
			return configuration;

		} catch (IntrospectionException | InstantiationException | IllegalAccessException e) {
			throw new CoreException("Cannot read connector configuration property!", e);
		}
	}
}
