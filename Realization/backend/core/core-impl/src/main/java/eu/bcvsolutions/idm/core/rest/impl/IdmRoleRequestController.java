package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Role request endpoint
 * 
 * @author svandav
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-requests")
@Api(
		value = IdmRoleRequestController.TAG, 
		description = "Operations with role requests", 
		tags = { IdmRoleRequestController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmRoleRequestController extends DefaultReadWriteDtoController<IdmRoleRequestDto, RoleRequestFilter>{

	protected static final String TAG = "Role Request - requests";
	
	@Autowired
	public IdmRoleRequestController(IdmRoleRequestService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(
			value = "Search role requests (/search/quick alias)", 
			nickname = "searchRoleRequests", 
			tags = { IdmRoleRequestController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(
			value = "Search role requests", 
			nickname = "searchQuickRoleRequests", 
			tags = { IdmRoleRequestController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(
			value = "Role request detail", 
			nickname = "getRoleRequest", 
			response = IdmRoleRequestDto.class, 
			tags = { IdmRoleRequestController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") })
					})
	public ResponseEntity<?> get(
			@ApiParam(value = "Role request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(
			value = "Create / update role request", 
			nickname = "postRoleRequest", 
			response = IdmRoleRequestDto.class, 
			tags = { IdmRoleRequestController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "")})
					})
	public ResponseEntity<?> post(@RequestBody @NotNull IdmRoleRequestDto dto) {
		if (RoleRequestedByType.AUTOMATICALLY == dto.getRequestedByType()) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_AUTOMATICALLY_NOT_ALLOWED,
					ImmutableMap.of("new", dto));
		}
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(
			value = "Update role request", 
			nickname = "putRoleRequest", 
			response = IdmRoleRequestDto.class, 
			tags = { IdmRoleRequestController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") })
					})
	public ResponseEntity<?> put(
			@ApiParam(value = "Role request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@RequestBody @NotNull IdmRoleRequestDto dto) {
		return super.put(backendId, dto);
	}

	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_DELETE + "')")
	@ApiOperation(
			value = "Delete role request", 
			nickname = "deleteRoleRequest",
			tags = { IdmRoleRequestController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_DELETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_DELETE, description = "") })
					})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Role request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmRoleRequestService service = ((IdmRoleRequestService)this.getService());
		IdmRoleRequestDto dto = service.get(backendId);
		//
		checkAccess(dto, IdmBasePermission.DELETE);
		//
		// Request in Executed state can not be delete or change
		if(RoleRequestState.EXECUTED == dto.getState()){
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_EXECUTED_CANNOT_DELETE,
					ImmutableMap.of("request", dto));
		}
		
		// Only request in Concept state, can be deleted. In others states, will be request set to Canceled state and save.
		if(RoleRequestState.CONCEPT == dto.getState()){
			service.delete(dto);
		}else {
			service.cancel(dto);
		}
		
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnIdentity", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@ResponseBody
//	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_WRITE + "') or hasAuthority('"
//			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	@RequestMapping(value = "/{backendId}/start", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Start role request", 
			nickname = "startRoleRequest", 
			response = IdmRoleRequestDto.class, 
			tags = { IdmRoleRequestController.TAG })
	public ResponseEntity<?> startRequest(
			@ApiParam(value = "Role request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		((IdmRoleRequestService)this.getService()).startRequest(UUID.fromString(backendId), true);
		return this.get(backendId);
	}

	@Override
	protected RoleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		RoleRequestFilter filter = new RoleRequestFilter();
		filter.setApplicant(getParameterConverter().toString(parameters, "applicant"));
		filter.setApplicantId(getParameterConverter().toUuid(parameters, "applicantId"));

		if (filter.getApplicant() != null) {
			try {
				// Applicant can be UUID (Username vs UUID identification schizma)
				// TODO: replace with parameterConverter#toEntityUuid ...
				filter.setApplicantId(UUID.fromString(filter.getApplicant()));
				filter.setApplicant(null);
			} catch (IllegalArgumentException ex) {
				// ok applicant is not UUID
			}
		}
		// TODO: remove redundant state field
		filter.setState(getParameterConverter().toEnum(parameters, "state", RoleRequestState.class));
		String statesStr = getParameterConverter().toString(parameters, "states");
		if(!Strings.isNullOrEmpty(statesStr)){
			List<RoleRequestState> states = new ArrayList<>();
			for( String state : statesStr.split(",")){
				String stateTrimmed = state.trim();
				if(!Strings.isNullOrEmpty(stateTrimmed)){
					// TODO: try / catch - see getParameterConverter().toEnum
					states.add(RoleRequestState.valueOf(stateTrimmed));
				}
			}
			if(!states.isEmpty()){
				filter.setStates(states);
			}
		}
		return filter;
	}
	
}
