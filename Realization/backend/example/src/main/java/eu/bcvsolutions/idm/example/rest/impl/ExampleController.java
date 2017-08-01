package eu.bcvsolutions.idm.example.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import eu.bcvsolutions.idm.example.dto.Pong;
import eu.bcvsolutions.idm.example.service.api.ExampleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

/**
 * Example controller
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/examples")
@Api(value = ExampleController.TAG, description = "Example operations", tags = { "Examples" })
public class ExampleController {
	
	protected static final String TAG = "Examples";
	@Autowired private ExampleService service;

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, path = "/ping")
	@ApiOperation(
			value = "Ping - Pong operation", 
			notes= "Returns message with additional informations",
			nickname = "ping", 
			tags={ ExampleController.TAG }, 
			response = Pong.class, 
			authorizations = {
				@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public ResponseEntity<Pong> ping(
			@ApiParam(value = "In / out message", example = "hello", defaultValue = "hello") 
			@RequestParam(required = false, defaultValue = "hello") String message
			) {
		return new ResponseEntity<>(service.ping(message), HttpStatus.OK); 
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, path = "/private-value")
	@ApiOperation(
			value = "Read private value", 
			notes= "Returns configuration property - private value.",
			nickname = "getPrivateValue", 
			tags={ ExampleController.TAG }, 
			authorizations = {
				@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public String getPrivateValue() {
		return service.getPrivateValue();
	}
	
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(method = RequestMethod.GET, path = "/notification")
	@ApiOperation(
			value = "Send notification", 
			notes= "Sending given message to currently logged identity (example topic is used).",
			nickname = "sendNotification", 
			tags={ ExampleController.TAG }, 
			authorizations = {
				@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public void sendNotification(
			@ApiParam(value = "Notification message", example = "hello", defaultValue = "hello") 
			@RequestParam(required = false, defaultValue = "hello") String message) {
		service.sendNotification(message);
	}
}
