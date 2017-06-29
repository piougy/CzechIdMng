package eu.bcvsolutions.idm.example.rest.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.example.dto.Pong;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

/**
 * Ping pong example controller
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/examples")
@Api(value = "Examples", description = "Example operations", tags = { "Examples" })
public class ExampleController {

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, path = "/ping")
	@ApiOperation(
			value = "Ping - Pong operation", 
			notes= "Returns message with additional informations",
			nickname = "ping", 
			tags={ "Examples" }, 
			response = Pong.class, 
			authorizations = {
				@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public ResponseEntity<?> ping(
			@ApiParam(value = "In / out message", example = "hello", defaultValue = "hello") 
			@RequestParam(required = false, defaultValue = "hello") String message
			) {
		return new ResponseEntity<>(new Pong(message), HttpStatus.OK); 
	}
}
