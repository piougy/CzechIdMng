package eu.bcvsolutions.idm.core.rest;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.RestApplicationException;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityLookup;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.service.SecurityService;

@RestController
@RequestMapping(value = "/api/identities/")
public class IdmIdentityController {

	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmIdentityLookup identityLookup;
	
	@Autowired
	private SecurityService securityService;

	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "{identityId}/password-change", method = RequestMethod.PUT)
	public ResponseEntity<Void> passwordChange(@PathVariable String identityId, @RequestBody @Valid PasswordChangeDto passwordChangeDto) {
		IdmIdentity identity = (IdmIdentity)identityLookup.lookupEntity(identityId);
		if (identity == null) {
			throw new RestApplicationException(CoreResultCode.NOT_FOUND, new Object[]{ identityId });
		}
		// TODO: settingResource + superAdminRole
		if (!securityService.hasAnyRole("superAdminRole") && !StringUtils.equals(new String(identity.getPassword()), new String(passwordChangeDto.getOldPassword()))) {
			throw new RestApplicationException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
		}
		identity.setPassword(passwordChangeDto.getNewPassword());
		identityRepository.save(identity);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
