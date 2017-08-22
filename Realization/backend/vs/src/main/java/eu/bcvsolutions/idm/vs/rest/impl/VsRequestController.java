package eu.bcvsolutions.idm.vs.rest.impl;

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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.repository.VsRequestRepository;
import eu.bcvsolutions.idm.vs.repository.filter.RequestFilter;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Rest methods for virtual system request
 * 
 * @author Svanda
 *
 */
@RequestMapping(value = BaseDtoController.BASE_PATH + "/vs/requests")
@Api(value = VsRequestController.TAG, tags = {
		VsRequestController.TAG }, description = "Operations with requests (in virtual system)", produces = BaseController.APPLICATION_HAL_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class VsRequestController extends AbstractReadWriteDtoController<VsRequestDto, RequestFilter> {

	protected static final String TAG = "Requests";
	//
	private final VsRequestRepository repository;

	@Autowired
	public VsRequestController(VsRequestService service, VsRequestRepository repository) {
		super(service);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@ApiOperation(value = "Search requests (/search/quick alias)", nickname = "searchRequests", tags = {
			VsRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_READ, description = "") }) })
	public Resources<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@ApiOperation(value = "Search requests", nickname = "searchQuickRequests", tags = {
			VsRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_READ, description = "") }) })
	public Resources<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE + "')")
	@ApiOperation(value = "Autocomplete requests (selectbox usage)", nickname = "autocompleteRequests", tags = {
			VsRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE, description = "") }) })
	public Resources<?> autocomplete(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@ApiOperation(value = "Request detail", nickname = "getRequest", response = VsRequestDto.class, tags = {
			VsRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_READ, description = "") }) })
	public ResponseEntity<?> get(
			@ApiParam(value = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_CREATE + "') or hasAuthority('"
			+ VirtualSystemGroupPermission.VS_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Create / update request", nickname = "postRequest", response = VsRequestDto.class, tags = {
			VsRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> post(@Valid @RequestBody VsRequestDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Update request", nickname = "putRequest", response = VsRequestDto.class, tags = {
			VsRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> put(
			@ApiParam(value = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			@Valid @RequestBody VsRequestDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_DELETE + "')")
	@ApiOperation(value = "Delete request", nickname = "deleteRequest", tags = {
			VsRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_DELETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_DELETE, description = "") }) })
	public ResponseEntity<?> delete(
			@ApiParam(value = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')" + " or hasAuthority('"
			+ VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE + "')")
	@ApiOperation(value = "What logged request can do with given record", nickname = "getPermissionsOnRequest", tags = {
			VsRequestController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE, description = "") }) })

	public Set<String> getPermissions(
			@ApiParam(value = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	protected RequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		RequestFilter filter = new RequestFilter();

		return filter;
	}
}
