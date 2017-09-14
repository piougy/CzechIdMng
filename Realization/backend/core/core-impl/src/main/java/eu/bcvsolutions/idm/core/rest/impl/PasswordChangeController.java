package eu.bcvsolutions.idm.core.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Changes identity password. Could be public, because previous password is required.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Api(
		value = PasswordChangeController.TAG,  
		tags = { PasswordChangeController.TAG }, 
		description = "Change identity's password",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class PasswordChangeController {
	
	protected static final String TAG = "Password change";
	//
	private final LookupService entityLookupService;
	private final SecurityService securityService;
	private final IdmIdentityService identityService;
	private final AuthenticationManager authenticationManager;
	
	@Autowired
	public PasswordChangeController(
			LookupService entityLookupService,
			SecurityService securityService,
			IdmIdentityService identityService,
			AuthenticationManager authenticationManager) {
		Assert.notNull(entityLookupService);
		Assert.notNull(securityService);
		Assert.notNull(identityService);
		Assert.notNull(authenticationManager);
		//
		this.entityLookupService = entityLookupService;
		this.securityService = securityService;
		this.identityService = identityService;
		this.authenticationManager = authenticationManager;
	}
	
	/**
	 * Changes identity password. Could be public, because previous password is required.
	 * 
	 * @param identityId
	 * @param passwordChangeDto
	 * @return
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = BaseController.BASE_PATH + "/public/identities/{backendId}/password-change", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Change identity's password", 
			nickname = "passwordChange",
			response = PasswordChangeDto.class,
			tags = { PasswordChangeController.TAG })
	public ResponseEntity<PasswordChangeDto> passwordChange(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId,
			@RequestBody @Valid PasswordChangeDto passwordChangeDto) {
		IdmIdentityDto identity = (IdmIdentityDto) entityLookupService.lookupDto(IdmIdentityDto.class, backendId);
		if (identity == null) {
			// we don't result not found by security reasons, it public endpoint
			throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
		}
		// we need to login as identity, if no one is logged in
		try{
			if (!securityService.isAuthenticated()) {
				LoginDto loginDto = new LoginDto();
				loginDto.setSkipMustChange(true);
				loginDto.setUsername(identity.getUsername());
				loginDto.setPassword(passwordChangeDto.getOldPassword());
				loginDto = authenticationManager.authenticate(loginDto);
				//
				// public password change password for all system including idm 
				passwordChangeDto.setAll(true);
				passwordChangeDto.setIdm(true);
			}
		} catch(IdmAuthenticationException ex) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM, ex);
		}
		//
		// check permission for password change
		identityService.checkAccess(identity, IdentityBasePermission.PASSWORDCHANGE);
		//
		identityService.passwordChange(identity, passwordChangeDto);
		return new ResponseEntity<>(passwordChangeDto, HttpStatus.OK);
	}
}
