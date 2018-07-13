package eu.bcvsolutions.idm.core.security.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Logout
 * 
 * @author Radek Tomiška 
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/logout")
@Api(value = LogoutController.TAG, description = "Logout endpoint", tags = { LogoutController.TAG })
public class LogoutController implements BaseController {
	
	protected static final String TAG = "Logout";
	//
	@Autowired private AuthenticationManager authenticationManager;
	
	@ApiOperation(
			value = "Logout", 
			notes= "Logout currently logged identity and disable currently used token.",
			response = LoginDto.class,
			tags = { LogoutController.TAG })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RequestMapping(method = RequestMethod.DELETE)
	public void logout() {
		authenticationManager.logout();
	}
}
