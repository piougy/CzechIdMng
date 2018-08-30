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
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizationEvaluatorDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Controller for assigning authorization evaluators to roles.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/authorization-policies")
@Api(
		value = IdmAuthorizationPolicyController.TAG, 
		description = "Operations with authorization policies", 
		tags = { IdmAuthorizationPolicyController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmAuthorizationPolicyController extends AbstractReadWriteDtoController<IdmAuthorizationPolicyDto, IdmAuthorizationPolicyFilter> {
	
	protected static final String TAG = "Authorization policies";
	private final AuthorizationManager authorizationManager;
	
	@Autowired
	public IdmAuthorizationPolicyController(
			IdmAuthorizationPolicyService service,
			AuthorizationManager authorizationManager) {
		super(service);
		//
		Assert.notNull(authorizationManager);
		//
		this.authorizationManager = authorizationManager;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "Search authorization policies (/search/quick alias)", 
			nickname = "searchAuthorizationPolicies", 
			tags = { IdmAuthorizationPolicyController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "Search authorization policies", 
			nickname = "searchQuickAuthorizationPolicies", 
			tags = { IdmAuthorizationPolicyController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countAuthorizationPolicies", 
			tags = { IdmAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "Authorization policy detail", 
			nickname = "getAuthorizationPolicy", 
			response = IdmAuthorizationPolicyDto.class, 
			tags = { IdmAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE + "')")
	@ApiOperation(
			value = "Create / update authorization policy", 
			nickname = "postAuthorizationPolicy", 
			response = IdmAuthorizationPolicyDto.class, 
			tags = { IdmAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmAuthorizationPolicyDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE + "')")
	@ApiOperation(
			value = "Update authorization policy", 
			nickname = "putAuthorizationPolicy", 
			response = IdmAuthorizationPolicyDto.class, 
			tags = { IdmAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmAuthorizationPolicyDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_DELETE + "')")
	@ApiOperation(
			value = "Delete authorization policy", 
			nickname = "deleteAuthorizationPolicy", 
			tags = { IdmAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnAuthorizationPolicy", 
			tags = { IdmAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTHORIZATIONPOLICY_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	/**
	 * Returns all registered evaluators
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "Get all supported evaluators", 
			nickname = "getSupportedAuthorizationEvaluators", 
			tags = { IdmAuthorizationPolicyController.TAG }, 
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
	@RequestMapping(method = RequestMethod.GET, value = "/search/authorizable-types")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@ApiOperation(
			value = "Get all supported authorizable types", 
			nickname = "getAuthorizableTypes", 
			tags = { IdmAuthorizationPolicyController.TAG }, 
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
