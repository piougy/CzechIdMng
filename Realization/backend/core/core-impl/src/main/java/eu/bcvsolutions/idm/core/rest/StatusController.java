package eu.bcvsolutions.idm.core.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * "Naive" status
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH)
@Api(
		value = StatusController.TAG,  
		tags = { StatusController.TAG }, 
		description = "Application status",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class StatusController {

	public static final String OK_STATUS_PLAIN = "OK";
	public static final String OK_STATUS_HTML = "<html><head><title>CzechIdM API</title></head><body><h1>CzechIdM API is running</h1><p>If you can see this message, API is running</p></body></html>";
	protected static final String TAG = "Status";
	
	@RequestMapping(method = RequestMethod.GET, value = "/status", produces = MediaType.TEXT_PLAIN_VALUE)
	@ApiOperation(
			value = "Get status", 
			nickname = "getPlainStatus",
			tags = { StatusController.TAG })
	public String getPlainStatus() {
		return OK_STATUS_PLAIN;
	}
	
	@ApiOperation(
			value = "Get status",
			nickname = "getPlainStatus",
			tags = { StatusController.TAG })
	@RequestMapping(method = RequestMethod.GET, value = "/status", produces = MediaType.TEXT_HTML_VALUE)
	public String getHtmlStatus() {
		return OK_STATUS_HTML;
	}
}
