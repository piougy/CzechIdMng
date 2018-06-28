package eu.bcvsolutions.idm.core.rest.impl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Role catalogue controller
 * 
 * TODO: use AbstractEventableDtoController - add missing methods, because DefaultReadWriteDtoController is used now :/ => read role catalogues is public now
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-catalogues")
@Api(
		value = IdmRoleCatalogueController.TAG, 
		tags = { IdmRoleCatalogueController.TAG }, 
		description = "Operations with role catalogues", 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmRoleCatalogueController extends DefaultReadWriteDtoController<IdmRoleCatalogueDto, IdmRoleCatalogueFilter> {
	
	protected static final String TAG = "Role catalogues";
	private final IdmRoleCatalogueService service;
	
	@Autowired
	public IdmRoleCatalogueController(IdmRoleCatalogueService service) {
		super(service);
		//
		this.service = service;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update role catalogue", 
			nickname = "postRoleCatalogue", 
			response = IdmRoleCatalogueDto.class, 
			tags = { IdmRoleCatalogueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLECATALOGUE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLECATALOGUE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLECATALOGUE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLECATALOGUE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmRoleCatalogueDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	@ApiOperation(
			value = "Update role catalogue", 
			nickname = "putRoleCatalogue", 
			response = IdmRoleCatalogueDto.class, 
			tags = { IdmRoleCatalogueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLECATALOGUE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLECATALOGUE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmRoleCatalogueDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	@ApiOperation(
			value = "Update role catalogue", 
			nickname = "patchRoleCatalogue	", 
			response = IdmRoleCatalogueDto.class, 
			tags = { IdmRoleCatalogueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLECATALOGUE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLECATALOGUE_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_DELETE + "')")
	@ApiOperation(
			value = "Delete role catalogue", 
			nickname = "deleteRoleCatalogue", 
			tags = { IdmRoleCatalogueController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLECATALOGUE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLECATALOGUE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/roots", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search root catalogues", 
			nickname = "searchRootRoleCatalogues", 
			tags = { IdmRoleCatalogueController.TAG })
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "string", paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "string", paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public Resources<?> findRoots(@PageableDefault Pageable pageable) {
		Page<IdmRoleCatalogueDto> roots = service.findRoots(pageable);
		return toResources(roots, IdmRoleCatalogue.class);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/children", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search sub catalogues", 
			nickname = "searchChildrenRoleCatalogues", 
			tags = { IdmRoleCatalogueController.TAG })
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "string", paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "string", paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public Resources<?> findChildren(
			@ApiParam(value = "Superior role catalogue's uuid identifier.", required = true)
			@RequestParam(name = "parent", required = true) @NotNull String parentId,
			@PageableDefault Pageable pageable) {
		Page<IdmRoleCatalogueDto> children = service.findChildrenByParent(UUID.fromString(parentId), pageable);
		return toResources(children, IdmRoleCatalogue.class);
	}	
	
	@Override
	protected IdmRoleCatalogueFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmRoleCatalogueFilter filter = new IdmRoleCatalogueFilter(parameters);
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setName(getParameterConverter().toString(parameters, "name"));
		filter.setCode(getParameterConverter().toString(parameters, "code"));
		filter.setParent(getParameterConverter().toEntityUuid(parameters, "parent", IdmRoleCatalogue.class));
		return filter;
	}
}
