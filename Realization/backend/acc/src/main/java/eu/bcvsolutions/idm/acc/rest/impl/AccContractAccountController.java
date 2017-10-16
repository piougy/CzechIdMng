package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Set;

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
import eu.bcvsolutions.idm.acc.dto.AccContractAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccContractAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccContractAccountService;
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
 * Contract accounts on target system
 * 
 * @author Svanda
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/contract-accounts")
@Api(
		value = AccContractAccountController.TAG,  
		tags = { AccContractAccountController.TAG }, 
		description = "Assigned accounts on target system",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class AccContractAccountController extends AbstractReadWriteDtoController<AccContractAccountDto, AccContractAccountFilter> {
	
	protected static final String TAG = "Contract accounts";
	
	@Autowired
	public AccContractAccountController(AccContractAccountService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.CONTRACT_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Search contract accounts (/search/quick alias)", 
			nickname = "searchContractAccounts", 
			tags = { AccContractAccountController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.CONTRACT_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Search contract accounts", 
			nickname = "searchQuickContractAccounts", 
			tags = { AccContractAccountController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.CONTRACT_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Contract account detail", 
			nickname = "getContractAccount", 
			response = AccContractAccountDto.class, 
			tags = { AccContractAccountController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_READ, description = "") })
					})
	public ResponseEntity<?> get(
			@ApiParam(value = "Contract account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.CONTRACT_ACCOUNT_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.CONTRACT_ACCOUNT_UPDATE + "')")
	@ApiOperation(
			value = "Create / update contract account", 
			nickname = "postContract", 
			response = AccContractAccountDto.class, 
			tags = { AccContractAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_UPDATE, description = "")})
				})
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> post(@RequestBody @NotNull AccContractAccountDto dto){
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.CONTRACT_ACCOUNT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update contract account", 
			nickname = "putContractAccount", 
			response = AccContractAccountDto.class, 
			tags = { AccContractAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Contract account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccContractAccountDto dto){
		return super.put(backendId,dto);
	}	
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.CONTRACT_ACCOUNT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete contract account", 
			nickname = "deleteContractAccount", 
			tags = { AccContractAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Contract account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.CONTRACT_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "What logged contract can do with given record", 
			nickname = "getPermissionsOnContractAccount", 
			tags = { AccContractAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.CONTRACT_ACCOUNT_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Contract account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	protected AccContractAccountFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccContractAccountFilter filter = new AccContractAccountFilter();
		filter.setAccountId(getParameterConverter().toUuid(parameters, "accountId"));
		filter.setContractId(getParameterConverter().toUuid(parameters, "contract"));
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		filter.setOwnership(getParameterConverter().toBoolean(parameters, "ownership"));
		return filter;
	}
}
