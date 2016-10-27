package eu.bcvsolutions.idm.icf.rest.impl;

import java.net.URL;
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

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.icf.IcfModuleDescriptor;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.domain.IcfResultCode;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;
import eu.bcvsolutions.idm.icf.service.impl.IcfConfigurationAggregatorService;
import eu.bcvsolutions.idm.security.api.domain.IfEnabled;;

/**
 * 
 * @author svandav
 *
 */
@RestController
@IfEnabled(IcfModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/" + IcfModuleDescriptor.MODULE_ID + "/connectors")
public class IcfConnectorController implements BaseController {

	private IcfConfigurationAggregatorService icfConfigurationAggregatorService;
	private IcfConfigurationController icfConfigurationController;

	@Autowired
	public IcfConnectorController(IcfConfigurationAggregatorService icfConfigurationAggregatorService, IcfConfigurationController icfConfigurationController) {
		super();
		this.icfConfigurationAggregatorService = icfConfigurationAggregatorService;
		this.icfConfigurationController = icfConfigurationController;
	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<Map<String, List<IcfConnectorInfo>>> getAvailableLocalConnectors() {
		return this.icfConfigurationController.getAvailableLocalConnectors(null);
	}
	
	

	@RequestMapping(method = RequestMethod.POST, value = "/connector-configuration")
	public ResponseEntity<IcfConnectorConfiguration> getConnectorConfigurations(
			@RequestBody(required = true) IcfConnectorInfoDto info) {
		Assert.notNull(info);
		Assert.notNull(info.getConnectorKey());
		if (!icfConfigurationAggregatorService.getIcfConfigs().containsKey(info.getConnectorKey().getIcfType())) {
			throw new ResultCodeException(IcfResultCode.ICF_IMPLEMENTATTION_NOT_FOUND,
					"ICF implementation with key " + info.getConnectorKey().getIcfType() + " is not found!");
		}
		IcfConnectorConfiguration conf = icfConfigurationAggregatorService.getIcfConfigs()
				.get(info.getConnectorKey().getIcfType()).getConnectorConfiguration(info);
		return new ResponseEntity<IcfConnectorConfiguration>(conf, HttpStatus.OK);
	}

}
