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
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.api.service.StatelessAsynchronousTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Automatic role controller
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-tree-nodes")
@Api(
		value = IdmRoleTreeNodeController.TAG,  
		tags = { IdmRoleTreeNodeController.TAG }, 
		description = "Automatic roles",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmRoleTreeNodeController extends AbstractReadWriteDtoController<IdmRoleTreeNodeDto, IdmRoleTreeNodeFilter> {
	
	protected static final String TAG = "Roles - by tree structures";
	private final LongRunningTaskManager taskManager;
	
	@Autowired
	public IdmRoleTreeNodeController(IdmRoleTreeNodeService service, LongRunningTaskManager taskManager) {
		super(service);
		//
		Assert.notNull(taskManager);
		//
		this.taskManager = taskManager;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_READ + "')")
	@ApiOperation(
			value = "Search automatic roles (/search/quick alias)", 
			nickname = "searchRoleTreeNodes", 
			tags = { IdmRoleTreeNodeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_READ + "')")
	@ApiOperation(
			value = "Search automatic roles", 
			nickname = "searchQuickRoleTreeNodes", 
			tags = { IdmRoleTreeNodeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_READ + "')")
	@ApiOperation(
			value = "Automatic role detail", 
			nickname = "getRoleTreeNode", 
			response = IdmRoleTreeNodeDto.class, 
			tags = { IdmRoleTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Automatic role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLETREENODE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update automatic role", 
			nickname = "postRoleTreeNode", 
			response = IdmRoleTreeNodeDto.class, 
			tags = { IdmRoleTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_UPDATE, description = "")})
				},
			notes = "If role has guarantee assigned, then automatic role has to be approved by him at first (configurable by entity event processor).")
	public ResponseEntity<?> post(@Valid @RequestBody IdmRoleTreeNodeDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_DELETE + "')")
	@ApiOperation(
			value = "Delete automatic role", 
			nickname = "deleteRoleTreeNode", 
			tags = { IdmRoleTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Automatic role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		LongRunningTaskExecutor<?> asyncTask = new StatelessAsynchronousTask() {
			
			@Override
			public String getName() {
				return "RemoveAutomaticRoleTask";
			}
			
			@Override
			public String getDescription() {
				return String.format("Remove automatic role [%s] asynchronously", backendId);
			}
			
			@Override
			public Boolean process() {
				IdmRoleTreeNodeController.super.delete(backendId);
				return Boolean.TRUE;			
			}
		};
		taskManager.execute(asyncTask);
		// TODO: improve status handling on FE
		// return new ResponseEntity<Object>(HttpStatus.ACCEPTED);
		throw new AcceptedException();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnRoleTreeNode", 
			tags = { IdmRoleTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLETREENODE_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Automatic role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	public IdmRoleTreeNodeDto postDto(IdmRoleTreeNodeDto entity) {
		if (!getService().isNew(entity)) {
			throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED, "Automatic role update is not supported");
		}
		return super.postDto(entity);
	}
}
