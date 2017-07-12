package eu.bcvsolutions.idm.core.rest.impl;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Default controller for password policy
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/password-policies")
@Api(
		value = IdmPasswordPolicyController.TAG, 
		tags = IdmPasswordPolicyController.TAG, 
		description = "Operations with password policies",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmPasswordPolicyController extends DefaultReadWriteEntityController<IdmPasswordPolicy, PasswordPolicyFilter> {
	
	protected static final String TAG = "Password policies";
	private final IdmPasswordPolicyService passwordPolicyService;
	
	@Autowired
	public IdmPasswordPolicyController(LookupService entityLookupService, IdmPasswordPolicyService passwordPolicyService) {
		super(entityLookupService);
		this.passwordPolicyService = passwordPolicyService;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PASSWORDPOLICY_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.PASSWORDPOLICY_UPDATE + "')")
	@ApiOperation(
			value = "Create / update password policy", 
			nickname = "postPasswordPolicy", 
			response = IdmPasswordPolicy.class, 
			tags = { IdmPasswordPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.PASSWORDPOLICY_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.PASSWORDPOLICY_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.PASSWORDPOLICY_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.PASSWORDPOLICY_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PASSWORDPOLICY_UPDATE + "')")
	@ApiOperation(
			value = "Update password policy",
			nickname = "putPasswordPolicy", 
			response = IdmPasswordPolicy.class, 
			tags = { IdmPasswordPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.PASSWORDPOLICY_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.PASSWORDPOLICY_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PASSWORDPOLICY_UPDATE + "')")
	@ApiOperation(
			value = "Update password policy",
			nickname = "patchPasswordPolicy", 
			response = IdmPasswordPolicy.class, 
			tags = { IdmPasswordPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.PASSWORDPOLICY_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.PASSWORDPOLICY_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PASSWORDPOLICY_DELETE + "')")
	@ApiOperation(
			value = "Delete password policy", 
			nickname = "deletePasswordPolicy", 
			tags = { IdmPasswordPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.PASSWORDPOLICY_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.PASSWORDPOLICY_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	/**
	 * Return generate password by password policy.
	 * Check password policy type.
	 * 
	 * @param backendId
	 * @return string, new password
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/generate", method = RequestMethod.GET)
	@ApiOperation(
			value = "Generate password", 
			nickname = "generatePassword", 
			tags = { IdmPasswordPolicyController.TAG },
			notes = "Returns generated password by password policy.")
	public String generate(
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable String backendId) {
		IdmPasswordPolicy entity = getPasswordPolicy(backendId);
		//
		return this.passwordPolicyService.generatePassword(entity);
	}
	
	/**
	 * Validate password by given password policy id
	 * 
	 * @param backendId
	 * @return
	 */
	@RequestMapping(value = "/{backendId}/validate", method = RequestMethod.POST)
	@ApiOperation(
			value = "Validate password", 
			nickname = "validatePassword", 
			response = IdmPasswordValidationDto.class,
			tags = { IdmPasswordPolicyController.TAG },
			notes = "Validate password by password policy.")
	public ResourceWrapper<IdmPasswordValidationDto> validate(
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable String backendId,
			@Valid @RequestBody(required = true) IdmPasswordValidationDto password) {
		IdmPasswordPolicy passwordPolicy = getPasswordPolicy(backendId);
		//
		this.passwordPolicyService.validate(password, passwordPolicy);
		//
		password.setValid(true);
		//
		return new ResourceWrapper<IdmPasswordValidationDto>(password);
	}
	
	/**
	 * Validate password by default validate policy
	 * 
	 * @return
	 */
	@RequestMapping(value = "/validate/default", method = RequestMethod.POST)
	@ApiOperation(
			value = "Validate password (by default policy)", 
			nickname = "validatePasswordByDefault", 
			response = IdmPasswordValidationDto.class,
			tags = { IdmPasswordPolicyController.TAG },
			notes = "Validate password by default password policy.")
	public ResourceWrapper<IdmPasswordValidationDto> validateByDefault(@Valid @RequestBody(required = true) IdmPasswordValidationDto password) {
		this.passwordPolicyService.validate(password);
		//
		password.setValid(true);
		//
		return new ResourceWrapper<IdmPasswordValidationDto>(password);
	}
	
	
	/**
	 * Method generate password by default generate password policy.
	 * This policy is only one.
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/generate/default", method = RequestMethod.GET)
	@ApiOperation(
			value = "Generate password (by default policy)", 
			nickname = "genaratePasswordByDefault", 
			response = IdmPasswordValidationDto.class,
			tags = { IdmPasswordPolicyController.TAG },
			notes = "Returns generated password by default password policy.")
	public String generateByDefaultPolicy() {
		return passwordPolicyService.generatePasswordByDefault();
	}
	
	
	private IdmPasswordPolicy getPasswordPolicy(String backendId) {
		IdmPasswordPolicy entity = this.getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		} else if (entity.getType() == IdmPasswordPolicyType.VALIDATE) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_BAD_TYPE, ImmutableMap.of("type", entity.getType()));
		}
		return entity;
	}
}
