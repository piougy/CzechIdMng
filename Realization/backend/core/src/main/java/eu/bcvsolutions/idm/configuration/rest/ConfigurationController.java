package eu.bcvsolutions.idm.configuration.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;;

/**
 * Public configuration controller
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RestController
@RequestMapping(value = "/api/public/configurations")
public class ConfigurationController {
	
	/**
	 * Returns all public configuration properties 
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String getPublicConfigurations() {
		return "test";
	}

}
