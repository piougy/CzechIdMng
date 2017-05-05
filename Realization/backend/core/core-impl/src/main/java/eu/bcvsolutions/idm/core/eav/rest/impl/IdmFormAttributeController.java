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
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteEntityController;

/**
 * EAV Form definitions
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/form-attributes")
public class IdmFormAttributeController extends DefaultReadWriteEntityController<IdmFormAttribute, FormAttributeFilter>  {

	@Autowired
	public IdmFormAttributeController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}
	
	@Override
	public void deleteEntity(IdmFormAttribute entity) {
		// attribute flagged as system attribute can't be deleted from controller
		if (entity.isUnmodifiable()) {
			throw new ResultCodeException(CoreResultCode.FORM_ATTRIBUTE_DELETE_FAILED_SYSTEM_ATTRIBUTE, ImmutableMap.of("name", entity.getName()));
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
			if (!oldEntity.getName().equals(entity.getName())) {
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
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_ATTRIBUTES_CREATE + "') or hasAuthority('" + CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE + "')")
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_ATTRIBUTES_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_ATTRIBUTES_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
}
