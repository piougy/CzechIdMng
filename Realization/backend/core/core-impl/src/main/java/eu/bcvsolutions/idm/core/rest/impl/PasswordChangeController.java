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
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.security.api.service.SecurityService;
import eu.bcvsolutions.idm.security.service.LoginService;

/**
 * Changes identity password. Could be public, because previous password is required.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
public class PasswordChangeController {
	
	private final EntityLookupService entityLookupService;
	private final LoginService loginService;
	private final SecurityService securityService;
	
	@Autowired
	public PasswordChangeController(
			EntityLookupService entityLookupService,
			LoginService loginService,
			SecurityService securityService) {
		Assert.notNull(entityLookupService);
		Assert.notNull(loginService);
		Assert.notNull(securityService);
		//
		this.entityLookupService = entityLookupService;
		this.loginService = loginService;
		this.securityService = securityService;
	}
	
	/**
	 * Changes identity password. Could be public, because previous password is required.
	 * 
	 * @param identityId
	 * @param passwordChangeDto
	 * @return
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = BaseEntityController.BASE_PATH + "/public/identities/{identityId}/password-change", method = RequestMethod.PUT)
	public ResponseEntity<Void> passwordChange(
			@PathVariable String identityId,
			@RequestBody @Valid PasswordChangeDto passwordChangeDto) {
		// we need to login as identity, if no one is logged in
		if (!securityService.isAuthenticated()) {
			loginService.login(identityId, passwordChangeDto.getOldPassword());
		}
		//
		IdmIdentity identity = (IdmIdentity) entityLookupService.lookup(IdmIdentity.class, identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}		
		getIdentityService().passwordChange(identity, passwordChangeDto);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	private IdmIdentityService getIdentityService() {
		return entityLookupService.getEntityService(IdmIdentity.class, IdmIdentityService.class);
	}
}
