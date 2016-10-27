package eu.bcvsolutions.idm.icf.rest.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ControllerUtils;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.icf.IcfModuleDescriptor;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperties;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.icf.domain.IcfResultCode;
import eu.bcvsolutions.idm.icf.dto.IcfAttributeDto;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertiesDto;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertyDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorConfigurationDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;
import eu.bcvsolutions.idm.icf.dto.IcfPasswordAttributeDto;
import eu.bcvsolutions.idm.icf.service.impl.IcfConfigurationAggregatorService;
import eu.bcvsolutions.idm.icf.service.impl.IcfConnectorAggregatorService;
import eu.bcvsolutions.idm.security.api.domain.IfEnabled;
import eu.bcvsolutions.idm.security.domain.GuardedString;;

/**
 * 
 * @author svandav
 *
 */
@RestController
@IfEnabled(IcfModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/" + IcfModuleDescriptor.MODULE_ID + "/configurations")
public class IcfConfigurationController implements BaseController {

	private IcfConfigurationAggregatorService icfConfigurationAggregatorService;
	@Autowired
	private IcfConnectorAggregatorService icfConnectorAggregatorService;

	@Autowired
	public IcfConfigurationController(IcfConfigurationAggregatorService icfConfigurationAggregatorService) {
		super();
		this.icfConfigurationAggregatorService = icfConfigurationAggregatorService;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/test")
	public Resources<?> test() {

		List<IcfConnectorInfo> infos = icfConfigurationAggregatorService.getAvailableLocalConnectors().get("connId");

		IcfConnectorConfigurationDto icfConf = new IcfConnectorConfigurationDto();
		IcfConfigurationProperties properties = new IcfConfigurationPropertiesDto();
		icfConf.setConfigurationProperties(properties);
		// Set all of the ConfigurationProperties needed by the connector.
		properties.getProperties().add(new IcfConfigurationPropertyDto("host", "localhost"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("port", "5432"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("user", "idmadmin"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("password",
				new org.identityconnectors.common.security.GuardedString("idmadmin".toCharArray())));
		properties.getProperties().add(new IcfConfigurationPropertyDto("database", "bcv_idm_storage"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("table", "system_users"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("keyColumn", "name"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("passwordColumn", "password"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("jdbcDriver", "org.postgresql.Driver"));
		properties.getProperties()
				.add(new IcfConfigurationPropertyDto("jdbcUrlTemplate", "jdbc:postgresql://%h:%p/%d"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("rethrowAllSQLExceptions", true));
		List<IcfAttribute> attributes = new ArrayList<>();

		attributes.add(new IcfAttributeDto("svandav"));
		attributes.add(new IcfAttributeDto("firstName", "Vít"));
		attributes.add(new IcfAttributeDto("lastName", "Švanda"));
		attributes.add(new IcfPasswordAttributeDto(new GuardedString("heslo")));

		IcfUidAttribute uid = icfConnectorAggregatorService.createObject(infos.get(0).getConnectorKey(), icfConf,
				attributes);
		return new Resources(ControllerUtils.EMPTY_RESOURCE_LIST);

		// Reflections reflections = new Reflections();
		// Set<Class<?>> annotated =
		// reflections.getTypesAnnotatedWith(ConnectorClass.class);
		// URL url = null;
		// for (Class clazz : annotated) {
		// url = clazz.getProtectionDomain().getCodeSource().getLocation();
		// }
		//
		// ConnectorInfoManagerFactory fact =
		// ConnectorInfoManagerFactory.getInstance();
		// ConnectorInfoManager manager = fact.getLocalManager(url);
		// ConnectorKey key = new ConnectorKey("org.identityconnectors.foobar",
		// "1.0", "FooBarConnector");
		// ConnectorInfo info = manager.getConnectorInfos().get(0);
		//
		// // From the ConnectorInfo object, create the default
		// APIConfiguration.
		// APIConfiguration apiConfig = info.createDefaultAPIConfiguration();
		//
		// // From the default APIConfiguration, retrieve the
		// // ConfigurationProperties.
		// ConfigurationProperties properties =
		// apiConfig.getConfigurationProperties();
		//
		// // Print out what the properties are (not necessary)
		// List<String> propertyNames = properties.getPropertyNames();
		// for (String propName : propertyNames) {
		// ConfigurationProperty prop = properties.getProperty(propName);
		// System.out.println("Property Name: " + prop.getName() + "\tProperty
		// Type: " + prop.getType());
		// }
		//
		// // Set all of the ConfigurationProperties needed by the connector.
		// properties.setPropertyValue("host", "localhost");
		// properties.setPropertyValue("port", "5432");
		// properties.setPropertyValue("user", "idmadmin");
		// properties.setPropertyValue("password",
		// new
		// org.identityconnectors.common.security.GuardedString("idmadmin".toCharArray()));
		// properties.setPropertyValue("database", "bcv_idm_storage");
		// properties.setPropertyValue("table", "system_users");
		// properties.setPropertyValue("keyColumn", "name");
		// properties.setPropertyValue("passwordColumn", "password");
		// properties.setPropertyValue("jdbcDriver", "org.postgresql.Driver");
		// properties.setPropertyValue("jdbcUrlTemplate",
		// "jdbc:postgresql://%h:%p/%d");
		// properties.setPropertyValue("rethrowAllSQLExceptions", true);
		//
		// // Use the ConnectorFacadeFactory's newInstance() method to get a new
		// // connector.
		// ConnectorFacade conn =
		// ConnectorFacadeFactory.getInstance().newInstance(apiConfig);
		//
		// // Make sure we have set up the Configuration properly
		// conn.validate();
		//
		// Set<Class<? extends APIOperation>> ops =
		// conn.getSupportedOperations();
		//
		// Schema schema = conn.schema();
		// Set<ObjectClassInfo> objectClasses = schema.getObjectClassInfo();
		// Set<ObjectClassInfo> ocinfos =
		// schema.getSupportedObjectClassesByOperation(CreateApiOp.class);
		// for(ObjectClassInfo oci : objectClasses) {
		// Set<AttributeInfo> attributeInfos = oci.getAttributeInfo();
		// String type = oci.getType();
		// if(ObjectClass.ACCOUNT_NAME.equals(type)) {
		// for(AttributeInfo i : attributeInfos) {
		// System.out.println(i.toString());
		// }
		// }
		// }
		// //create an account
		// Set<Attribute> attrs = new HashSet();
		// attrs.add(new Name("TESTUSER"));
		// attrs.add(AttributeBuilder.buildPassword("TESTPASSWORD".toCharArray()));
		// attrs.add(AttributeBuilder.build("firstName", "Tirasa"));
		// attrs.add(AttributeBuilder.build("lastName", "Larsa"));
		// SearchResult result = conn.search(ObjectClass.ACCOUNT,null, null,
		// null);
		//
		// return new Resources(ControllerUtils.EMPTY_RESOURCE_LIST);

		// Start using the Connector
		// conn.[authenticate|create|update|delete|search|...];

	}

	@RequestMapping(method = RequestMethod.GET, value = "/available-local-connectors")
	public ResponseEntity<Map<String, List<IcfConnectorInfo>>> getAvailableLocalConnectors(
			@RequestParam(required = false) String implementation) {
		Map<String, List<IcfConnectorInfo>> infos = new HashMap<>();
		if (implementation != null) {
			if (!icfConfigurationAggregatorService.getIcfConfigs().containsKey(implementation)) {
				throw new ResultCodeException(IcfResultCode.ICF_IMPLEMENTATTION_NOT_FOUND,
						ImmutableMap.of("icf", implementation));
			}
			infos.put(implementation, icfConfigurationAggregatorService.getIcfConfigs().get(implementation)
					.getAvailableLocalConnectors());

		} else {
			infos = icfConfigurationAggregatorService.getAvailableLocalConnectors();
		}
		return new ResponseEntity<Map<String, List<IcfConnectorInfo>>>(infos, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/connector-configuration")
	public ResponseEntity<IcfConnectorConfiguration> getConnectorConfigurations(
			@RequestBody(required = true) IcfConnectorInfoDto info) {
		Assert.notNull(info);
		Assert.notNull(info.getConnectorKey());
		if (!icfConfigurationAggregatorService.getIcfConfigs().containsKey(info.getConnectorKey().getIcfType())) {
			throw new ResultCodeException(IcfResultCode.ICF_IMPLEMENTATTION_NOT_FOUND,
					ImmutableMap.of("icf", info.getConnectorKey().getIcfType()));
		}
		IcfConnectorConfiguration conf = icfConfigurationAggregatorService.getIcfConfigs()
				.get(info.getConnectorKey().getIcfType()).getConnectorConfiguration(info);
		return new ResponseEntity<IcfConnectorConfiguration>(conf, HttpStatus.OK);
	}

}
