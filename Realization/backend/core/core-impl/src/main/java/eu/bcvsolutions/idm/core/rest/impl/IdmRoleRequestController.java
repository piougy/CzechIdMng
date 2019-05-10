package eu.bcvsolutions.idm.core.rest.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
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
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestByIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleRequestApprovalProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-requests")
@Api(value = IdmRoleRequestController.TAG, description = "Operations with role requests", tags = {
		IdmRoleRequestController.TAG }, produces = BaseController.APPLICATION_HAL_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmRoleRequestController extends AbstractReadWriteDtoController<IdmRoleRequestDto, IdmRoleRequestFilter> {

	protected static final String TAG = "Role Request - requests";
	//
	private final IdmConceptRoleRequestController conceptRoleRequestController;
	private final IdmRoleRequestService service;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmConceptRoleRequestService conceptService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Autowired
	public IdmRoleRequestController(IdmRoleRequestService service,
			IdmConceptRoleRequestController conceptRoleRequestController) {
		super(service);
		//
		Assert.notNull(conceptRoleRequestController);
		Assert.notNull(service);
		//
		this.conceptRoleRequestController = conceptRoleRequestController;
		this.service = service;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Search role requests (/search/quick alias)", nickname = "searchRoleRequests", tags = {
			IdmRoleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
	public Resources<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Search role requests", nickname = "searchQuickRoleRequests", tags = {
			IdmRoleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
	public Resources<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countRoleRequests", 
			tags = { IdmRoleRequestController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Role request detail", nickname = "getRoleRequest", response = IdmRoleRequestDto.class, tags = {
			IdmRoleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
	public ResponseEntity<?> get(
			@ApiParam(value = "Role request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		ResponseEntity<?> response = super.get(backendId);
		this.addMetadataToConcepts(response);
		return response;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Create / update role request", nickname = "postRoleRequest", response = IdmRoleRequestDto.class, tags = {
			IdmRoleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> post(@RequestBody @NotNull IdmRoleRequestDto dto) {
		if (RoleRequestedByType.AUTOMATICALLY == dto.getRequestedByType()) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_AUTOMATICALLY_NOT_ALLOWED,
					ImmutableMap.of("new", dto));
		}
		ResponseEntity<?> response = super.post(dto);
		this.addMetadataToConcepts(response);
		return response;
	}


	@RequestMapping(value = "/{backendId}/copy-roles", method = RequestMethod.POST)
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
	public ResponseEntity<?> copyRoles(@ApiParam(value = "Role request's uuid identifier.", required = true)
	@PathVariable @NotNull String backendId, @RequestBody @NotNull IdmRoleRequestByIdentityDto dto) {
		dto.setRoleRequest(UUID.fromString(backendId));
		IdmRoleRequestDto roleRequest = this.service.copyRolesByIdentity(dto);

		return new ResponseEntity<Object>(roleRequest, HttpStatus.OK);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Update role request", nickname = "putRoleRequest", response = IdmRoleRequestDto.class, tags = {
			IdmRoleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> put(
			@ApiParam(value = "Role request's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmRoleRequestDto dto) {
		ResponseEntity<?> response =  super.put(backendId, dto);
		this.addMetadataToConcepts(response);
		return response;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_DELETE + "')")
	@ApiOperation(value = "Delete role request", nickname = "deleteRoleRequest", tags = {
			IdmRoleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_DELETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_DELETE, description = "") }) })
	public ResponseEntity<?> delete(
			@ApiParam(value = "Role request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		IdmRoleRequestService service = ((IdmRoleRequestService) this.getService());
		IdmRoleRequestDto dto = getDto(backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		//
		checkAccess(dto, IdmBasePermission.DELETE);
		//
		// Request in Executed state can not be delete or change
		if (RoleRequestState.EXECUTED == dto.getState()) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_EXECUTED_CANNOT_DELETE,
					ImmutableMap.of("request", dto));
		}

		// Only request in Concept state, can be deleted. In others states, will be
		// request set to Canceled state and save.
		if (RoleRequestState.CONCEPT == dto.getState()) {
			service.delete(dto);
		} else {
			service.cancel(dto);
		}

		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "What logged identity can do with given record", nickname = "getPermissionsOnRoleRequest", tags = {
			IdmRoleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
	public Set<String> getPermissions(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true) @PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/start", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Start role request", nickname = "startRoleRequest", response = IdmRoleRequestDto.class, tags = {
			IdmRoleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> startRequest(
			@ApiParam(value = "Role request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		IdmRoleRequestDto requestDto = this.getDto(backendId);
		// Validate
		service.validate(requestDto);
		// Start request
		Map<String, Serializable> variables = new HashMap<>();
		variables.put(RoleRequestApprovalProcessor.CHECK_RIGHT_PROPERTY, Boolean.TRUE);
		RoleRequestEvent event = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, requestDto, variables);
		event.setPriority(PriorityType.HIGH);
		//
		requestDto = service.startRequest(event);
		if(!requestDto.getState().isTerminatedState()) {
			throw new AcceptedException();
		}
		ResourceSupport resource = toResource(requestDto);
		ResponseEntity<ResourceSupport> response = new ResponseEntity<>(resource, HttpStatus.OK);
		addMetadataToConcepts(response);
		
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/concepts", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Role request concepts", nickname = "getRoleRequestConcepts", tags = {
			IdmRoleRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "parameters", allowMultiple = true, dataType = "string", paramType = "query", value = "Search criteria parameters. Parameters could be registered by module. Example id=25c5b9e8-b15d-4f95-b715-c7edf6f4aee6"),
			@ApiImplicitParam(name = "page", dataType = "string", paramType = "query", value = "Results page you want to retrieve (0..N)"),
			@ApiImplicitParam(name = "size", dataType = "string", paramType = "query", value = "Number of records per page."),
			@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(,asc|desc). "
					+ "Default sort order is ascending. " + "Multiple sort criteria are supported.") })
	public Resources<?> getConcepts(
			@ApiParam(value = "Role request's uuid identifier.", required = true) @PathVariable String backendId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		IdmRoleRequestDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmConceptRoleRequestFilter filter = conceptRoleRequestController.toFilter(parameters);
		filter.setRoleRequestId(entity.getId());
		//
		return toResources(conceptRoleRequestController.find(filter, pageable, IdmBasePermission.READ),
				IdmRoleRequestDto.class);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/incompatible-roles", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(
			value = "Incompatible roles related to this request", 
			nickname = "getRoleRequestIncompatibleRoles", 
			tags = { IdmRoleRequestController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") })
				},
			notes = "Incompatible roles are resolved from currently assigned identity roles (which can logged used read) and the current request concepts.")
	public Resources<?> getIncompatibleRoles(
			@ApiParam(value = "Role request's uuid identifier.", required = true)
			@PathVariable String backendId) {	
		IdmRoleRequestDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		Set<ResolvedIncompatibleRoleDto> incompatibleRoles = service.getIncompatibleRoles(entity, IdmBasePermission.READ);
		//
		return toResources(incompatibleRoles, ResolvedIncompatibleRoleDto.class);
	}

	
	private void addMetadataToConcepts(ResponseEntity<?> response) {
		if(response != null && response.getBody() instanceof Resource) {
			@SuppressWarnings("unchecked")
			Resource<IdmRoleRequestDto> resource = (Resource<IdmRoleRequestDto>) response.getBody();
			this.addMetadataToConcepts(resource.getContent());
		}
	}

	/**
	 * Fill each {@link IdmConceptRoleRequestDto} with metadata abou EAV's values and changes.
	 * And also set duplicates for each concept and another concepts or identity role.
	 *
	 * @param dto
	 * @return
	 */
	private IdmRoleRequestDto addMetadataToConcepts(IdmRoleRequestDto dto) {
		if (dto != null) {
			// TODO: concepts will be removed from request
			// Add EAV values and evaluate changes on EAV values for concepts 
			dto.getConceptRoles().stream() //
			.filter(concept -> ConceptRoleRequestOperation.REMOVE != concept.getOperation()) //
			.forEach(concept -> { //
				IdmFormInstanceDto formInstanceDto = conceptService.getRoleAttributeValues(concept, true);
				if (formInstanceDto != null) {
					concept.getEavs().clear();
					concept.getEavs().add(formInstanceDto);
					// Validate the concept
					List<InvalidFormAttributeDto> validationResults = formService.validate(formInstanceDto);
					formInstanceDto.setValidationErrors(formService.validate(formInstanceDto));
					if (!validationResults.isEmpty()) {
						// Concept is not valid (no other metadata for validation problem is not
						// necessary now).
						concept.setValid(false);
					}
				}
			});

			// Mark duplicates
			UUID identityId = dto.getApplicant();
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.findValidRoles(identityId, null).getContent();
			// Add to all identity roles form instance. For identity role can exists only one form instance.
			identityRoles.forEach(identityRole -> {
				identityRole.setEavs(Lists.newArrayList(identityRoleService.getRoleAttributeValues(identityRole)));
			});
			List<IdmConceptRoleRequestDto> concepts = dto.getConceptRoles();
			concepts = this.service.markDuplicates(concepts, identityRoles);
			dto.setConceptRoles(concepts);
		}
		return dto;
	}

	@Override
	protected IdmRoleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmRoleRequestFilter filter = new IdmRoleRequestFilter(parameters);
		filter.setApplicant(getParameterConverter().toString(parameters, "applicant"));
		filter.setApplicantId(getParameterConverter().toUuid(parameters, "applicantId"));
		filter.setCreatedFrom(getParameterConverter().toDateTime(parameters, "createdFrom"));
		filter.setCreatedTill(getParameterConverter().toDateTime(parameters, "createdTill"));
		//
		if (filter.getApplicant() != null) {
			try {
				// Applicant can be UUID (Username vs UUID identification schizma)
				// TODO: replace with parameterConverter#toEntityUuid ...
				filter.setApplicantId(UUID.fromString(filter.getApplicant()));
				filter.setApplicant(null);
			} catch (IllegalArgumentException ex) {
				// Ok applicant is not UUID
			}
		}
		// TODO: remove redundant state field
		filter.setState(getParameterConverter().toEnum(parameters, "state", RoleRequestState.class));
		filter.setStates(getParameterConverter().toEnums(parameters, "states", RoleRequestState.class));
		filter.setApplicants(getParameterConverter().toUuids(parameters, "applicants"));
		filter.setCreatorId(getParameterConverter().toEntityUuid(parameters, "creator", IdmIdentityDto.class));
		return filter;
	}

}
