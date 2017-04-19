package eu.bcvsolutions.idm.core.eav.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

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

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteEntityController;

/**
 * EAV Form definitions
 * 
 * TODO: Split form definition and form instance controller
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/form-definitions")
public class IdmFormDefinitionController extends DefaultReadWriteEntityController<IdmFormDefinition, QuickFilter>  {

	private final FormService formService;
	
	@Autowired
	public IdmFormDefinitionController(EntityLookupService entityLookupService, FormService formService) {
		super(entityLookupService);
		//
		Assert.notNull(formService);
		//
		this.formService = formService;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_CREATE + "') or hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE + "')")
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EAV_FORM_DEFINITIONS_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	public void deleteEntity(IdmFormDefinition entity) {
		// definitions flagged as system definition can't be deleted from controller
		if (entity.isUnmodifiable()) {
			throw new ResultCodeException(CoreResultCode.FORM_DEFINITION_DELETE_FAILED_SYSTEM_DEFINITION, ImmutableMap.of("name", entity.getName()));
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
			if (!oldEntity.getName().equals(entity.getName())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "name", "class", entity.getClass().getSimpleName()));
			}
			if (!oldEntity.getType().equals(entity.getType())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "type", "class", entity.getClass().getSimpleName()));
			}
		}
		return super.validateEntity(entity);
	}
	
	public ResponseEntity<?> getDefinition(Class<? extends FormableEntity> ownerClass, PersistentEntityResourceAssembler assembler) {
		return new ResponseEntity<>(toResource(getDefinition(ownerClass, (IdmFormDefinition) null), assembler), HttpStatus.OK);
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
	
	@ResponseBody
	@RequestMapping(value = "/search/types", method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<String>>  getFormDefinition() {
		List<String> types = formService.getOwnerTypes();
		ResourcesWrapper<String> resource = new ResourcesWrapper<>(types);
		return new ResponseEntity<ResourcesWrapper<String>>(resource, HttpStatus.OK);
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
	
}
