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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Contract guarantee slice controller
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/contract-slice-guarantees")
@Api(
		value = IdmContractSliceGuaranteeController.TAG, 
		description = "Operations with identity contract slice guarantees", 
		tags = { IdmContractSliceGuaranteeController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmContractSliceGuaranteeController extends AbstractEventableDtoController<IdmContractSliceGuaranteeDto, IdmContractSliceGuaranteeFilter> {
	
	protected static final String TAG = "Contract slice guarantees";
	
	@Autowired
	public IdmContractSliceGuaranteeController(IdmContractSliceGuaranteeService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ + "')")
	@ApiOperation(
			value = "Search contract slice guarantees (/search/quick alias)", 
			nickname = "searchContractSliceGuarantees", 
			tags = { IdmContractSliceGuaranteeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ + "')")
	@ApiOperation(
			value = "Search contract guarantees", 
			nickname = "searchQuickContractSliceGuarantees", 
			tags = { IdmContractSliceGuaranteeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICEGUARANTEE_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete contract guarantees (selectbox usage)", 
			nickname = "autocompleteContractSliceGuarantees", 
			tags = { IdmContractSliceGuaranteeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ + "')")
	@ApiOperation(
			value = "Contract guarantee detail", 
			nickname = "getContractSliceGuarantee", 
			response = IdmContractSliceGuaranteeDto.class, 
			tags = { IdmContractSliceGuaranteeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Contract slice guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICEGUARANTEE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.CONTRACTSLICEGUARANTEE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update contract guarantee", 
			nickname = "postContractSliceGuarantee", 
			response = IdmContractSliceGuaranteeDto.class, 
			tags = { IdmContractSliceGuaranteeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmContractSliceGuaranteeDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICEGUARANTEE_UPDATE + "')")
	@ApiOperation(
			value = "Update contract guarantee", 
			nickname = "putContractSliceGuarantee", 
			response = IdmContractSliceGuaranteeDto.class, 
			tags = { IdmContractSliceGuaranteeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Contract slice guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmContractSliceGuaranteeDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICEGUARANTEE_DELETE + "')")
	@ApiOperation(
			value = "Delete contract guarantee", 
			nickname = "deleteContractSliceGuarantee", 
			tags = { IdmContractSliceGuaranteeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Contract slice guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnContractSliceGuarantee", 
			tags = { IdmContractSliceGuaranteeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICEGUARANTEE_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Contract slice guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
