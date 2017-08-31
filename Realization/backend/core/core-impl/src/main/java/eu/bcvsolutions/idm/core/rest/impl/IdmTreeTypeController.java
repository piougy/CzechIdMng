package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeTypeFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.rest.impl.IdmLongRunningTaskController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Tree type structures
 * 
 * TODO: secure read operations? 
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseDtoController.BASE_PATH + BaseDtoController.TREE_BASE_PATH + "-types")
@Api(
		value = IdmTreeTypeController.TAG,  
		tags = { IdmTreeTypeController.TAG }, 
		description = "Operation with tree types",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmTreeTypeController extends AbstractReadWriteDtoController<IdmTreeTypeDto, IdmTreeTypeFilter> {
	
	protected static final String TAG = "Tree structure - types";
	private final IdmLongRunningTaskController longRunningTaskController;
	private final IdmTreeTypeService service;
	private final IdmTreeNodeService treeNodeservice;
	
	@Autowired
	public IdmTreeTypeController(
			IdmTreeTypeService service, 
			IdmTreeNodeService treeNodeservice,
			IdmLongRunningTaskController longRunningTaskController) {
		super(service);
		//
		Assert.notNull(longRunningTaskController);
		Assert.notNull(treeNodeservice);
		//
		this.service = service;
		this.longRunningTaskController = longRunningTaskController;
		this.treeNodeservice = treeNodeservice;
	}
	
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Search tree types (/search/quick alias)", 
			nickname = "searchTreeTypes",
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Search tree types", 
			nickname = "searchQuickTreeTypes", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete tree types (selectbox usage)", 
			nickname = "autocompleteTreeTypes", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Tree type detail", 
			nickname = "getTreeType", 
			response = IdmTreeTypeDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.TREETYPE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update tree type", 
			nickname = "postTreeType", 
			response = IdmTreeTypeDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmTreeTypeDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_UPDATE + "')")
	@ApiOperation(
			value = "Update tree type",
			nickname = "putTreeType", 
			response = IdmTreeTypeDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmTreeTypeDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_UPDATE + "')")
	@ApiOperation(
			value = "Update tree type",
			nickname = "patchTreeType", 
			response = IdmTreeTypeDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_DELETE + "')")
	@ApiOperation(
			value = "Delete tree type", 
			nickname = "deleteTreeType", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnTreeType", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	/**
	 * Returns default tree type or {@code null}, if no default tree type is defined
	 * 
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value= "/search/default", method = RequestMethod.GET)
	@ApiOperation(
			value = "Get default tree type detail", 
			nickname = "getDefaultTreeType", 
			response = IdmTreeTypeDto.class, 
			tags = { IdmTreeTypeController.TAG })
	public ResponseEntity<?> getDefaultTreeType() {
		IdmTreeTypeDto defaultTreeType = service.getDefaultTreeType();
		if (defaultTreeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", "default tree type"));
		}
		return new ResponseEntity<>(toResource(defaultTreeType), HttpStatus.OK);
	}
	
	/**
	 * Returns all configuration properties for given tree type.
	 * 
	 * @param backendId
	 * @return list of granted authorities
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/configurations", method = RequestMethod.GET)
	@ApiOperation(
			value = "Get tree type configuration items", 
			nickname = "getTreeTypeConfigurations", 
			tags = { IdmTreeTypeController.TAG })
	public List<IdmConfigurationDto> getConfigurations(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable String backendId) {
		IdmTreeTypeDto treeType = getDto(backendId);
		if (treeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		return service.getConfigurations(treeType.getId());
	}
	
	/**
	 * Rebuild (drop and create) all indexes for given treeType.
	 * 
	 * @param backendId tree type id
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/index/rebuild", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@ApiOperation(
			value = "Rebuild tree type index", 
			nickname = "rebuildTreeTypeIndex", 
			response = IdmLongRunningTaskDto.class, 
			tags = { IdmTreeTypeController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") })
					},
			notes = "Rebuild forest index for given tree type.")
	public ResponseEntity<?> rebuildIndex(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable String backendId,
			PersistentEntityResourceAssembler assembler) {
		IdmTreeTypeDto treeType = getDto(backendId);
		if (treeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		UUID longRunningTaskId = treeNodeservice.rebuildIndexes(treeType.getId());
		//
		return longRunningTaskController.get(longRunningTaskId.toString());
	}
}
