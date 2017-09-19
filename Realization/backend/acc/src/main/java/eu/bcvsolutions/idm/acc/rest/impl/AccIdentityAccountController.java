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
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;;

/**
 * Identity accounts on target system
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/identity-accounts")
@Api(
		value = AccIdentityAccountController.TAG,  
		tags = { AccIdentityAccountController.TAG }, 
		description = "Assigned accounts on target system",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class AccIdentityAccountController extends AbstractReadWriteDtoController<AccIdentityAccountDto, AccIdentityAccountFilter> {
	
	protected static final String TAG = "Identity accounts";
	
	@Autowired
	public AccIdentityAccountController(AccIdentityAccountService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Search identity accounts (/search/quick alias)", 
			nickname = "searchIdentityAccounts", 
			tags = { AccIdentityAccountController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Search identity accounts", 
			nickname = "searchQuickIdentityAccounts", 
			tags = { AccIdentityAccountController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Identity account detail", 
			nickname = "getIdentityAccount", 
			response = AccIdentityAccountDto.class, 
			tags = { AccIdentityAccountController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_READ, description = "") })
					})
	public ResponseEntity<?> get(
			@ApiParam(value = "Identity account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_UPDATE + "')")
	@ApiOperation(
			value = "Create / update identity account", 
			nickname = "postIdentity", 
			response = AccIdentityAccountDto.class, 
			tags = { AccIdentityAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_UPDATE, description = "")})
				})
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> post(@RequestBody @NotNull AccIdentityAccountDto dto){
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update identity account", 
			nickname = "putIdentityAccount", 
			response = AccIdentityAccountDto.class, 
			tags = { AccIdentityAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Identity account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccIdentityAccountDto dto){
		return super.put(backendId,dto);
	}	
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete identity account", 
			nickname = "deleteIdentityAccount", 
			tags = { AccIdentityAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Identity account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnIdentityAccount", 
			tags = { AccIdentityAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.IDENTITY_ACCOUNT_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Identity account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	protected AccIdentityAccountFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setAccountId(getParameterConverter().toUuid(parameters, "accountId"));
		filter.setIdentityId(getParameterConverter().toEntityUuid(parameters, "identity", IdmIdentity.class));
		filter.setRoleId(getParameterConverter().toUuid(parameters, "roleId"));
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		filter.setOwnership(getParameterConverter().toBoolean(parameters, "ownership"));
		return filter;
	}
}
