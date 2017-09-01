package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
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

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityContractFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Identity contract endpoint
 * 
 * TODO: eav to dtos
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController // TODO: @RestController after eav to dto
@RequestMapping(value = BaseDtoController.BASE_PATH + "/identity-contracts")
@Api(
		value = IdmIdentityContractController.TAG, 
		description = "Operations with identity contracts", 
		tags = { IdmIdentityContractController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmIdentityContractController extends AbstractReadWriteDtoController<IdmIdentityContractDto, IdentityContractFilter> {
	
	protected static final String TAG = "Contracts";
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public IdmIdentityContractController(
			LookupService entityLookupService, 
			IdmIdentityContractService identityContractService,
			IdmFormDefinitionController formDefinitionController) {
		super(identityContractService);
		//
		Assert.notNull(formDefinitionController);
		//
		this.formDefinitionController = formDefinitionController;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@ApiOperation(
			value = "Search identity contracts (/search/quick alias)", 
			nickname = "searchIdentityContracts", 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@ApiOperation(
			value = "Search identity contracts", 
			nickname = "searchQuickIdentityContracts", 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete identity contracts (selectbox usage)", 
			nickname = "autocompleteIdentityContracts", 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@ApiOperation(
			value = "Identity contract detail", 
			nickname = "getIdentityContract", 
			response = IdmIdentityContractDto.class, 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Contract's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')")
	@ApiOperation(
			value = "Create / update identity contract", 
			nickname = "postIdentityContract", 
			response = IdmIdentityContractDto.class, 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmIdentityContractDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')")
	@ApiOperation(
			value = "Update identity contract", 
			nickname = "putIdentityContract", 
			response = IdmIdentityContractDto.class, 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Contract's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmIdentityContractDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_DELETE + "')")
	@ApiOperation(
			value = "Delete identity contract", 
			nickname = "deleteIdentityContract", 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Contract's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnIdentityContract", 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Contract's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@ApiOperation(
			value = "Identity contract extended attributes form definitions", 
			nickname = "getIdentityContractFormDefinitions", 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") })
				})
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Contract's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			PersistentEntityResourceAssembler assembler) {
		return formDefinitionController.getDefinitions(IdmIdentityContract.class, assembler);
	}
	
	/**
	 * Returns entity's filled form values
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@ApiOperation(
			value = "Identity contract form definition - read values", 
			nickname = "getIdentityContractFormValues", 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_READ, description = "") })
				})
	public Resources<?> getFormValues(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			PersistentEntityResourceAssembler assembler) {
		IdmIdentityContractDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		checkAccess(dto, IdmBasePermission.READ);
		//
		IdmFormDefinition formDefinition = formDefinitionController.getDefinition(IdmIdentityContract.class, definitionCode);
		//
		return formDefinitionController.getFormValues(dto.getId(), IdmIdentityContract.class, formDefinition, assembler);
	}
	
	/**
	 * Saves entity's form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	@ApiOperation(
			value = "Identity contract form definition - save values", 
			nickname = "postIdentityContractFormValues", 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITYCONTRACT_UPDATE, description = "") })
				})
	public Resources<?> saveFormValues(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			@ApiParam(value = "Filled form data.", required = true)
			@RequestBody @Valid List<IdmIdentityContractFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmIdentityContractDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		// 
		checkAccess(dto, IdmBasePermission.UPDATE);
		//
		IdmFormDefinition formDefinition = formDefinitionController.getDefinition(IdmIdentityContract.class, definitionCode);
		//
		return formDefinitionController.saveFormValues(dto.getId(), IdmIdentityContract.class, formDefinition, formValues, assembler);
	}
	
	@Override
	protected IdentityContractFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdentityContractFilter filter = new IdentityContractFilter(parameters);
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setIdentity(getParameterConverter().toEntityUuid(parameters, "identity", IdmIdentity.class));
		filter.setValid(getParameterConverter().toBoolean(parameters, "valid"));
		filter.setExterne(getParameterConverter().toBoolean(parameters, "externe"));
		filter.setDisabled(getParameterConverter().toBoolean(parameters, "disabled"));
		filter.setMain(getParameterConverter().toBoolean(parameters, "main"));
		filter.setValidNowOrInFuture(getParameterConverter().toBoolean(parameters, "validNowOrInFuture"));
		// TODO: localdate converters
		return filter;
	}
}
