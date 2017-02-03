package eu.bcvsolutions.idm.core.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;

/**
 * "Naive" status
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH)
public class StatusController {

	public static final String OK_STATUS_PLAIN = "OK";
	public static final String OK_STATUS_HTML = "<html><head><title>CzechIdM API</title></head><body><h1>CzechIdM API is running</h1><p>If you can see this message, API is running</p></body></html>";
	
	@RequestMapping(method = RequestMethod.GET, value = "/status", produces = "text/plain")
	public String getPlainStatus() {
		return OK_STATUS_PLAIN;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/status", produces = "text/html")
	public String getHtmlStatus() {
		return OK_STATUS_HTML;
	}
}
