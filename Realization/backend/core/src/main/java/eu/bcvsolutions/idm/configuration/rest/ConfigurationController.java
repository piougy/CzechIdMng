package eu.bcvsolutions.idm.configuration.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.configuration.dto.ConfigurationDto;
import eu.bcvsolutions.idm.configuration.service.ConfigurationService;;

/**
 * Configuration controller - add custom methods to configuration repository
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RestController
@RequestMapping(value = "/api")
public class ConfigurationController {
	
	@Autowired
	private ConfigurationService configurationService; 
	
	/**
	 * Returns all public configuration properties 
	 * 
	 * @return
	 */
	@RequestMapping(path = "/public/configurations", method = RequestMethod.GET)
	public List<ConfigurationDto> getAllPublicConfigurations() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllPublicConfigurations();
	}
	
	/**
	 * Returns configurations from property files 
	 * 
	 * @return
	 */
	@PostFilter("filterObject.name.startsWith('idm.pub.') or hasAuthority('CONFIGURATIONSECURED_READ')")
	@RequestMapping(path = "/configurations/file", method = RequestMethod.GET)
	public List<ConfigurationDto> getFileConfigurations() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllFileConfigurations();
	}

}
