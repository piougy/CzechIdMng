package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import eu.bcvsolutions.idm.core.api.dto.AbstractRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleRequestFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleRequestService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Automatic role rule request endpoint search all entities are available for roleRequestId
 * in filter has to be filled.
 * 
 * @author svandav
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/automatic-role-rule-requests")
@Api(value = IdmAutomaticRoleAttributeRuleRequestController.TAG, description = "Operations with single roles in request", tags = {
		IdmAutomaticRoleAttributeRuleRequestController.TAG }, produces = BaseController.APPLICATION_HAL_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmAutomaticRoleAttributeRuleRequestController extends
		AbstractReadWriteDtoController<IdmAutomaticRoleAttributeRuleRequestDto, IdmAutomaticRoleAttributeRuleRequestFilter> {

	protected static final String TAG = "Automatic role rule request";
	private final IdmAutomaticRoleRequestService automaticRoleRequestService;

	@Autowired
	public IdmAutomaticRoleAttributeRuleRequestController(IdmAutomaticRoleAttributeRuleRequestService service,
			IdmAutomaticRoleRequestService automaticRoleRequestService) {
		super(service);
		//
		Assert.notNull(automaticRoleRequestService);
		//
		this.automaticRoleRequestService = automaticRoleRequestService;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Search rule request role requests (/search/quick alias)", nickname = "searchRule requestRoleRequests", tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_ADMIN, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_ADMIN, description = "") }) })
	public Resources<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Search rule request role requests", nickname = "searchQuickRule requestRoleRequests", tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_ADMIN, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_ADMIN, description = "") }) })
	public Resources<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	public Page<IdmAutomaticRoleAttributeRuleRequestDto> find(IdmAutomaticRoleAttributeRuleRequestFilter filter,
			Pageable pageable, BasePermission permission) {
		// Check access
		// Beware, if has filter requestId filled, then we check permission via right on
		// the request.
		if (filter == null || filter.getRoleRequestId() == null) {
			return super.find(filter, pageable, permission);
		}
		AbstractRequestDto roleRequest = automaticRoleRequestService.get(filter.getRoleRequestId(), permission);
		if (roleRequest == null) {
			// return empty result (find method doesn't throw 404)
			return new PageImpl<>(new ArrayList<>());
		} else {
			return super.find(filter, pageable, null);
		}
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Rule request detail", nickname = "getRule requestRoleRequest", response = IdmAutomaticRoleAttributeRuleRequestDto.class, tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") }) })
	public ResponseEntity<?> get(
			@ApiParam(value = "Rule request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Create / update rule request", nickname = "postRule requestRoleRequest", response = IdmAutomaticRoleAttributeRuleRequestDto.class, tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> post(@RequestBody @NotNull IdmAutomaticRoleAttributeRuleRequestDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Update rule request", nickname = "putRule requestRoleRequest", response = IdmAutomaticRoleAttributeRuleRequestDto.class, tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> put(
			@ApiParam(value = "Rule request's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmAutomaticRoleAttributeRuleRequestDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_DELETE + "')")
	@ApiOperation(value = "Delete rule request", nickname = "delete Rule requestRoleRequest", tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_DELETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_DELETE, description = "") }) })
	public ResponseEntity<?> delete(
			@ApiParam(value = "Rule request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "What logged identity can do with given record", nickname = "getPermissionsOnAutomaticRoleRuleRequest", tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ, description = "") }) })
	public Set<String> getPermissions(
			@ApiParam(value = "Rule request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	protected IdmAutomaticRoleAttributeRuleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmAutomaticRoleAttributeRuleRequestFilter filter = new IdmAutomaticRoleAttributeRuleRequestFilter(parameters);
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setRoleRequestId(getParameterConverter().toUuid(parameters, "roleRequestId"));
		filter.setRoleId(getParameterConverter().toUuid(parameters, "roleId"));
		filter.setAutomaticRoleId(getParameterConverter().toUuid(parameters, "automaticRole"));
		return filter;
	}
}
