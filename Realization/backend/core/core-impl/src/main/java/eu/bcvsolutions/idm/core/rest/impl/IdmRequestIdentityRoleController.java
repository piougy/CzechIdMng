package eu.bcvsolutions.idm.core.rest.impl;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Controller for show and processing wish on assigned identity roles.
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/request-identity-roles")
@Api(value = IdmRequestIdentityRoleController.TAG, description = "Operations with single roles in request", tags = {
		IdmRequestIdentityRoleController.TAG }, produces = BaseController.APPLICATION_HAL_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmRequestIdentityRoleController
		extends AbstractReadWriteDtoController<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter> {

	protected static final String TAG = "Role Request identity-roles";
	@Autowired
	private final IdmRequestIdentityRoleService service;

	@Autowired
	public IdmRequestIdentityRoleController(IdmRequestIdentityRoleService service) {
		super(service);
		Assert.notNull(service);
		this.service = service;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Search concept role requests (/search/quick alias)", nickname = "searchConceptRoleRequests", tags = {
			IdmRequestIdentityRoleController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_ADMIN, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_ADMIN, description = "") }) })
	public Resources<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Search concept role requests", nickname = "searchQuickConceptRoleRequests", tags = {
			IdmRequestIdentityRoleController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_ADMIN, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_ADMIN, description = "") }) })
	public Resources<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}


	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Concept detail", nickname = "getConceptRoleRequest", response = IdmConceptRoleRequestDto.class, tags = {
			IdmRequestIdentityRoleController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
	public ResponseEntity<?> get(
			@ApiParam(value = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	public Page<IdmRequestIdentityRoleDto> find(IdmRequestIdentityRoleFilter filter, Pageable pageable,
			BasePermission permission) {
		// We need to load EAV attributes
		filter.setIncludeEav(true);
		
		Page<IdmRequestIdentityRoleDto> results = super.find(filter, pageable, permission);
		return results;
	}
	

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Create / update request-identity-role", nickname = "postRequest-identity-role", response = IdmRequestIdentityRoleDto.class, tags = {
			IdmRequestIdentityRoleController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> post(@RequestBody @NotNull IdmRequestIdentityRoleDto dto) {
	
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Update request-identity-role", nickname = "putRequest-identity-role", response = IdmRequestIdentityRoleDto.class, tags = {
			IdmRequestIdentityRoleController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> put(
			@ApiParam(value = "Request-identity-role to update.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmRequestIdentityRoleDto dto) {
		return super.put(backendId, dto);
	}

	/**
	 * Delete is realized as PUT, because we need to use 
	 * body (is not supported for DELETE)
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/delete", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_DELETE + "')")
	@ApiOperation(value = "Delete request-identity-role", nickname = "delete request-identity-role", tags = {
			IdmRequestIdentityRoleController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_DELETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_DELETE, description = "") }) })
	public ResponseEntity<?> delete(
			@ApiParam(value = "Request-identity-role to delete.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmRequestIdentityRoleDto dto) {
		dto.setId(UUID.fromString(backendId));
		IdmRequestIdentityRoleDto deletedRequestIdentityRole = service.deleteRequestIdentityRole(dto, IdmBasePermission.DELETE);
		return new ResponseEntity<>(toResource(deletedRequestIdentityRole), HttpStatus.OK);
	}
	
	
	
//
//	@Override
//	@ResponseBody
//	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
//	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
//	@ApiOperation(value = "What logged identity can do with given record", nickname = "getPermissionsOnConceptRoleRequest", tags = {
//			IdmRequestIdentityRoleController.TAG }, authorizations = {
//					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
//							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
//					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
//							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
//	public Set<String> getPermissions(
//			@ApiParam(value = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
//		return super.getPermissions(backendId);
//	}
//
//	/**
//	 * Returns form definition to given entity.
//	 * 
//	 * @param backendId
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
//	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
//	@ApiOperation(value = "Concept extended attributes form definitions", nickname = "getIdentityRoleFormDefinitions", tags = {
//			IdmIdentityRoleController.TAG }, authorizations = {
//					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
//							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
//					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
//							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
//	public ResponseEntity<?> getFormDefinitions(
//			@ApiParam(value = "Role's uuid identifier or code.", required = true) @PathVariable @NotNull String backendId) {
//		IdmConceptRoleRequestDto dto = getDto(backendId);
//		if (dto == null) {
//			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
//		}
//		// Search definition by definition in role
//		IdmRoleDto roleDto = DtoUtils.getEmbedded(dto, IdmConceptRoleRequest_.role, IdmRoleDto.class);
//		if (roleDto != null && roleDto.getIdentityRoleAttributeDefinition() != null) {
//			return formDefinitionController.getDefinitions(roleDto.getIdentityRoleAttributeDefinition());
//		}
//
//		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//	}
//
//	/**
//	 * Returns entity's filled form values
//	 * 
//	 * @param backendId
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
//	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
//	@ApiOperation(value = "Concept form definition - read values", nickname = "getRoleFormValues", tags = {
//			IdmIdentityRoleController.TAG }, authorizations = {
//					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
//							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
//					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
//							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
//	public Resource<?> getFormValues(
//			@ApiParam(value = "Concept's uuid identifier or code.", required = true) @PathVariable @NotNull String backendId,
//			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE) @RequestParam(name = "definitionCode", required = false) String definitionCode) {
//		IdmConceptRoleRequestDto dto = getDto(backendId);
//		if (dto == null) {
//			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
//		}
//		//
//		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmConceptRoleRequest.class,
//				definitionCode);
//		//
//		return formDefinitionController.getFormValues(dto, formDefinition);
//	}
//
//	/**
//	 * Saves entity's form values
//	 * 
//	 * @param backendId
//	 * @param formValues
//	 * @return
//	 */
//	@ResponseBody
//	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
//	@RequestMapping(value = "/{backendId}/form-values", method = { RequestMethod.POST, RequestMethod.PATCH })
//	@ApiOperation(value = "Concept form definition - save values", nickname = "postIdentityRoleFormValues", tags = {
//			IdmIdentityRoleController.TAG }, authorizations = {
//					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
//							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }),
//					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
//							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }) })
//	public Resource<?> saveFormValues(
//			@ApiParam(value = "Concept's uuid identifier or code.", required = true) @PathVariable @NotNull String backendId,
//			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE) @RequestParam(name = "definitionCode", required = false) String definitionCode,
//			@RequestBody @Valid List<IdmFormValueDto> formValues) {
//		IdmConceptRoleRequestDto dto = getDto(backendId);
//		if (dto == null) {
//			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
//		}
//		checkAccess(dto, IdmBasePermission.UPDATE);
//		//
//		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmConceptRoleRequest.class,
//				definitionCode);
//		//
//		return formDefinitionController.saveFormValues(dto, formDefinition, formValues);
//	}

	@Override
	protected IdmRequestIdentityRoleFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmRequestIdentityRoleFilter filter = new IdmRequestIdentityRoleFilter(parameters);
		filter.setIdentityId(getParameterConverter().toUuid(parameters, "identityId"));
		filter.setIdentityContractId(getParameterConverter().toUuid(parameters, "identityContractId"));
		filter.setRoleId(getParameterConverter().toUuid(parameters, "roleId"));
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setRoleEnvironment(getParameterConverter().toString(parameters, "roleEnvironment"));
		filter.setRoleRequestId(getParameterConverter().toUuid(parameters, "roleRequestId"));
		filter.setState(getParameterConverter().toEnum(parameters, "state", RoleRequestState.class));
		Boolean onlyChanges = getParameterConverter().toBoolean(parameters, "onlyChanges");
		filter.setOnlyChanges(onlyChanges == Boolean.TRUE ? true : false);
		return filter;
	}
}
