package eu.bcvsolutions.idm.core.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.bcvsolutions.idm.core.api.rest.BaseController;

/**
 * Root api controller (documentation purpose only) - redirect to swagger ui
 * 
 * @author Radek Tomiška
 *
 */
@Controller
public class RootController implements BaseController {
	
	@RequestMapping(value = "/api", method = RequestMethod.GET)
	public String rootApi() {
		return redirectUrl();
	}
	
	private String redirectUrl() {
		return "redirect:/swagger-ui.html";
	}
}
