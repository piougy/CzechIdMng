package eu.bcvsolutions.idm.acc.rest.impl;

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
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.AbstractRequestDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;;

/**
 * Request - Role system attribute rest
 * 
 * @author svandav
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/requests")
@Api(
		value = SysRequestRoleSystemAttributeController.TAG, 
		tags = SysRequestRoleSystemAttributeController.TAG, 
		description = "Requests for - Override system mapping attribute by role",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysRequestRoleSystemAttributeController
		extends AbstractRequestDtoController<SysRoleSystemAttributeDto, SysRoleSystemAttributeFilter> {

	protected static final String TAG = "Request - role system - attributes";	
	protected static final String REQUEST_SUB_PATH = "/role-system-attributes";

	@Override
	public String getRequestSubPath(){
		return REQUEST_SUB_PATH;
	}
	
	@Autowired
	public SysRequestRoleSystemAttributeController(SysRoleSystemAttributeService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH, method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Search role system attributes (/search/quick alias)", 
			nickname = "searchRoleSystemAttributes",
			tags = { SysRequestRoleSystemAttributeController.TAG }, 
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

	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH + "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search role system attributes", 
			nickname = "searchQuickRoleSystemAttributes",
			tags = { SysRequestRoleSystemAttributeController.TAG }, 
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
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Role system attribute detail", 
			nickname = "getRoleSystemAttribute", 
			response = SysRoleSystemAttributeDto.class, 
			tags = { SysRequestRoleSystemAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role system attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(requestId, backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH, method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update role system attribute", 
			nickname = "postRoleSystemAttribute", 
			response = SysRoleSystemAttributeDto.class, 
			tags = { SysRequestRoleSystemAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(
			@PathVariable @NotNull String requestId,
			@RequestBody @NotNull SysRoleSystemAttributeDto dto) {
		return super.post(requestId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update role system attribute",
			nickname = "putRoleSystemAttribute", 
			response = SysRoleSystemAttributeDto.class, 
			tags = { SysRequestRoleSystemAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role system attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysRoleSystemAttributeDto dto) {
		return super.put(requestId, backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_DELETE + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete role system attribute", 
			nickname = "deleteRoleSystemAttribute", 
			tags = { SysRequestRoleSystemAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role system attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(requestId, backendId);
	}
}
