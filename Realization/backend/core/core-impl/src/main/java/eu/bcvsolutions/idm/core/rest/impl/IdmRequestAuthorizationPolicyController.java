package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.AbstractRequestDtoController;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizationEvaluatorDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Requests - Controller for assigning authorization evaluators to roles.
 * 
 * @author Radek Tomiška
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/requests")
@Api(
		value = IdmRequestAuthorizationPolicyController.TAG, 
		description = "Requests for -Operations with authorization policies", 
		tags = { IdmRequestAuthorizationPolicyController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmRequestAuthorizationPolicyController extends AbstractRequestDtoController<IdmAuthorizationPolicyDto, IdmAuthorizationPolicyFilter> {
	
	protected static final String TAG = "Authorization policies";
	protected static final String REQUEST_SUB_PATH = "/authorization-policies";
	private final AuthorizationManager authorizationManager;

	
	@Autowired
	public IdmRequestAuthorizationPolicyController(
			IdmAuthorizationPolicyService service,
			AuthorizationManager authorizationManager) {
		super(service);
		//
		Assert.notNull(authorizationManager);
		//
		this.authorizationManager = authorizationManager;
	}
	
	@Override
	public String getRequestSubPath(){
		return REQUEST_SUB_PATH;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH, method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "Search authorization policies (/search/quick alias)", 
			nickname = "searchAuthorizationPolicies", 
			tags = { IdmRequestAuthorizationPolicyController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") })
				})
	public Resources<?> find(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH + "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "Search authorization policies", 
			nickname = "searchQuickAuthorizationPolicies", 
			tags = { IdmRequestAuthorizationPolicyController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") })
				})
	public Resources<?> findQuick(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH + "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countAuthorizationPolicies", 
			tags = { IdmRequestAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_COUNT, description = "") })
				})
	public long count(@PathVariable @NotNull String requestId, @RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(requestId, parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "Authorization policy detail", 
			nickname = "getAuthorizationPolicy", 
			response = IdmAuthorizationPolicyDto.class, 
			tags = { IdmRequestAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(requestId, backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH, method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE + "')")
	@ApiOperation(
			value = "Create / update authorization policy", 
			nickname = "postAuthorizationPolicy", 
			response = IdmAuthorizationPolicyDto.class, 
			tags = { IdmRequestAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@PathVariable @NotNull String requestId, @Valid @RequestBody IdmAuthorizationPolicyDto dto) {
		return super.post(requestId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE + "')")
	@ApiOperation(
			value = "Update authorization policy", 
			nickname = "putAuthorizationPolicy", 
			response = IdmAuthorizationPolicyDto.class, 
			tags = { IdmRequestAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmAuthorizationPolicyDto dto) {
		return super.put(requestId, backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_DELETE + "')")
	@ApiOperation(
			value = "Delete authorization policy", 
			nickname = "deleteAuthorizationPolicy", 
			tags = { IdmRequestAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(requestId, backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH + "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnAuthorizationPolicy", 
			tags = { IdmRequestAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") })
				})
	public Set<String> getPermissions(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(requestId, backendId);
	}
	
	/**
	 * Returns all registered evaluators
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value= "/{requestId}"+ REQUEST_SUB_PATH + "/search/supported")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "Get all supported evaluators", 
			nickname = "getSupportedAuthorizationEvaluators", 
			tags = { IdmRequestAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") })
				})
	public Resources<AuthorizationEvaluatorDto> getSupportedEvaluators() {
		return new Resources<>(authorizationManager.getSupportedEvaluators());
	}
	
	/**
	 * Returns all registered tasks
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value= "/{requestId}"+ REQUEST_SUB_PATH + "/search/authorizable-types")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "Get all supported authorizable types", 
			nickname = "getAuthorizableTypes", 
			tags = { IdmRequestAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") })
				},
			notes = "Returns all types, with securing data support (by authorization policies).")
	public Resources<AuthorizableType> getAuthorizableTypes() {
		return new Resources<>(authorizationManager.getAuthorizableTypes());
	}
}
