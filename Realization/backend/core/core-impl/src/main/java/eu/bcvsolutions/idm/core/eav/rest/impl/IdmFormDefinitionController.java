package eu.bcvsolutions.idm.core.eav.rest.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
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
@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
@RequestMapping(value = BaseEntityController.BASE_PATH + "/form-definitions")
public class IdmFormDefinitionController extends DefaultReadWriteEntityController<IdmFormDefinition, EmptyFilter>  {

	private final FormService formService;
	
	@Autowired
	public IdmFormDefinitionController(EntityLookupService entityLookupService, FormService formService) {
		super(entityLookupService);
		//
		Assert.notNull(formService);
		//
		this.formService = formService;
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
