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
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordSystemService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;;

/**
 * Rest controller for standard CRUD operation for uniform password connection between
 * {@link AccUniformPasswordDto} and {@link SysSystemDto}.
 * Controller has same permission as controller {@link AccUniformPasswordController}
 * 
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/uniform-password-systems")
@Api(
		value = AccUniformPasswordSystemController.TAG, 
		tags = AccUniformPasswordSystemController.TAG, 
		description = "Uniform password conection with systems",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class AccUniformPasswordSystemController extends AbstractReadWriteDtoController<AccUniformPasswordSystemDto, AccUniformPasswordSystemFilter> {
	
	protected static final String TAG = "Uniform password systems";
	
	@Autowired
	public AccUniformPasswordSystemController(AccUniformPasswordSystemService uniformPasswordSystemService) {
		super(uniformPasswordSystemService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@ApiOperation(
			value = "Search definition for uniform password and system (/search/quick alias)", 
			nickname = "searchUniformPasswordSystems",
			tags = { AccUniformPasswordSystemController.TAG }, 
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
			value = "Search uniform password system", 
			nickname = "searchQuickUniformPasswordSystems",
			tags = { AccUniformPasswordSystemController.TAG }, 
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
			value = "Autocomplete uniform password systems (selectbox usage)", 
			nickname = "autocompleteUniformPasswordSystems", 
			tags = { AccUniformPasswordSystemController.TAG }, 
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
			value = "Uniform password system detail", 
			nickname = "getUniformPasswordSystem", 
			response = AccUniformPasswordSystemDto.class, 
			tags = { AccUniformPasswordSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Uniform password system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update uniform password system", 
			nickname = "postUniformPasswordSystem", 
			response = AccUniformPasswordSystemDto.class, 
			tags = { AccUniformPasswordSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull AccUniformPasswordSystemDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update uniform password system",
			nickname = "putUniformPassword", 
			response = AccUniformPasswordSystemDto.class, 
			tags = { AccUniformPasswordSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Uniform password system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccUniformPasswordSystemDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@ApiOperation(
			value = "Update uniform password system", 
			nickname = "patchUniformPasswordSystem", 
			response = AccUniformPasswordSystemDto.class, 
			tags = { AccUniformPasswordSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Uniform password system's uuid identifier.", required = true)
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
			value = "Delete uniform password system", 
			nickname = "deleteUniformPassword", 
			tags = { AccUniformPasswordSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Uniform password system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnUniformPasswordSystem", 
			tags = { AccUniformPasswordSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.UNIFORM_PASSWORD_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Uniform password system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	
	@Override
	protected AccUniformPasswordSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccUniformPasswordSystemFilter filter = new AccUniformPasswordSystemFilter(parameters, getParameterConverter());
		return filter;
	}
}
