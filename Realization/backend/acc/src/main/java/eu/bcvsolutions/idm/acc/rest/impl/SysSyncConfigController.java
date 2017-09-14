package eu.bcvsolutions.idm.acc.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
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
 * System synchronization configurations
 * 
 * @author svandav
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-synchronization-configs")
@Api(
		value = SysSyncConfigController.TAG, 
		tags = SysSyncConfigController.TAG, 
		description = "Synchronization setting",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysSyncConfigController
		extends AbstractReadWriteDtoController<SysSyncConfigDto, SynchronizationConfigFilter> {

	protected static final String TAG = "Synchronization - configurations";
	//
	private final SynchronizationService synchronizationService;
	
	@Autowired
	public SysSyncConfigController(SysSyncConfigService service, SynchronizationService synchronizationService) {
		super(service);
		Assert.notNull(synchronizationService);
		
		this.synchronizationService = synchronizationService;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@ApiOperation(
			value = "Search synchronization configs (/search/quick alias)", 
			nickname = "searchSyncConfigs",
			tags = { SysSyncConfigController.TAG }, 
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
			value = "Search synchronization configs", 
			nickname = "searchQuickSyncConfigs",
			tags = { SysSyncConfigController.TAG }, 
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
			value = "Synchronization config detail", 
			nickname = "getSyncConfig", 
			response = SysSyncConfig.class, 
			tags = { SysSyncConfigController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Config's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update synchronization config", 
			nickname = "postSyncConfig", 
			response = SysSyncConfig.class, 
			tags = { SysSyncConfigController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull SysSyncConfigDto dto)
			throws HttpMessageNotReadableException {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update synchronization config", 
			nickname = "putSyncConfig", 
			response = SysSyncConfig.class, 
			tags = { SysSyncConfigController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")})
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Config's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysSyncConfigDto dto) throws HttpMessageNotReadableException {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete synchronization config", 
			nickname = "deleteSyncConfig", 
			tags = { SysSyncConfigController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_DELETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_DELETE, description = "")})
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Config's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	/**
	 * Start synchronization
	 * 
	 * @param backendId
	 * @returnon
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYNCHRONIZATION_CREATE + "')")
	@RequestMapping(value = "/{backendId}/start", method = RequestMethod.POST)
	@ApiOperation(
			value = "Start synchronization", 
			nickname = "startSynchronization", 
			response = SysSyncConfig.class, 
			tags = { SysSyncConfigController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYNCHRONIZATION_CREATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYNCHRONIZATION_CREATE, description = "")})
				},
			notes = "Start synchronization by given config.")
	public ResponseEntity<?> startSynchronization(
			@ApiParam(value = "Config's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId)  {
		return new ResponseEntity<>(toResource(this.synchronizationService.startSynchronizationEvent(this.getService().get(backendId))), HttpStatus.OK);
	}
	
	/**
	 * Cancel synchronization
	 * 
	 * @param backendId
	 * @returnion
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYNCHRONIZATION_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/cancel", method = RequestMethod.POST)
	@ApiOperation(
			value = "Cancel synchronization", 
			nickname = "cancelSynchronization", 
			response = SysSyncConfig.class, 
			tags = { SysSyncConfigController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYNCHRONIZATION_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYNCHRONIZATION_UPDATE, description = "")})
				},
			notes = "Cancel synchronization by given config.")
	public ResponseEntity<?> cancelSynchronization(
			@ApiParam(value = "Config's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return new ResponseEntity<>(toResource(this.synchronizationService.stopSynchronizationEvent(this.getService().get(backendId))), HttpStatus.OK);
	}
	
	/**
	 * Is synchronization running
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/is-running", method = RequestMethod.POST)
	@ApiOperation(
			value = "Synchronization is running", 
			nickname = "isRunningSynchronization",
			tags = { SysSyncConfigController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
				},
			notes = "If sync by given config's identifier is running.")
	public ResponseEntity<?> isRunningSynchronization(
			@ApiParam(value = "Config's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {		
		boolean running = ((SysSyncConfigService)this.getService()).isRunning(this.getService().get(backendId));
		return new ResponseEntity<>(running, HttpStatus.OK);
	}

	@Override
	protected SynchronizationConfigFilter toFilter(MultiValueMap<String, Object> parameters) {
		SynchronizationConfigFilter filter = new SynchronizationConfigFilter();	
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		return filter;
	}
}
