package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.GeneratorDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmGeneratedValueFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmGeneratedValueService;
import eu.bcvsolutions.idm.core.api.service.ValueGeneratorManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Generated values controller
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/generated-values")
@Api(
		value = IdmGeneratedValueController.TAG, 
		description = "Operations with generated values", 
		tags = { IdmGeneratedValueController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmGeneratedValueController extends AbstractReadWriteDtoController<IdmGeneratedValueDto, IdmGeneratedValueFilter> {

	protected static final String TAG = "Generated values";

	private final ValueGeneratorManager valueGeneratorManager;

	@Autowired
	public IdmGeneratedValueController(IdmGeneratedValueService entityService, ValueGeneratorManager valueGeneratorManager) {
		super(entityService);
		//
		Assert.notNull(valueGeneratorManager);
		//
		this.valueGeneratorManager = valueGeneratorManager;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_READ + "')")
	@ApiOperation(
			value = "Search generated values (/search/quick alias)", 
			nickname = "searchGeneratedValues", 
			tags = { IdmGeneratedValueController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_READ + "')")
	@ApiOperation(
			value = "Search generated values", 
			nickname = "searchQuickGeneratedValues", 
			tags = { IdmGeneratedValueController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countGeneratedValues", 
			tags = { IdmGeneratedValueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_READ + "')")
	@ApiOperation(
			value = "Generated value detail", 
			nickname = "getGeneratedValues", 
			response = IdmGeneratedValueDto.class, 
			tags = { IdmGeneratedValueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Generated value uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update generated value", 
			nickname = "postGeneratedValue", 
			response = IdmGeneratedValueDto.class, 
			tags = { IdmGeneratedValueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmGeneratedValueDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_UPDATE + "')")
	@ApiOperation(
			value = "Update generated value", 
			nickname = "putGeneratedValues", 
			response = IdmGeneratedValueDto.class, 
			tags = { IdmGeneratedValueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Generated value uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmGeneratedValueDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_DELETE + "')")
	@ApiOperation(
			value = "Delete generated value", 
			nickname = "deleteGeneratedValue", 
			tags = { IdmGeneratedValueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Generate value uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnGeneratedValue", 
			tags = { IdmGeneratedValueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Generated value uuid.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	/**
	 * Returns all registered entities for generate values
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_READ + "')")
	@ApiOperation(
			value = "Get all supported entities", 
			nickname = "getSupportedEntities", 
			tags = { IdmGeneratedValueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") })
				})
	public ResponseEntity<?> getSupportedEntities() {
		return new ResponseEntity<>(toResources(valueGeneratorManager.getSupportedEntityTypes(), null), HttpStatus.OK);
	}
	
	/**
	 * Returns all supported generators for type
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/generators{entityType}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATED_VALUE_READ + "')")
	@ApiOperation(
			value = "Get all supported generator", 
			nickname = "getGenerators", 
			tags = { IdmAuthorizationPolicyController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.GENERATED_VALUE_READ, description = "") })
				},
			notes = "Returns all available generators.")
	public Resources<GeneratorDefinitionDto> getAvailableGenerators(@PathVariable String entityType) {
		// in older version path variable doesnt support null
		if (StringUtils.isEmpty(entityType)) {
			entityType = null;
		}
		return new Resources<>(valueGeneratorManager.getAvailableGenerators(entityType));
	}
}
