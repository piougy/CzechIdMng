package eu.bcvsolutions.idm.core.security.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginRequestDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Identity authentication
 * 
 * @author Radek Tomiška 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/authentication")
@Api(value = LoginController.TAG, description = "Authentication endpoint", tags = { LoginController.TAG })
public class LoginController {
	
	protected static final String TAG = "Authentication";
	public static final String REMOTE_AUTH_PATH = "/remote-auth";
	//
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private LoginService loginService;
	
	@ResponseBody
	@ApiOperation(
			value = "Login an get the CIDMST token", 
			notes= "Login an get the CIDMST token. Use returned token attribute value as \"CIDMST\" http header in next requests.",
			response = LoginDto.class,
			tags = { LoginController.TAG } )
	@RequestMapping(method = RequestMethod.POST)
	public Resource<LoginDto> login(
			@ApiParam(value = "Identity credentials.", required = true)
			@Valid @RequestBody(required = true) LoginRequestDto loginDto) {
		if(loginDto == null || loginDto.getUsername() == null || loginDto.getPassword() == null){
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Username and password must be filled");
		}
		return new Resource<LoginDto>(authenticationManager.authenticate(new LoginDto(loginDto)));
	}
	
	@ApiOperation(
			value = "Login with remote token", 
			notes= "Login with remote token an get the CIDMST token. Remote token can be obtained by external authentication system (e.g. OpenAM, OAuth)",
			response = LoginDto.class,
			tags = { LoginController.TAG })
	@RequestMapping(path = REMOTE_AUTH_PATH, method = RequestMethod.GET)
	public Resource<LoginDto> loginWithRemoteToken() {
		return new Resource<LoginDto>(loginService.loginAuthenticatedUser());
	}
	
}
