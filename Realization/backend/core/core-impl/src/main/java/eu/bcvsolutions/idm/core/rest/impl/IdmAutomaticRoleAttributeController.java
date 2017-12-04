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
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.StatelessAsynchronousTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Automatic role controller by attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/automatic-role-attributes")
@Api(
		value = IdmAutomaticRoleAttributeController.TAG,  
		tags = { IdmAutomaticRoleAttributeController.TAG }, 
		description = "Automatic roles by attribute",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmAutomaticRoleAttributeController extends AbstractReadWriteDtoController<IdmAutomaticRoleAttributeDto, IdmAutomaticRoleFilter> {

	protected static final String TAG = "Automatic roles by attribute";
	private final LongRunningTaskManager taskManager;
	
	@Autowired
	public IdmAutomaticRoleAttributeController(IdmAutomaticRoleAttributeService entityService,
			LongRunningTaskManager taskManager) {
		super(entityService);
		//
		Assert.notNull(taskManager);
		//
		this.taskManager = taskManager;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ + "')")
	@ApiOperation(
			value = "Search automatic roles by attribute (/search/quick alias)", 
			nickname = "searchAutomaticRoleAttributes", 
			tags = { IdmAutomaticRoleAttributeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ + "')")
	@ApiOperation(
			value = "Search automatic roles", 
			nickname = "searchQuickAutomaticRoleAttibutes", 
			tags = { IdmAutomaticRoleAttributeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ + "')")
	@ApiOperation(
			value = "Automatic role detail", 
			nickname = "getAutomaticRoleAttributeRule", 
			response = IdmAutomaticRoleAttributeDto.class, 
			tags = { IdmAutomaticRoleAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Automatic role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update automatic role attribute", 
			nickname = "postAutomaticRoleAttribute", 
			response = IdmAutomaticRoleAttributeDto.class, 
			tags = { IdmAutomaticRoleAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_UPDATE, description = "")})
				},
			notes = "If role has guarantee assigned, then automatic role has to be approved by him at first (configurable by entity event processor).")
	public ResponseEntity<?> post(@Valid @RequestBody IdmAutomaticRoleAttributeDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_DELETE + "')")
	@ApiOperation(
			value = "Delete automatic role by attribute", 
			nickname = "deleteAutomaticRoleAttribute", 
			tags = { IdmAutomaticRoleAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_DELETE, description = "") })
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
				IdmAutomaticRoleAttributeController.super.delete(backendId);
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnAutomaticRole", 
			tags = { IdmAutomaticRoleAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Automatic role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	public IdmAutomaticRoleAttributeDto postDto(IdmAutomaticRoleAttributeDto entity) {
		if (!getService().isNew(entity)) {
			throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED, "Automatic role by attribute update is not supported");
		}
		return super.postDto(entity);
	}
}