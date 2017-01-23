package eu.bcvsolutions.idm.core.rest.impl;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.model.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.model.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;

/**
 * Default controller for password policy
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/password-policies")
public class IdmPasswordPolicyController extends DefaultReadWriteEntityController<IdmPasswordPolicy, PasswordPolicyFilter> {
	
	private final IdmPasswordPolicyService passwordPolicyService;
	
	@Autowired
	public IdmPasswordPolicyController(EntityLookupService entityLookupService, IdmPasswordPolicyService passwordPolicyService) {
		super(entityLookupService);
		this.passwordPolicyService = passwordPolicyService;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.PASSWORDPOLICY_WRITE + "')")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.PASSWORDPOLICY_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.PASSWORDPOLICY_WRITE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.PASSWORDPOLICY_WRITE + "')")
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}
	
	/**
	 * Return generate password by password policy.
	 * Check password policy type.
	 * 
	 * @param entityId
	 * @return string, new password
	 */
	@ResponseBody
	@RequestMapping(value = "/{entityId}/generate", method = RequestMethod.GET)
	public String generate(@PathVariable String entityId) {
		IdmPasswordPolicy entity = getPasswordPolicy(entityId);
		//
		return this.passwordPolicyService.generatePassword(entity);
	}
	
	/**
	 * Validate password by given password policy id
	 * 
	 * @param entityId
	 * @return
	 */
	@RequestMapping(value = "/{entityId}/validate", method = RequestMethod.POST)
	public ResourceWrapper<IdmPasswordValidationDto> validate(@Valid @RequestBody(required = true) IdmPasswordValidationDto password, @PathVariable String entityId) {
		IdmPasswordPolicy passwordPolicy = getPasswordPolicy(entityId);
		
		if (this.passwordPolicyService.validate(password, passwordPolicy)) {
			password.setValid(true);
		}
		return new ResourceWrapper<IdmPasswordValidationDto>(password);
	}
	
	/**
	 * Validate password by default validate policy
	 * 
	 * @return
	 */
	@RequestMapping(value = "/validate", method = RequestMethod.POST)
	public ResourceWrapper<IdmPasswordValidationDto> validateByDefault(@Valid @RequestBody(required = true) IdmPasswordValidationDto password) {
		if (this.passwordPolicyService.validate(password)) {
			password.setValid(true);
		}
		return new ResourceWrapper<IdmPasswordValidationDto>(password);
	}
	
	
	/**
	 * Method generate password by default generate password policy.
	 * This policy is only one.
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/generate", method = RequestMethod.GET)
	public String generateByDefaultPolicy() {
		return passwordPolicyService.generatePasswordByDefault();
	}
	
	
	private IdmPasswordPolicy getPasswordPolicy(String entityId) {
		IdmPasswordPolicy entity = this.getEntity(entityId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", entityId));
		} else if (entity.getType() == IdmPasswordPolicyType.VALIDATE) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_BAD_TYPE, ImmutableMap.of("type", entity.getType()));
		}
		return entity;
	}
}
