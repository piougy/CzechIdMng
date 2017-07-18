package eu.bcvsolutions.idm.core.eav.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
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
 * TODO: secure read operations?
 * TODO: add get method + model
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/form-attributes")
@Api(
		value = IdmFormAttributeController.TAG, 
		description = "Operations with form attributes (eav)", 
		tags = { IdmFormAttributeController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE)
public class IdmFormAttributeController extends DefaultReadWriteEntityController<IdmFormAttribute, FormAttributeFilter>  {

	protected static final String TAG = "Form attributes";
	
	@Autowired
	public IdmFormAttributeController(LookupService entityLookupService) {
		super(entityLookupService);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_ATTRIBUTES_CREATE + "') or hasAuthority('" + CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE + "')")
	@ApiOperation(
			value = "Create / update form attribute", 
			nickname = "postFormAttribute", 
			response = IdmFormAttribute.class, 
			tags = { IdmFormAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_ATTRIBUTES_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_ATTRIBUTES_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE + "')")
	@ApiOperation(
			value = "Update form attribute",
			nickname = "putFormAttribute", 
			response = IdmFormAttribute.class, 
			tags = { IdmFormAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE, description = "") })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE + "')")
	@ApiOperation(
			value = "Patch form attribute", 
			nickname = "patchFormAttribute", 
			response = IdmFormAttribute.class, 
			tags = { IdmFormAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE, description = "") })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_ATTRIBUTES_DELETE + "')")
	@ApiOperation(
			value = "Patch form attribute", 
			nickname = "patchFormAttribute", 
			response = IdmFormAttribute.class, 
			tags = { IdmFormAttributeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_ATTRIBUTES_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.EAV_FORM_ATTRIBUTES_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Form definition's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	public void deleteEntity(IdmFormAttribute entity) {
		// attribute flagged as system attribute can't be deleted from controller
		if (entity.isUnmodifiable()) {
			throw new ResultCodeException(CoreResultCode.FORM_ATTRIBUTE_DELETE_FAILED_SYSTEM_ATTRIBUTE, ImmutableMap.of("code", entity.getCode()));
		}
		super.deleteEntity(entity);
	}
	
	@Override
	protected IdmFormAttribute validateEntity(IdmFormAttribute entity) {
		// check if exist id = create entity, then check if exist old entity = create entity with id
		if (entity.getId() == null) {
			return super.validateEntity(entity);
		}
		IdmFormAttribute oldEntity = getEntity(entity.getId());
		if (oldEntity != null) {
			// check explicit attributes that can't be changed
			if (!oldEntity.getCode().equals(entity.getCode())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "name", "class", entity.getClass().getSimpleName()));
			}
			if (oldEntity.getPersistentType() != entity.getPersistentType()) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "persistentType", "class", entity.getClass().getSimpleName()));
			}
			if (oldEntity.isConfidential() != entity.isConfidential()) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "confidential", "class", entity.getClass().getSimpleName()));
			}
			if (oldEntity.isRequired() != entity.isRequired()) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "required", "class", entity.getClass().getSimpleName()));
			}
			if (oldEntity.isReadonly() != entity.isReadonly()) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "readonly", "class", entity.getClass().getSimpleName()));
			}
			if (oldEntity.isMultiple() != entity.isMultiple()) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "multiple", "class", entity.getClass().getSimpleName()));
			}
			if (oldEntity.isRequired() != entity.isRequired()) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "required", "class", entity.getClass().getSimpleName()));
			}
			if (oldEntity.isUnmodifiable() != entity.isUnmodifiable()) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "unmodifiable", "class", entity.getClass().getSimpleName()));
			}
		}
		return super.validateEntity(entity);
	}
}
