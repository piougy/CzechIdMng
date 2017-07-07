package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
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
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + BaseEntityController.TREE_BASE_PATH + "-types")
@Api(
		value = IdmTreeTypeController.TAG,  
		tags = { IdmTreeTypeController.TAG }, 
		description = "Operation with tree types",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmTreeTypeController extends DefaultReadWriteEntityController<IdmTreeType, QuickFilter> {
	
	protected static final String TAG = "Tree structure - types";
	private final IdmLongRunningTaskController longRunningTaskController;
	
	@Autowired
	public IdmTreeTypeController(LookupService entityLookupService, IdmLongRunningTaskController longRunningTaskController) {
		super(entityLookupService);
		//
		Assert.notNull(longRunningTaskController);
		//
		this.longRunningTaskController = longRunningTaskController;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.TREETYPE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update tree type", 
			nickname = "postTreeType", 
			response = IdmTreeType.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_UPDATE + "')")
	@ApiOperation(
			value = "Update tree type",
			nickname = "putTreeType", 
			response = IdmTreeType.class, 
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
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_UPDATE + "')")
	@ApiOperation(
			value = "Update tree type",
			nickname = "patchTreeType", 
			response = IdmTreeType.class, 
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
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
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
			response = IdmTreeType.class, 
			tags = { IdmTreeTypeController.TAG })
	public ResponseEntity<?> getDefaultTreeType(PersistentEntityResourceAssembler assembler) {
		IdmTreeType defaultTreeType = entityLookupService.getEntityService(IdmTreeType.class, IdmTreeTypeService.class).getDefaultTreeType();
		if (defaultTreeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", "default tree type"));
		}		
		return new ResponseEntity<>(toResource(defaultTreeType, assembler), HttpStatus.OK);
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
		IdmTreeType treeType = getEntity(backendId);
		if (treeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		return entityLookupService.getEntityService(IdmTreeType.class, IdmTreeTypeService.class).getConfigurations(treeType);
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
		IdmTreeType treeType = getEntity(backendId);
		if (treeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		UUID longRunningTaskId = entityLookupService.getEntityService(IdmTreeNode.class, IdmTreeNodeService.class).rebuildIndexes(treeType);
		//
		return longRunningTaskController.get(longRunningTaskId.toString());
	}
}
