package eu.bcvsolutions.idm.core.security.rest.impl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginRequestDto;
import eu.bcvsolutions.idm.core.security.api.dto.TwoFactorRequestDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.api.service.TwoFactorAuthenticationManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Identity authentication.
 * 
 * @author Radek Tomiška 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/authentication")
@Api(value = LoginController.TAG, description = "Authentication endpoint", tags = { LoginController.TAG })
public class LoginController implements BaseController {
	
	protected static final String TAG = "Authentication";
	public static final String REMOTE_AUTH_PATH = "/remote-auth";
	//
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private TwoFactorAuthenticationManager twoFactorAuthenticationManager;
	@Autowired private LoginService loginService;
	@Autowired private LookupService lookupService;
	
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
		if (loginDto == null || loginDto.getUsername() == null || loginDto.getPassword() == null){
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Username and password must be filled");
		}
		LoginDto authenticate = authenticationManager.authenticate(new LoginDto(loginDto));
		//
		return new Resource<LoginDto>(authenticate);
	}
	
	/**
	 * Two factor login.
	 * 
	 * @param twoFactorDto
	 * @return
	 * @since 10.7.0
	 */
	@ResponseBody
	@ApiOperation(
			value = "Login - additional two factor authentication", 
			notes= "Additional two factor authentication with TOTP verification code.",
			response = LoginDto.class,
			tags = { LoginController.TAG } )
	@RequestMapping(path = "/two-factor", method = RequestMethod.POST)
	public Resource<LoginDto> twoFactor(
			@ApiParam(value = "Token and verification code.", required = true)
			@Valid @RequestBody(required = true) TwoFactorRequestDto twoFactorDto) {
		if (twoFactorDto == null 
				|| twoFactorDto.getVerificationCode() == null
				|| twoFactorDto.getToken() == null) {
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Verification code must be filled");
		}
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setPassword(twoFactorDto.getVerificationCode());
		loginDto.setToken(twoFactorDto.getToken().asString());
		//
		return new Resource<LoginDto>(twoFactorAuthenticationManager.authenticate(loginDto));
	}
	
	@ApiOperation(
			value = "Login with remote token", 
			notes= "Login with remote token an get the CIDMST token. Remote token can be obtained by external authentication system (e.g. OpenAM, OAuth).",
			response = LoginDto.class,
			tags = { LoginController.TAG })
	@RequestMapping(path = REMOTE_AUTH_PATH, method = RequestMethod.GET)
	public Resource<LoginDto> loginWithRemoteToken() {
		return new Resource<LoginDto>(loginService.loginAuthenticatedUser());
	}
	
	/**
	 * Switch user.
	 * 
	 * 
	 * @param username target user
	 * @return new login dto
	 * @since 10.5.0
	 */
	@ResponseBody
	@ApiOperation(
			value = "Login as other user", 
			notes= "Login as other user (switch user).",
			response = LoginDto.class,
			tags = { LoginController.TAG } )
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_SWITCHUSER + "')")
	@RequestMapping(path = "/switch-user", method = RequestMethod.PUT)
	public Resource<LoginDto> switchUser(
			@ApiParam(value = "Switch to user by given username.", required = true)
			@RequestParam @NotNull String username) {
		// change logged token authorities
		IdmIdentityDto identity = lookupService.lookupDto(IdmIdentityDto.class, username);
		if (identity == null) {
			throw new EntityNotFoundException(IdmIdentity.class, username);
		}
		return new Resource<LoginDto>(loginService.switchUser(identity, IdentityBasePermission.SWITCHUSER));
	}
	
	/**
	 * Switch user - logout. Available for all logged identities (without authority check).
	 * 
	 * @param username target user
	 * @return new login dto
	 * @since 10.5.0
	 */
	@ResponseBody
	@ApiOperation(
			value = "Logout after login as other user", 
			notes= "Logout after login as other user (switch user logout).",
			response = LoginDto.class,
			tags = { LoginController.TAG } )
	@RequestMapping(path = "/switch-user", method = RequestMethod.DELETE)
	public Resource<LoginDto> switchUserLogout() {
		return new Resource<LoginDto>(loginService.switchUserLogout());
	}	
}
