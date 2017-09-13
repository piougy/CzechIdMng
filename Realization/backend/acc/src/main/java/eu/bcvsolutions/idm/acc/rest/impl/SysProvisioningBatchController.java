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
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
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
 * Active provisioning operations - batch by system entity.
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/provisioning-batches")
@Api(
		value = SysProvisioningBatchController.TAG, 
		tags = SysProvisioningBatchController.TAG, 
		description = "Active provisioning operations in queue - grouped by system entity.",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysProvisioningBatchController
		extends AbstractReadDtoController<SysProvisioningBatchDto, EmptyFilter> {

	protected static final String TAG = "Provisioning - batch";
	//
	private final ProvisioningExecutor provisioningExecutor;

	@Autowired
	public SysProvisioningBatchController(SysProvisioningBatchService service,
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
			value = "Search provisioning batches (/search/quick alias)", 
			nickname = "searchProvisioningBatches",
			tags = { SysProvisioningBatchController.TAG }, 
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
			value = "Search provisioning batches ", 
			nickname = "searchQuickProvisioningBatches",
			tags = { SysProvisioningBatchController.TAG }, 
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
			value = "Provisioning batch detail", 
			nickname = "getProvisioningBatch", 
			response = SysProvisioningBatch.class, 
			tags = { SysProvisioningBatchController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Provisioning batch's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/{backendId}/retry", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Retry provisioning batch", 
			nickname = "retryProvisioningBatch",
			tags = { SysProvisioningBatchController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				},
			notes = "Retry all provisioning operations in given batch - retry all active operations in queue grouped by system entity ordered by incomming date.")
	public ResponseEntity<?> retry(
			@ApiParam(value = "Provisioning batch's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		SysProvisioningBatchDto batch = getDto(backendId);
		if (batch == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		provisioningExecutor.execute(batch);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/{backendId}/cancel", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Cance provisioning batch", 
			nickname = "cancelProvisioningBatch",
			tags = { SysProvisioningBatchController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				},
			notes = "Cancel all provisioning operations in given batch - cancel all active operations in queue grouped by system entity.")
	public ResponseEntity<Void> cancel(
			@ApiParam(value = "Provisioning batch's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		SysProvisioningBatchDto batch = getDto(backendId);
		if (batch == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		provisioningExecutor.cancel(batch);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
}
