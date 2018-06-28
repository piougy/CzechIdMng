package eu.bcvsolutions.idm.core.rest.impl;

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
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Contract time slice controller
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/contract-slices")
@Api(value = IdmContractSliceController.TAG, description = "Operations with contract slices", tags = {
		IdmContractSliceController.TAG }, produces = BaseController.APPLICATION_HAL_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmContractSliceController
		extends AbstractEventableDtoController<IdmContractSliceDto, IdmContractSliceFilter> {

	protected static final String TAG = "Contract slice";
	private final IdmFormDefinitionController formDefinitionController;

	@Autowired
	public IdmContractSliceController(LookupService entityLookupService,
			IdmContractSliceService identityContractService, IdmFormDefinitionController formDefinitionController) {
		super(identityContractService);
		//
		Assert.notNull(formDefinitionController);
		//
		this.formDefinitionController = formDefinitionController;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@ApiOperation(value = "Search contract slices (/search/quick alias)", nickname = "searchIdentityContracts", tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }) })
	public Resources<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@ApiOperation(value = "Search contract slices", nickname = "searchQuickIdentityContracts", tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }) })
	public Resources<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_AUTOCOMPLETE + "')")
	@ApiOperation(value = "Autocomplete contract slices (selectbox usage)", nickname = "autocompleteIdentityContracts", tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_AUTOCOMPLETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_AUTOCOMPLETE, description = "") }) })
	public Resources<?> autocomplete(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countContractSlices", 
			tags = { IdmIdentityContractController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@ApiOperation(value = "Contract slice detail", nickname = "getIdentityContract", response = IdmContractSliceDto.class, tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }) })
	public ResponseEntity<?> get(
			@ApiParam(value = "Contract's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.CONTRACTSLICE_UPDATE + "')")
	@ApiOperation(value = "Create / update contract slice", nickname = "postIdentityContract", response = IdmContractSliceDto.class, tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_UPDATE, description = "") }) })
	public ResponseEntity<?> post(@Valid @RequestBody IdmContractSliceDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_UPDATE + "')")
	@ApiOperation(value = "Update contract slice", nickname = "putIdentityContract", response = IdmContractSliceDto.class, tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_UPDATE, description = "") }) })
	public ResponseEntity<?> put(
			@ApiParam(value = "Contract's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			@Valid @RequestBody IdmContractSliceDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_DELETE + "')")
	@ApiOperation(value = "Delete contract slice", nickname = "deleteIdentityContract", tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_DELETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_DELETE, description = "") }) })
	public ResponseEntity<?> delete(
			@ApiParam(value = "Contract's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@ApiOperation(value = "What logged identity can do with given record", nickname = "getPermissionsOnIdentityContract", tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }) })
	public Set<String> getPermissions(
			@ApiParam(value = "Contract's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@ApiOperation(value = "Contract slice extended attributes form definitions", nickname = "getIdentityContractFormDefinitions", tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }) })
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Contract's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return formDefinitionController.getDefinitions(IdmIdentityContract.class);
	}

	/**
	 * Returns entity's filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@ApiOperation(value = "Contract slice form definition - read values", nickname = "getIdentityContractFormValues", tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_READ, description = "") }) })
	public Resource<?> getFormValues(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true) @PathVariable @NotNull String backendId,
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE) @RequestParam(name = "definitionCode", required = false) String definitionCode) {
		IdmContractSliceDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		checkAccess(dto, IdmBasePermission.READ);
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmIdentityContract.class,
				definitionCode);
		//
		return formDefinitionController.getFormValues(dto, formDefinition);
	}

	/**
	 * Saves entity's form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = { RequestMethod.POST, RequestMethod.PATCH } )
	@ApiOperation(value = "Contract slice form definition - save values", nickname = "postIdentityContractFormValues", tags = {
			IdmContractSliceController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.CONTRACTSLICE_UPDATE, description = "") }) })
	public Resource<?> saveFormValues(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true) @PathVariable @NotNull String backendId,
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE) @RequestParam(name = "definitionCode", required = false) String definitionCode,
			@ApiParam(value = "Filled form data.", required = true) @RequestBody @Valid List<IdmFormValueDto> formValues) {
		IdmContractSliceDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		checkAccess(dto, IdmBasePermission.UPDATE);
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmIdentityContract.class,
				definitionCode);
		//
		return formDefinitionController.saveFormValues(dto, formDefinition, formValues);
	}

	@Override
	protected IdmContractSliceFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmContractSliceFilter filter = new IdmContractSliceFilter(parameters);
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setIdentity(getParameterConverter().toEntityUuid(parameters, "identity", IdmIdentity.class));
		filter.setValid(getParameterConverter().toBoolean(parameters, "valid"));
		filter.setExterne(getParameterConverter().toBoolean(parameters, "externe"));
		filter.setDisabled(getParameterConverter().toBoolean(parameters, "disabled"));
		filter.setMain(getParameterConverter().toBoolean(parameters, "main"));
		filter.setValidNowOrInFuture(getParameterConverter().toBoolean(parameters, "validNowOrInFuture"));
		filter.setExcludeContract(getParameterConverter().toUuid(parameters, "excludeContract"));
		filter.setParentContract(getParameterConverter().toUuid(parameters, "parentContract"));
		filter.setWithoutParent(getParameterConverter().toBoolean(parameters, "withoutParent"));
		filter.setContractCode(getParameterConverter().toString(parameters, "contractCode"));

		return filter;
	}
}
