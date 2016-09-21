package eu.bcvsolutions.idm.core.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityLookup;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.rest.BaseEntityController;

/**
 * Changes identity password. Could be public, because previous password is required.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
public class PasswordChangeController {
	
	@Autowired
	private IdmIdentityLookup identityLookup;

	@Autowired
	private IdmIdentityService identityService;
	
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
		IdmIdentity identity = (IdmIdentity) identityLookup.lookupEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		identityService.passwordChange(identity, passwordChangeDto);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
