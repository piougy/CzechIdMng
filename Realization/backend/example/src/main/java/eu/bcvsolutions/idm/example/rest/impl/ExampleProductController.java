package eu.bcvsolutions.idm.example.rest.impl;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
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
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import eu.bcvsolutions.idm.example.domain.ExampleGroupPermission;
import eu.bcvsolutions.idm.example.dto.ExampleProductDto;
import eu.bcvsolutions.idm.example.dto.filter.ExampleProductFilter;
import eu.bcvsolutions.idm.example.service.api.ExampleProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * RESTful example product endpoint
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/example-products")
@Api(
		value = ExampleProductController.TAG, 
		description = "Example products", 
		tags = { ExampleProductController.TAG })
public class ExampleProductController extends DefaultReadWriteDtoController<ExampleProductDto, ExampleProductFilter> {

	protected static final String TAG = "Example products";
	
	@Autowired
	public ExampleProductController(ExampleProductService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_READ + "')")
	@ApiOperation(
			value = "Search example products (/search/quick alias)", 
			nickname = "searchExampleProducts", 
			tags = { ExampleProductController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_READ + "')")
	@ApiOperation(
			value = "Search example products", 
			nickname = "searchQuickExampleProducts", 
			tags = { ExampleProductController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete example products (selectbox usage)", 
			nickname = "autocompleteExampleProducts", 
			tags = { ExampleProductController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_READ + "')")
	@ApiOperation(
			value = "Example product detail", 
			nickname = "getExampleProduct", 
			response = ExampleProductDto.class, 
			tags = { ExampleProductController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_CREATE + "') or hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE + "')")
	@ApiOperation(
			value = "Create / update example product", 
			nickname = "postExampleProduct", 
			response = ExampleProductDto.class, 
			tags = { ExampleProductController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_CREATE, description = ""),
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_CREATE, description = ""),
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody ExampleProductDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE + "')")
	@ApiOperation(
			value = "Update example product", 
			nickname = "putExampleProduct", 
			response = ExampleProductDto.class, 
			tags = { ExampleProductController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody ExampleProductDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_DELETE + "')")
	@ApiOperation(
			value = "Delete example product", 
			nickname = "deleteExampleProduct", 
			tags = { ExampleProductController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_READ + "')"
			+ " or hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnExampleProduct", 
			tags = { ExampleProductController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_READ, description = ""),
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_READ, description = ""),
						@AuthorizationScope(scope = ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE, description = "")})
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	protected ExampleProductFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new ExampleProductFilter(parameters);
	}
}
