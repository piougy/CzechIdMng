package eu.bcvsolutions.idm.core.eav.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteDtoController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * EAV Form definitions
 * 
 * TODO: secure read operations?
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/form-definitions")
@Api(
		value = IdmFormDefinitionController.TAG, 
		description = "Operations with form definitions (eav)", 
		tags = { IdmFormDefinitionController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE)
public class IdmFormDefinitionController extends DefaultReadWriteDtoController<IdmFormDefinitionDto, QuickFilter>  {

	protected static final String TAG = "Form definitions";
	private final FormService formService;
	
	@Autowired
	public IdmFormDefinitionController(IdmFormDefinitionService service, FormService formService) {
		super(service);
		//
		Assert.notNull(formService);
		//
		this.formService = formService;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_CREATE + "') or hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE + "')")
	@ApiOperation(
			value = "Create / update form definition", 
			nickname = "postFormDefinition", 
			response = IdmFormDefinitionDto.class, 
			tags = { IdmFormDefinitionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmFormDefinitionDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE + "')")
	@ApiOperation(
			value = "Update form definition",
			nickname = "putFormDefinition", 
			response = IdmFormDefinitionDto.class, 
			tags = { IdmFormDefinitionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Form definition's uuid identifier", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmFormDefinitionDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE + "')")
	@ApiOperation(
			value = "Patch form definition", 
			nickname = "patchFormDefinition", 
			response = IdmFormDefinitionDto.class, 
			tags = { IdmFormDefinitionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Form definition's uuid identifier", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_DELETE + "')")
	@ApiOperation(
			value = "Delete form definition", 
			nickname = "deleteFormDefinition", 
			tags = { IdmFormDefinitionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Form definition's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/types", method = RequestMethod.GET)
	@ApiOperation(
			value = "Form definition owner types", 
			nickname = "getFormDefinitionOwnerTypes", 
			tags = { IdmFormDefinitionController.TAG },
			notes = "Returns all supported form definition owner types (FormableEntity descendants).")
	public List<String> getOwnerTypes() {
		return formService.getOwnerTypes();
	}
	
	@Override
	public void deleteDto(IdmFormDefinitionDto entity) {
		// definitions flagged as system definition can't be deleted from controller
		if (entity.isUnmodifiable()) {
			throw new ResultCodeException(CoreResultCode.FORM_DEFINITION_DELETE_FAILED_SYSTEM_DEFINITION, ImmutableMap.of("code", entity.getCode()));
		}
		super.deleteDto(entity);
	}
	
	@Override
	protected IdmFormDefinitionDto validateDto(IdmFormDefinitionDto entity) {
		// check if exist id = create entity, then check if exist old entity = create entity with id
		if (entity.getId() == null) {
			return super.validateDto(entity);
		}
		IdmFormDefinitionDto previousDto = getDto(entity.getId());
		
		if (previousDto != null) {
			// type cannot be changed
			if (!previousDto.getType().equals(entity.getType())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "type", "class", entity.getClass().getSimpleName()));
			}
			// check explicit attributes that can't be changed
			if (previousDto.isUnmodifiable() && !previousDto.getCode().equals(entity.getCode())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "name", "class", entity.getClass().getSimpleName()));
			}
		}
		return super.validateDto(entity);
	}
	
	/**
	 * Returns default definition for given ownerClass
	 * 
	 * @param ownerClass
	 * @return
	 */
	public ResponseEntity<?> getDefinition(Class<? extends Identifiable> ownerClass) {
		return new ResponseEntity<>(toResource(getDefinition(ownerClass, (IdmFormDefinitionDto) null)), HttpStatus.OK);
	}
	
	/**
	 * Returns all definitions for given ownerClass
	 * 
	 * @param ownerClass
	 * @return
	 */
	public ResponseEntity<?> getDefinitions(Class<? extends FormableEntity> ownerClass) {
		return new ResponseEntity<>(toResources(formService.getDefinitions(ownerClass), getDtoClass()), HttpStatus.OK);
	}
	
	
	/**
	 * Returns given formDefinition or default definition for given ownerClass, if no formDefinition is given.
	 * 
	 * @param ownerClass
	 * @param formDefinitionId [optional]
	 * @return
	 */
	private IdmFormDefinitionDto getDefinition(Class<? extends Identifiable> ownerClass, IdmFormDefinitionDto formDefinition) {
		Assert.notNull(ownerClass);
		//
		if (formDefinition != null) {
			return formDefinition;
		}
		formDefinition = formService.getDefinition(ownerClass);
		if (formDefinition == null) {			
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("formDefinition", ownerClass));
		}
		return formDefinition;
	}
	
	/**
	 * Gets form definition for given owner type.
	 * 
	 * @param ownerClass owner type
	 * @param definitionCode [optional] definition code, default definition will be returned, if no code is given
	 * @return
	 */
	public IdmFormDefinitionDto getDefinition(Class<? extends Identifiable> ownerClass, String definitionCode) {
		IdmFormDefinitionDto formDefinition = null; // default will be used
		if (StringUtils.isNotEmpty(definitionCode)) {
			formDefinition = formService.getDefinition(ownerClass, definitionCode);
			if (formDefinition == null) {
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of(
						"formDefinition", ownerClass.getSimpleName(),
						"code", definitionCode));
			}
		}
		if (formDefinition == null) {
			formDefinition = formService.getDefinition(ownerClass);
		}
		if (formDefinition == null) {			
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("formDefinition", ownerClass));
		}
		return formDefinition;
	}
	
	/**
	 * Returns owner's form values
	 * 
	 * @param owner
	 * @param formDefinitionId 
	 * @return
	 */
	public Resource<?> getFormValues(Identifiable owner, IdmFormDefinitionDto formDefinition) {
		Assert.notNull(owner); 
		//
		return new Resource<>(formService.getFormInstance(owner, getDefinition(owner.getClass(), formDefinition)));
	}
	
	/**
	 * Saves owner's form values
	 * 
	 * @param owner
	 * @param formDefinitionId
	 * @param formValuesr
	 * @return
	 */
	public Resource<?> saveFormValues(Identifiable owner, IdmFormDefinitionDto formDefinition, List<IdmFormValueDto> formValues) {		
		formDefinition = getDefinition(owner.getClass(), formDefinition); 
		//
		return new Resource<>(formService.saveFormInstance(owner, formDefinition, formValues));
	}	
}
