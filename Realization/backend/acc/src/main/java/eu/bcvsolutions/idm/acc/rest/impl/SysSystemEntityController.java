package eu.bcvsolutions.idm.acc.rest.impl;

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

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;;

/**
 * Entities on target system
 * 
 * TODO: remove ROLE_READ access? 
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-entities")
@Api(
		value = SysSystemEntityController.TAG, 
		tags = SysSystemEntityController.TAG, 
		description = "Raw entities on target system",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysSystemEntityController extends AbstractReadWriteDtoController<SysSystemEntityDto, SystemEntityFilter> {

	protected static final String TAG = "System entities";
	
	@Autowired
	public SysSystemEntityController(SysSystemEntityService systemEntityService) {
		super(systemEntityService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Search system entities (/search/quick alias)", 
			nickname = "searchSystemEntities",
			tags = { SysSystemEntityController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")})
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search system entities", 
			nickname = "searchQuickSystemEntities",
			tags = { SysSystemEntityController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")})
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "System entity detail", 
			nickname = "getSystemEntity", 
			response = SysSystemEntityDto.class, 
			tags = { SysSystemEntityController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")})
					})
	public ResponseEntity<?> get(
			@ApiParam(value = "System entity's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update system entity", 
			nickname = "postSystemEntity", 
			response = SysSystemEntityDto.class, 
			tags = { SysSystemEntityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull SysSystemEntityDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update system entity",
			nickname = "putSystemEntity", 
			response = SysSystemEntityDto.class, 
			tags = { SysSystemEntityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "System entity's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysSystemEntityDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete system entity", 
			nickname = "deleteSystemEntity", 
			tags = { SysSystemEntityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "System entity's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	protected SystemEntityFilter toFilter(MultiValueMap<String, Object> parameters) {
		SystemEntityFilter filter = new SystemEntityFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		filter.setEntityType(getParameterConverter().toEnum(parameters, "entityType", SystemEntityType.class));
		filter.setUid(getParameterConverter().toString(parameters, "uid"));
		return filter;
	}
}
