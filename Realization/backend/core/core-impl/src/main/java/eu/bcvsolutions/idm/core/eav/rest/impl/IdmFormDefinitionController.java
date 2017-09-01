package eu.bcvsolutions.idm.core.eav.rest.impl;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteEntityController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * EAV Form definitions
 * 
 * TODO: Split form definition and form instance controller
 * TODO: secure read operations?
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/form-definitions")
@Api(
		value = IdmFormDefinitionController.TAG, 
		description = "Operations with form definitions (eav)", 
		tags = { IdmFormDefinitionController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE)
public class IdmFormDefinitionController extends DefaultReadWriteEntityController<IdmFormDefinition, QuickFilter>  {

	protected static final String TAG = "Form definitions";
	private final FormService formService;
	
	@Autowired
	public IdmFormDefinitionController(LookupService entityLookupService, FormService formService) {
		super(entityLookupService);
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
			response = IdmFormDefinition.class, 
			tags = { IdmFormDefinitionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE + "')")
	@ApiOperation(
			value = "Update form definition",
			nickname = "putFormDefinition", 
			response = IdmFormDefinition.class, 
			tags = { IdmFormDefinitionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Form definition's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE + "')")
	@ApiOperation(
			value = "Patch form definition", 
			nickname = "patchFormDefinition", 
			response = IdmFormDefinition.class, 
			tags = { IdmFormDefinitionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Form definition's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
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
	public ResponseEntity<ResourcesWrapper<String>> getOwnerTypes() {
		List<String> types = formService.getOwnerTypes();
		ResourcesWrapper<String> resource = new ResourcesWrapper<>(types);
		return new ResponseEntity<ResourcesWrapper<String>>(resource, HttpStatus.OK);
	}
	
	@Override
	public void deleteEntity(IdmFormDefinition entity) {
		// definitions flagged as system definition can't be deleted from controller
		if (entity.isUnmodifiable()) {
			throw new ResultCodeException(CoreResultCode.FORM_DEFINITION_DELETE_FAILED_SYSTEM_DEFINITION, ImmutableMap.of("code", entity.getCode()));
		}
		super.deleteEntity(entity);
	}
	
	@Override
	protected IdmFormDefinition validateEntity(IdmFormDefinition entity) {
		// check if exist id = create entity, then check if exist old entity = create entity with id
		if (entity.getId() == null) {
			return super.validateEntity(entity);
		}
		IdmFormDefinition oldEntity = getEntity(entity.getId());
		if (oldEntity != null) {
			// check explicit attributes that can't be changed
			if (!oldEntity.getCode().equals(entity.getCode())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "name", "class", entity.getClass().getSimpleName()));
			}
			if (!oldEntity.getType().equals(entity.getType())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "type", "class", entity.getClass().getSimpleName()));
			}
		}
		return super.validateEntity(entity);
	}
	
	/**
	 * Returns default definition for given ownerClass
	 * 
	 * @param ownerClass
	 * @param assembler
	 * @return
	 */
	public ResponseEntity<?> getDefinition(Class<? extends FormableEntity> ownerClass, PersistentEntityResourceAssembler assembler) {
		return new ResponseEntity<>(toResource(getDefinition(ownerClass, (IdmFormDefinition) null), assembler), HttpStatus.OK);
	}
	
	/**
	 * Returns all definitions for given ownerClass
	 * 
	 * @param ownerClass
	 * @param assembler
	 * @return
	 */
	public ResponseEntity<?> getDefinitions(Class<? extends FormableEntity> ownerClass, PersistentEntityResourceAssembler assembler) {
		return new ResponseEntity<>(toResources(formService.getDefinitions(ownerClass), assembler, getEntityClass(), null), HttpStatus.OK);
	}
	
	
	/**
	 * Returns given formDefinition or default definition for given ownerClass, if no formDefinition is given.
	 * 
	 * @param ownerClass
	 * @param formDefinitionId [optional]
	 * @return
	 */
	private IdmFormDefinition getDefinition(Class<? extends FormableEntity> ownerClass, IdmFormDefinition formDefinition) {
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
	public IdmFormDefinition getDefinition(Class<? extends FormableEntity> ownerClass, String definitionCode) {
		IdmFormDefinition formDefinition = null; // default will be used
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
	 * @param assembler
	 * @return
	 */
	public Resources<?> getFormValues(FormableEntity owner, IdmFormDefinition formDefinition, PersistentEntityResourceAssembler assembler) {
		Assert.notNull(owner); 
		//
		return toResources(formService.getValues(owner, getDefinition(owner.getClass(), formDefinition)), assembler, owner.getClass(), null);
	}
	
	public Resources<?> getFormValues(UUID ownerId, Class<? extends FormableEntity> ownerType, IdmFormDefinition formDefinition, PersistentEntityResourceAssembler assembler) {
		Assert.notNull(ownerId);
		Assert.notNull(ownerType);
		//
		return toResources(formService.getValues(ownerId, ownerType, getDefinition(ownerType, formDefinition)), assembler, ownerType, null);
	}
	
	/**
	 * Saves owner's form values
	 * 
	 * @param owner
	 * @param formDefinitionId
	 * @param formValues
	 * @param assembler
	 * @return
	 */
	public <O extends FormableEntity> Resources<?> saveFormValues(
			O owner, 
			IdmFormDefinition formDefinition, 
			List<? extends AbstractFormValue<O>> formValues, 
			PersistentEntityResourceAssembler assembler) {		
		formDefinition = getDefinition(owner.getClass(), formDefinition); 
		formService.saveValues(owner, formDefinition, formValues);
		//
		return toResources(formService.getValues(owner, formDefinition), assembler, owner.getClass(), null);
	}
	
	public <O extends FormableEntity> Resources<?> saveFormValues(
			UUID ownerId,
			Class<O> ownerType,
			IdmFormDefinition formDefinition, 
			List<? extends AbstractFormValue<O>> formValues, 
			PersistentEntityResourceAssembler assembler) {		
		formDefinition = getDefinition(ownerType, formDefinition); 
		formService.saveValues(ownerId, ownerType, formDefinition, formValues);
		//
		return toResources(formService.getValues(ownerId, ownerType, formDefinition), assembler, ownerType, null);
	}
	
}
