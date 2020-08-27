package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccPasswordChangeOptionDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordFilter;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;;

/**
 * Rest controller for standard CRUD operation for uniform password definitions,
 * but also for change a valid method.
 * 
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/uniform-passwords")
@Api(
		value = AccUniformPasswordController.TAG, 
		tags = AccUniformPasswordController.TAG, 
		description = "Uniform password definitions and method for check a valid",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class AccUniformPasswordController extends AbstractReadWriteDtoController<AccUniformPasswordDto, AccUniformPasswordFilter> {

	@Autowired
	private AccUniformPasswordService uniformPasswordService;

	protected static final String TAG = "Uniform password definitions";
	
	@Autowired
	public AccUniformPasswordController(AccUniformPasswordService uniformPasswordService) {
		super(uniformPasswordService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@ApiOperation(
			value = "Search definition for uniform password (/search/quick alias)", 
			nickname = "searchUniformPasswords",
			tags = { AccUniformPasswordController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search definition for uniform password", 
			nickname = "searchQuickUniformPasswords",
			tags = { AccUniformPasswordController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete uniform passwords (selectbox usage)", 
			nickname = "autocompleteUniformPasswords", 
			tags = { AccUniformPasswordController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Uniform password detail", 
			nickname = "getUniformPassword", 
			response = AccUniformPasswordDto.class, 
			tags = { AccUniformPasswordController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Uniform password's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update uniform password definition", 
			nickname = "postUniformPassword", 
			response = AccUniformPasswordDto.class, 
			tags = { AccUniformPasswordController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull AccUniformPasswordDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update uniform password definition",
			nickname = "putUniformPassword", 
			response = AccUniformPasswordDto.class, 
			tags = { AccUniformPasswordController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Uniform password's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccUniformPasswordDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@ApiOperation(
			value = "Update uniform password definition", 
			nickname = "patchUnifromPassword", 
			response = AccUniformPasswordDto.class, 
			tags = { AccUniformPasswordController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Uniform password's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete uniform password definition", 
			nickname = "deleteUniformPassword", 
			tags = { AccUniformPasswordController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Uniform password's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnUniformPassword", 
			tags = { AccUniformPasswordController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Uniform password's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * Find and compose password change options. Options will be united by {@link AccUniformPasswordDto} definition.
	 * Endpoint has permission from account (ACCOUNT_READ) - backward compatibility and returned IDs is account id.
	 *
	 * @param identityIdentifier
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@RequestMapping(value= "/search/password-change-options/{identityIdentifier}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search available password change options for given identity. For call this method must be permissions for read account!", 
			nickname = "searchPasswordChangeOptions",
			tags = { AccUniformPasswordController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") })
				})
	public Resources<?> findPasswordChangeOptions(@PathVariable @NotNull String identityIdentifier) {

		BaseDto identityDto = getLookupService().lookupDto(IdmIdentityDto.class, identityIdentifier);
		
		return toResources(uniformPasswordService.findOptionsForPasswordChange((IdmIdentityDto)identityDto, IdmBasePermission.READ), AccPasswordChangeOptionDto.class);
	}

	@Override
	protected AccUniformPasswordFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccUniformPasswordFilter filter = new AccUniformPasswordFilter(parameters, getParameterConverter());
		return filter;
	}
}
