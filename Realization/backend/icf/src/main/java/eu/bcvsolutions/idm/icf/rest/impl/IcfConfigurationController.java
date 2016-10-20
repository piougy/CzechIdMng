package eu.bcvsolutions.idm.icf.rest.impl;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.reflections.Reflections;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ControllerUtils;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.icf.IcfModuleDescriptor;
import eu.bcvsolutions.idm.security.api.domain.IfEnabled;;

/**
 * 
 * @author svandav
 *
 */
@RestController
@IfEnabled(IcfModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/" +IcfModuleDescriptor.MODULE_ID + "/configurations")
public class IcfConfigurationController /*extends AbstractReadWriteEntityController<SysSystem, QuickFilter>*/ {

	
	@RequestMapping(method = RequestMethod.GET)
	public Resources<?>  test(@RequestParam MultiValueMap<String, Object> parameters, @PageableDefault Pageable pageable,
			PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		Reflections reflections = new Reflections();
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(ConnectorClass.class);
		URL url = null;
		for (Class clazz : annotated) {
			url = clazz.getProtectionDomain().getCodeSource().getLocation();
		}

		ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
		ConnectorInfoManager manager = fact.getLocalManager(url);
		ConnectorKey key = new ConnectorKey("org.identityconnectors.foobar", "1.0", "FooBarConnector");
		ConnectorInfo info = manager.getConnectorInfos().get(0);

		// From the ConnectorInfo object, create the default APIConfiguration.
		APIConfiguration apiConfig = info.createDefaultAPIConfiguration();

		// From the default APIConfiguration, retrieve the
		// ConfigurationProperties.
		ConfigurationProperties properties = apiConfig.getConfigurationProperties();

		// Print out what the properties are (not necessary)
		List<String> propertyNames = properties.getPropertyNames();
		for (String propName : propertyNames) {
			ConfigurationProperty prop = properties.getProperty(propName);
			System.out.println("Property Name: " + prop.getName() + "\tProperty Type: " + prop.getType());
		}

		// Set all of the ConfigurationProperties needed by the connector.
		// properties.setPropertyValue("host", FOOBAR_HOST);
		// properties.setPropertyValue("adminName", FOOBAR_ADMIN);
		// properties.setPropertyValue("adminPassword", FOOBAR_PASSWORD);
		// properties.setPropertyValue("useSSL", false);

		// Use the ConnectorFacadeFactory's newInstance() method to get a new
		// connector.
		ConnectorFacade conn = ConnectorFacadeFactory.getInstance().newInstance(apiConfig);

		// Make sure we have set up the Configuration properly
		conn.validate();
		return new Resources(ControllerUtils.EMPTY_RESOURCE_LIST);

		// Start using the Connector
		// conn.[authenticate|create|update|delete|search|...];

	}

}
