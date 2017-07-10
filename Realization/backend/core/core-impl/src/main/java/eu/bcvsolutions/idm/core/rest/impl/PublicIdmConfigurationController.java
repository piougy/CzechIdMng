package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Provides public configurations
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/public/configurations")
@Api( 
		tags = { IdmConfigurationController.TAG }, 
		description = "Public configuration items",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class PublicIdmConfigurationController implements BaseController {
	
	private final ConfigurationService configurationService;
	
	@Autowired
	public PublicIdmConfigurationController(ConfigurationService configurationService) {
		Assert.notNull(configurationService);
		//
		this.configurationService = configurationService;
	}
	
	/**
	 * Returns all public configuration properties 
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ApiOperation(
			value = "Read public configuration items", 
			nickname = "findAllPublicConfigurations", 
			tags = { IdmConfigurationController.TAG })
	public List<IdmConfigurationDto> getAllPublicConfigurations() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllPublicConfigurations();
	}
}
