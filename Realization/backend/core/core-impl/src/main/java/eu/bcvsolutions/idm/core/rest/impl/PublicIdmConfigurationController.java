package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.ConfigurationDto;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/public/configurations")
public class PublicIdmConfigurationController implements BaseEntityController<IdmConfiguration> {
	
	private final IdmConfigurationService configurationService;
	
	@Autowired
	public PublicIdmConfigurationController(IdmConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	/**
	 * Returns all public configuration properties 
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<ConfigurationDto> getAllPublicConfigurations() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllPublicConfigurations();
	}

}
