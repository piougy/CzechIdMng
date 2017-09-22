package eu.bcvsolutions.idm.vs.rest.impl;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resource;
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

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.repository.filter.VsAccountFilter;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsAccountDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Rest methods for virtual system account
 * 
 * @author Svanda
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/vs/accounts")
@Api(
		value = VsAccountController.TAG,  
		tags = { VsAccountController.TAG }, 
		description = "Operations with accounts (in virtual system)",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class VsAccountController extends AbstractReadWriteDtoController<VsAccountDto, VsAccountFilter> {

	protected static final String TAG = "Accounts";
	//
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public VsAccountController(
			VsAccountService service, 
			IdmFormDefinitionController formDefinitionController) {
		super(service);
		//
		Assert.notNull(formDefinitionController);
		//
		this.formDefinitionController = formDefinitionController;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Search accounts (/search/quick alias)", 
			nickname = "searchAccounts", 
			tags = { VsAccountController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Search accounts", 
			nickname = "searchQuickAccounts", 
			tags = { VsAccountController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete accounts (selectbox usage)", 
			nickname = "autocompleteAccounts", 
			tags = { VsAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Account detail", 
			nickname = "getAccount", 
			response = VsAccountDto.class, 
			tags = { VsAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_CREATE + "') or hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE + "')")
	@ApiOperation(
			value = "Create / update account", 
			nickname = "postAccount", 
			response = VsAccountDto.class, 
			tags = { VsAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_CREATE, description = ""),
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_CREATE, description = ""),
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody VsAccountDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE + "')")
	@ApiOperation(
			value = "Update account", 
			nickname = "putAccount", 
			response = VsAccountDto.class, 
			tags = { VsAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody VsAccountDto dto) {
		return super.put(backendId, dto);
	}
	

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_DELETE + "')")
	@ApiOperation(
			value = "Delete account", 
			nickname = "deleteAccount", 
			tags = { VsAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')"
			+ " or hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "What logged account can do with given record", 
			nickname = "getPermissionsOnAccount", 
			tags = { VsAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = ""),
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = ""),
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE, description = "")})
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * Returns form definition to given account.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Account extended attributes form definitions", 
			nickname = "getAccountFormDefinitions", 
			tags = { VsAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = "") })
				})
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return formDefinitionController.getDefinitions(VsAccount.class);
	}
	
	/**
	 * Returns filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Account form definition - read values", 
			nickname = "getAccountFormValues", 
			tags = { VsAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_READ, description = "") })
				})
	public Resource<?> getFormValues(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode) {
		VsAccountDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(VsAccount.class, definitionCode);
		//
		return formDefinitionController.getFormValues(entity, formDefinition);
	}
	
	/**
	 * Saves connector configuration form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	@ApiOperation(
			value = "Account form definition - save values", 
			nickname = "postAccountFormValues", 
			tags = { VsAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE, description = "") })
				})
	public Resource<?> saveFormValues(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			@ApiParam(value = "Filled form data.", required = true)
			@RequestBody @Valid List<IdmFormValueDto> formValues) {		
		VsAccountDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(entity, IdmBasePermission.UPDATE);
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(VsAccount.class, definitionCode);
		//
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues);
	}
	
	@Override
	protected VsAccountFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new VsAccountFilter();
	}
}
