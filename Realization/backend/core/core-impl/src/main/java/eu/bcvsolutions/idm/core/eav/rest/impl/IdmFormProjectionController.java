package eu.bcvsolutions.idm.core.eav.rest.impl;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormProjectionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Form projections.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/form-projections")
@Api(
		value = IdmFormProjectionController.TAG, 
		description = "Operations with form projections", 
		tags = { IdmFormProjectionController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE)
public class IdmFormProjectionController extends AbstractReadWriteDtoController<IdmFormProjectionDto, IdmFormProjectionFilter>  {

	protected static final String TAG = "Form projections";
	
	@Autowired
	public IdmFormProjectionController(IdmFormProjectionService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_READ + "')")
	@ApiOperation(
			value = "Search form projections (/search/quick alias)", 
			nickname = "searchFormProjections",
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_READ + "')")
	@ApiOperation(
			value = "Search form projections", 
			nickname = "searchQuickFormProjections", 
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete form projections (selectbox usage)", 
			nickname = "autocompleteFormProjections", 
			tags = { IdmFormAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countFormProjections", 
			tags = { IdmFormAttributeController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_READ + "')")
	@ApiOperation(
			value = "Form definition detail", 
			nickname = "getFormDefiniton", 
			response = IdmFormProjectionDto.class, 
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Form projection's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_CREATE + "') or hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_UPDATE + "')")
	@ApiOperation(
			value = "Create / update form projection", 
			nickname = "postFormProjection", 
			response = IdmFormProjectionDto.class, 
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmFormProjectionDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_UPDATE + "')")
	@ApiOperation(
			value = "Update form projection",
			nickname = "putFormProjection", 
			response = IdmFormProjectionDto.class, 
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Form projection's uuid identifier", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmFormProjectionDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_UPDATE + "')")
	@ApiOperation(
			value = "Patch form projection", 
			nickname = "patchFormProjection", 
			response = IdmFormProjectionDto.class, 
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Form projection's uuid identifier", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_DELETE + "')")
	@ApiOperation(
			value = "Delete form projection", 
			nickname = "deleteFormProjection", 
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Form projection's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnFormProjection", 
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Form projection's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_READ + "')")
	@ApiOperation(
			value = "Process bulk action for form projections", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_PROJECTION_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action for form projections", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmFormProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.FORM_PROJECTION_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@Override
	protected IdmFormProjectionFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmFormProjectionFilter(parameters);
	}
}
