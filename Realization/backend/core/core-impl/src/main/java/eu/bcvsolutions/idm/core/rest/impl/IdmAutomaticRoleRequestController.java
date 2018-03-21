package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleRequestType;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.RequestService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Automatic role request endpoint
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/automatic-role-requests")
@Api(
		value = IdmAutomaticRoleRequestController.TAG, 
		description = "Operations with role requests", 
		tags = { IdmAutomaticRoleRequestController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmAutomaticRoleRequestController extends AbstractReadWriteDtoController<IdmAutomaticRoleRequestDto, IdmAutomaticRoleRequestFilter>{

	protected static final String TAG = "Automatic role request - requests";
	
	@Autowired
	public IdmAutomaticRoleRequestController(
			IdmAutomaticRoleRequestService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@ApiOperation(
			value = "Search role requests (/search/quick alias)", 
			nickname = "searchRoleRequests", 
			tags = { IdmAutomaticRoleRequestController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@ApiOperation(
			value = "Search role requests", 
			nickname = "searchQuickRoleRequests", 
			tags = { IdmAutomaticRoleRequestController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete roles (selectbox usage)", 
			nickname = "autocompleteRoles", 
			tags = { IdmRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@ApiOperation(
			value = "Role request detail", 
			nickname = "getRoleRequest", 
			response = IdmAutomaticRoleRequestDto.class, 
			tags = { IdmAutomaticRoleRequestController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") })
					})
	public ResponseEntity<?> get(
			@ApiParam(value = "Role request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(
			value = "Create / update role request", 
			nickname = "postRoleRequest", 
			response = IdmAutomaticRoleRequestDto.class, 
			tags = { IdmAutomaticRoleRequestController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE, description = "")})
					})
	public ResponseEntity<?> post(@RequestBody @NotNull IdmAutomaticRoleRequestDto request) {
		if (getService().isNew(request)) { 
			request.setResult(new OperationResultDto(OperationState.CREATED));
			request.setState(RequestState.CONCEPT);
			if(request.getRequestType() == null) {
				request.setRequestType(AutomaticRoleRequestType.ATTRIBUTE);
			}
		}
		return super.post(request);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(
			value = "Update role request", 
			nickname = "putRoleRequest", 
			response = IdmAutomaticRoleRequestDto.class, 
			tags = { IdmAutomaticRoleRequestController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE, description = "") })
					})
	public ResponseEntity<?> put(
			@ApiParam(value = "Role request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@RequestBody @NotNull IdmAutomaticRoleRequestDto dto) {
		return super.put(backendId, dto);
	}

	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_DELETE + "')")
	@ApiOperation(
			value = "Delete role request", 
			nickname = "deleteRoleRequest",
			tags = { IdmAutomaticRoleRequestController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_DELETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_DELETE, description = "") })
					})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Role request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmAutomaticRoleRequestService service = ((IdmAutomaticRoleRequestService)this.getService());
		IdmAutomaticRoleRequestDto dto = service.get(backendId);
		//
		checkAccess(dto, IdmBasePermission.DELETE);
		//
		// Request in Executed state can not be delete or change
		if(RequestState.EXECUTED == dto.getState()){
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_EXECUTED_CANNOT_DELETE,
					ImmutableMap.of("request", dto));
		}
		
		// Only request in Concept state, can be deleted. In others states, will be request set to Canceled state and save.
		if(RequestState.CONCEPT == dto.getState()){
			service.delete(dto);
		}else {
			service.cancel(dto);
		}
		
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnRoleRequest", 
			tags = { IdmAutomaticRoleRequestController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/start", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(
			value = "Start role request", 
			nickname = "startRoleRequest", 
			response = IdmAutomaticRoleRequestDto.class, 
			tags = { IdmAutomaticRoleRequestController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE, description = "") })
					})
	public ResponseEntity<?> startRequest(
			@ApiParam(value = "Role request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		((RequestService<?>)this.getService()).startRequest(UUID.fromString(backendId), true);
		return this.get(backendId);
	}
	

	@Override
	protected IdmAutomaticRoleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmAutomaticRoleRequestFilter filter = new IdmAutomaticRoleRequestFilter();
		filter.setRoleId(getParameterConverter().toUuid(parameters, "roleId"));
		filter.setAutomaticRoleId(getParameterConverter().toUuid(parameters, "automaticRole"));
		filter.setRole(getParameterConverter().toString(parameters, "role"));
		filter.setStates(getParameterConverter().toEnums(parameters, "states", RequestState.class));
		filter.setRequestType(getParameterConverter().toEnum(parameters, "requestType", AutomaticRoleRequestType.class));
		return filter;
	}
	
}
