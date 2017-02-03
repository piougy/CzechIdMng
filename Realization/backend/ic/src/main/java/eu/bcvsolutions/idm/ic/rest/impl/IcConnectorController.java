package eu.bcvsolutions.idm.ic.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.ic.IcModuleDescriptor;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.domain.IcResultCode;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.impl.DefaultIcConfigurationFacade;;

/**
 * Rest endpoint provides available connectors and their configuration
 * 
 * @author svandav
 *
 */
@RestController
@Enabled(IcModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/connectors")
public class IcConnectorController implements BaseController {

	private IcConfigurationFacade icConfiguration;

	@Autowired
	public IcConnectorController(DefaultIcConfigurationFacade icConfiguration) {
		this.icConfiguration = icConfiguration;
	}

	/**
	 * Return all local connectors of given framework
	 * 
	 * TODO: search quick?
	 * 
	 * @param framework - ic framework
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search/local")
	public ResponseEntity<Map<String, List<IcConnectorInfo>>> getAvailableLocalConnectors(
			@RequestParam(required = false) String framework) {
		Map<String, List<IcConnectorInfo>> infos = new HashMap<>();
		if (framework != null) {
			if (!icConfiguration.getIcConfigs().containsKey(framework)) {
				throw new ResultCodeException(IcResultCode.IC_FRAMEWORK_NOT_FOUND,
						ImmutableMap.of("framework", framework));
			}
			infos.put(framework, icConfiguration.getIcConfigs().get(framework)
					.getAvailableLocalConnectors());

		} else {
			infos = icConfiguration.getAvailableLocalConnectors();
		}
		return new ResponseEntity<Map<String, List<IcConnectorInfo>>>(infos, HttpStatus.OK);
	}
}
