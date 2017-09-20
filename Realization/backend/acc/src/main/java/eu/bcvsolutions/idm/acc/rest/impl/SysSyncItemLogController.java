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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;;

/**
 * System synchronization item log rest
 * 
 * @author svandav
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-synchronization-item-logs")
@Api(
		value = SysSyncItemLogController.TAG, 
		tags = SysSyncItemLogController.TAG, 
		description = "Single entity log",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysSyncItemLogController
		extends AbstractReadWriteDtoController<SysSyncItemLogDto, SysSyncItemLogFilter> {

	protected static final String TAG = "Synchronization - log items";
	
	@Autowired
	public SysSyncItemLogController(SysSyncItemLogService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@ApiOperation(
			value = "Search synchronization item logs (/search/quick alias)", 
			nickname = "searchSyncItemLogs",
			tags = { SysSyncItemLogController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search synchronization item logs", 
			nickname = "searchQuickSyncItemLogs",
			tags = { SysSyncItemLogController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Synchronization item log detail", 
			nickname = "getSyncItemLog", 
			response = SysSyncItemLog.class, 
			tags = { SysSyncItemLogController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Item log's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYNCHRONIZATION_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete synchronization item log", 
			nickname = "deleteSyncItemLog", 
			tags = { SysSyncItemLogController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYNCHRONIZATION_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYNCHRONIZATION_UPDATE, description = "")})
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Item log's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	protected SysSyncItemLogFilter toFilter(MultiValueMap<String, Object> parameters) {
		SysSyncItemLogFilter filter = new SysSyncItemLogFilter();
		filter.setSyncActionLogId(getParameterConverter().toUuid(parameters, "syncActionLogId"));
		filter.setDisplayName(getParameterConverter().toString(parameters, "displayName"));
		return filter;
	}
}
