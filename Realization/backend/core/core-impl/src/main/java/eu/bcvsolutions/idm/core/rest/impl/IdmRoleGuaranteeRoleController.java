package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Role guarantee controller - by role
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-guarantee-roles")
@Api(
		value = IdmRoleGuaranteeRoleController.TAG, 
		description = "Operations with role guarantees by role", 
		tags = { IdmRoleGuaranteeRoleController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmRoleGuaranteeRoleController extends AbstractEventableDtoController<IdmRoleGuaranteeRoleDto, IdmRoleGuaranteeRoleFilter> {
	
	protected static final String TAG = "Role guarantees - by roles";
	
	@Autowired
	public IdmRoleGuaranteeRoleController(IdmRoleGuaranteeRoleService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_READ + "')")
	@ApiOperation(
			value = "Search role guarantee roles (/search/quick alias)", 
			nickname = "searchRoleGuaranteeRoles", 
			tags = { IdmRoleGuaranteeRoleController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_READ + "')")
	@ApiOperation(
			value = "Search role guarantee roles", 
			nickname = "searchQuickRoleGuaranteeRoles", 
			tags = { IdmRoleGuaranteeRoleController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete role guarantee roles (selectbox usage)", 
			nickname = "autocompleteRoleGuaranteeRoles", 
			tags = { IdmRoleGuaranteeRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countRoleGuaranteeRoles", 
			tags = { IdmRoleGuaranteeRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_READ + "')")
	@ApiOperation(
			value = "Role guarantee role detail", 
			nickname = "getRoleGuaranteeRole", 
			response = IdmRoleGuaranteeRoleDto.class, 
			tags = { IdmRoleGuaranteeRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Role guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update role guarantee role", 
			nickname = "postRoleGuaranteeRole", 
			response = IdmRoleGuaranteeRoleDto.class, 
			tags = { IdmRoleGuaranteeRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmRoleGuaranteeRoleDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_UPDATE + "')")
	@ApiOperation(
			value = "Update role guarantee role", 
			nickname = "putRoleGuaranteeRole", 
			response = IdmRoleGuaranteeRoleDto.class, 
			tags = { IdmRoleGuaranteeRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Role guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmRoleGuaranteeRoleDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_UPDATE + "')")
	@ApiOperation(
			value = "Update role guarantee role", 
			nickname = "patchRoleGuaranteeRole", 
			response = IdmRoleGuaranteeRoleDto.class, 
			tags = { IdmRoleGuaranteeRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Role guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_DELETE + "')")
	@ApiOperation(
			value = "Delete role guarantee role", 
			nickname = "deleteRoleGuaranteeRole", 
			tags = { IdmRoleGuaranteeRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Role guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEEROLE_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnRoleGuaranteeRole", 
			tags = { IdmRoleGuaranteeRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLEGUARANTEEROLE_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Role guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
