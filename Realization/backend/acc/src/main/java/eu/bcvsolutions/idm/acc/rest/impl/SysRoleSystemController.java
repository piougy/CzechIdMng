package eu.bcvsolutions.idm.acc.rest.impl;

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
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.config.domain.RequestConfiguration;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;;

/**
 * Role could assign identity account on target system.
 * 
 * TODO: Authorization policy - transitive by role
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-systems")
@Api(
		value = SysRoleSystemController.TAG, 
		tags = SysRoleSystemController.TAG, 
		description = "Assign system to role",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysRoleSystemController extends AbstractReadWriteDtoController<SysRoleSystemDto, SysRoleSystemFilter> {
	
	protected static final String TAG = "Role system - mappings";
	
	
	@Autowired
	private RequestConfiguration requestConfiguration;
	
	@Autowired
	public SysRoleSystemController(SysRoleSystemService roleSysteService) {
		super(roleSysteService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Search role systems (/search/quick alias)", 
			nickname = "searchRoleSystems",
			tags = { SysRoleSystemController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search role systems", 
			nickname = "searchQuickRoleSystems",
			tags = { SysRoleSystemController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Role system detail", 
			nickname = "getRoleSystem", 
			response = SysRoleSystemDto.class, 
			tags = { SysRoleSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Role system mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update role system", 
			nickname = "postRoleSystem", 
			response = SysRoleSystemDto.class, 
			tags = { SysRoleSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull SysRoleSystemDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update role system",
			nickname = "putRoleSystem", 
			response = SysRoleSystemDto.class, 
			tags = { SysRoleSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Role system mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysRoleSystemDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@ApiOperation(
			value = "Update role system", 
			nickname = "patchRoleSystem", 
			response = SysRoleSystemDto.class, 
			tags = { SysRoleSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Role system mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete role system", 
			nickname = "deleteRoleSystem", 
			tags = { SysRoleSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Role system mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	protected boolean supportsRequests() {
		return requestConfiguration.isRoleRequestEnabled();
	}
	
	@Override
	protected SysRoleSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		SysRoleSystemFilter filter = new SysRoleSystemFilter();
		filter.setRoleId(getParameterConverter().toUuid(parameters, "roleId"));
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		return filter;
	}
}
