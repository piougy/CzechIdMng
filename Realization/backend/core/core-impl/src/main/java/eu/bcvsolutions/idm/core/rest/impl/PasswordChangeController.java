package eu.bcvsolutions.idm.core.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.service.LoginService;

/**
 * Changes identity password. Could be public, because previous password is required.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
public class PasswordChangeController {
	
	private final LookupService entityLookupService;
	private final LoginService loginService;
	private final SecurityService securityService;
	private final IdmIdentityService identityService;
	
	@Autowired
	public PasswordChangeController(
			LookupService entityLookupService,
			LoginService loginService,
			SecurityService securityService,
			IdmIdentityService identityService) {
		Assert.notNull(entityLookupService);
		Assert.notNull(loginService);
		Assert.notNull(securityService);
		Assert.notNull(identityService);
		//
		this.entityLookupService = entityLookupService;
		this.loginService = loginService;
		this.securityService = securityService;
		this.identityService = identityService;
	}
	
	/**
	 * Changes identity password. Could be public, because previous password is required.
	 * 
	 * @param identityId
	 * @param passwordChangeDto
	 * @return
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = BaseController.BASE_PATH + "/public/identities/{identityId}/password-change", method = RequestMethod.PUT)
	public ResponseEntity<Void> passwordChange(
			@PathVariable String identityId,
			@RequestBody @Valid PasswordChangeDto passwordChangeDto) {
		// we need to login as identity, if no one is logged in
		try{
			if (!securityService.isAuthenticated()) {
				LoginDto loginDto = new LoginDto();
				loginDto.setSkipMustChange(true);
				loginDto.setUsername(identityId);
				loginDto.setPassword(passwordChangeDto.getOldPassword());
				loginService.login(loginDto);
			}
		} catch(IdmAuthenticationException ex) {
			// TODO: Could be splitted to identity not found / wrong password
			throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
		}
		//
		IdmIdentityDto identity = (IdmIdentityDto) entityLookupService.lookupDto(IdmIdentityDto.class, identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		//
		identityService.checkAccess(identity, IdentityBasePermission.PASSWORDCHANGE);
		//
		identityService.passwordChange(identity, passwordChangeDto);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
