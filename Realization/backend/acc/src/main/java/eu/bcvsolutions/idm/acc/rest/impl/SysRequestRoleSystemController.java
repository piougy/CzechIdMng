package eu.bcvsolutions.idm.acc.rest.impl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.AbstractRequestDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;;

/**
 * Role-system request - Role could assign identity account on target system.
 * 
 * @author svandav
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/requests")
@Api(
		value = SysRequestRoleSystemController.TAG, 
		tags = SysRequestRoleSystemController.TAG, 
		description = "Reqeusts for - Assign system to role",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysRequestRoleSystemController extends AbstractRequestDtoController<SysRoleSystemDto, SysRoleSystemFilter> {
	
	protected static final String TAG = "Role system - mappings";
	protected static final String REQUEST_SUB_PATH = "/role-systems";
	
	@Autowired
	public SysRequestRoleSystemController(SysRoleSystemService roleSysteService) {
		super(roleSysteService);
	}
	
	@Override
	public String getRequestSubPath(){
		return REQUEST_SUB_PATH;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH, method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Search role systems (/search/quick alias)", 
			nickname = "searchRoleSystems",
			tags = { SysRequestRoleSystemController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public Resources<?> find(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH+"/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search role systems", 
			nickname = "searchQuickRoleSystems",
			tags = { SysRequestRoleSystemController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Role system detail", 
			nickname = "getRoleSystem", 
			response = SysRoleSystemDto.class, 
			tags = { SysRequestRoleSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role system mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(requestId, backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH, method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update role system", 
			nickname = "postRoleSystem", 
			response = SysRoleSystemDto.class, 
			tags = { SysRequestRoleSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@PathVariable @NotNull String requestId, @RequestBody @NotNull SysRoleSystemDto dto) {
		return super.post(requestId, dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH +"/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update role system",
			nickname = "putRoleSystem", 
			response = SysRoleSystemDto.class, 
			tags = { SysRequestRoleSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role system mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysRoleSystemDto dto) {
		return super.put(requestId, backendId, dto);
	}
	
//	@Override
//	@ResponseBody
//	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
//	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
//	@ApiOperation(
//			value = "Update role system", 
//			nickname = "patchRoleSystem", 
//			response = SysRoleSystemDto.class, 
//			tags = { SysRequestRoleSystemController.TAG }, 
//			authorizations = { 
//				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
//						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }),
//				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
//						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") })
//				})
//	public ResponseEntity<?> patch(
//			@ApiParam(value = "Role system mapping's uuid identifier.", required = true)
//			@PathVariable @NotNull String backendId,
//			HttpServletRequest nativeRequest)
//			throws HttpMessageNotReadableException {
//		return super.patch(backendId, nativeRequest);
//	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete role system", 
			nickname = "deleteRoleSystem", 
			tags = { SysRequestRoleSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role system mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(requestId, backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = REQUEST_SUB_PATH, method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.ROLE_UPDATE + "')")
	@ApiOperation( //
			value = "Create request for role system", //
			nickname = "createRequestForRoleSystem", //
			response = SysRoleSystemDto.class, //
			tags = { SysRequestRoleSystemController.TAG }, //
			authorizations = { //
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { //
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_CREATE, description = ""), //
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }), //
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { //
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_CREATE, description = ""), //
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }) //
			}) //
	public ResponseEntity<?> createRequest(@Valid @RequestBody SysRoleSystemDto dto) {
		return super.createRequest(dto);
	}
	
	
	@Override
	protected SysRoleSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		SysRoleSystemFilter filter = new SysRoleSystemFilter();
		filter.setRoleId(getParameterConverter().toUuid(parameters, "roleId"));
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		return filter;
	}
}
