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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.filter.ProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Active provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/provisioning-operations")
@Api(
		value = SysProvisioningOperationController.TAG, 
		tags = SysProvisioningOperationController.TAG, 
		description = "Active provisioning operations in queue",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysProvisioningOperationController
		extends AbstractReadDtoController<SysProvisioningOperationDto, ProvisioningOperationFilter> {

	protected static final String TAG = "Provisioning - queue";
	//
	private final ProvisioningExecutor provisioningExecutor;

	@Autowired
	public SysProvisioningOperationController(SysProvisioningOperationService service,
			ProvisioningExecutor provisioningExecutor) {
		super(service);
		//
		Assert.notNull(provisioningExecutor);
		//
		this.provisioningExecutor = provisioningExecutor;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@ApiOperation(
			value = "Search provisioning operations (/search/quick alias)", 
			nickname = "searchProvisioningOperations",
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search provisioning operations", 
			nickname = "searchQuickProvisioningOperations",
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Provisioning operation detail", 
			nickname = "getProvisioningOperation", 
			response = SysProvisioningOperation.class, 
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Provisioning operation's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/{backendId}/retry", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Retry provisioning operation", 
			nickname = "retryProvisioningOperation", 
			response = SysProvisioningOperation.class, 
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public ResponseEntity<?> retry(
			@ApiParam(value = "Provisioning operation's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		SysProvisioningOperationDto provisioningOperation = getDto(backendId);
		if (provisioningOperation == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		provisioningOperation = provisioningExecutor.execute(provisioningOperation);
		return new ResponseEntity<>(toResource(provisioningOperation), HttpStatus.OK);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/{backendId}/cancel", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Cancel provisioning operation", 
			nickname = "cancelProvisioningOperation", 
			response = SysProvisioningOperation.class, 
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public ResponseEntity<?> cancel(
			@ApiParam(value = "Provisioning operation's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		SysProvisioningOperationDto provisioningOperation = getDto(backendId);
		if (provisioningOperation == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		provisioningOperation = provisioningExecutor.cancel(provisioningOperation);
		return new ResponseEntity<>(toResource(provisioningOperation), HttpStatus.OK);
	}
}
