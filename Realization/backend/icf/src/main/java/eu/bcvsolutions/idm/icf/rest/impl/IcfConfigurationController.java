package eu.bcvsolutions.idm.icf.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.domain.IcfResultCode;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorKeyImpl;
import eu.bcvsolutions.idm.icf.service.impl.DefaultIcfConfigurationFacade;
import eu.bcvsolutions.idm.security.api.domain.IfEnabled;;

/**
 * Rest endpoint provides available connectors and their configuration
 * 
 * @author svandav
 *
 */
@RestController
@IfEnabled(IcfModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/" + IcfModuleDescriptor.MODULE_ID + "/configurations")
public class IcfConfigurationController implements BaseController {

	private DefaultIcfConfigurationFacade icfConfigurationAggregatorService;

	@Autowired
	public IcfConfigurationController(DefaultIcfConfigurationFacade icfConfigurationAggregatorService) {
		super();
		this.icfConfigurationAggregatorService = icfConfigurationAggregatorService;
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
			@RequestBody(required = true) IcfConnectorKeyImpl key) {
		Assert.notNull(key);
		if (!icfConfigurationAggregatorService.getIcfConfigs().containsKey(key.getIcfType())) {
			throw new ResultCodeException(IcfResultCode.ICF_IMPLEMENTATTION_NOT_FOUND,
					ImmutableMap.of("icf", key.getIcfType()));
		}
		IcfConnectorConfiguration conf = icfConfigurationAggregatorService.getIcfConfigs()
				.get(key.getIcfType()).getConnectorConfiguration(key);
		return new ResponseEntity<IcfConnectorConfiguration>(conf, HttpStatus.OK);
	}

}
