package eu.bcvsolutions.idm.icf.rest.impl;

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
import eu.bcvsolutions.idm.icf.IcfModuleDescriptor;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.domain.IcfResultCode;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationFacade;
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
@RequestMapping(value = BaseEntityController.BASE_PATH + "/connectors")
public class IcfConnectorController implements BaseController {

	private IcfConfigurationFacade icfConfiguration;

	@Autowired
	public IcfConnectorController(DefaultIcfConfigurationFacade icfConfiguration) {
		this.icfConfiguration = icfConfiguration;
	}

	/**
	 * Return all local connectors of given framework
	 * 
	 * TODO: search quick?
	 * 
	 * @param framework - icf framework
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search/local")
	public ResponseEntity<Map<String, List<IcfConnectorInfo>>> getAvailableLocalConnectors(
			@RequestParam(required = false) String framework) {
		Map<String, List<IcfConnectorInfo>> infos = new HashMap<>();
		if (framework != null) {
			if (!icfConfiguration.getIcfConfigs().containsKey(framework)) {
				throw new ResultCodeException(IcfResultCode.ICF_FRAMEWORK_NOT_FOUND,
						ImmutableMap.of("framework", framework));
			}
			infos.put(framework, icfConfiguration.getIcfConfigs().get(framework)
					.getAvailableLocalConnectors());

		} else {
			infos = icfConfiguration.getAvailableLocalConnectors();
		}
		return new ResponseEntity<Map<String, List<IcfConnectorInfo>>>(infos, HttpStatus.OK);
	}
}
