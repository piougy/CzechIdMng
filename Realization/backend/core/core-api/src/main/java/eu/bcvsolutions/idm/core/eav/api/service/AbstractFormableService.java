package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.FormableDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.FormableFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Abstract implementation for generic CRUD operations on a repository for a specific type with extended attributes. 
 * Owner type has to support event processing.
 * 
 * @author Radek Tomiška
 *
 * @see FormableSaveProcessor - saves eav values, after owner's dto is saved
 * @param <DTO> {@link FormableDto} type which supports extended attributes
 * @param <E> {@link FormableEntity} type which supports extended attributes
 * @param <F> {@link BaseFilter} type
 */
public abstract class AbstractFormableService<DTO extends FormableDto, E extends FormableEntity, F extends BaseFilter> 
		extends AbstractEventableDtoService<DTO, E, F>
		implements FormableDtoService<DTO, F> {

	private final FormService formService;
	//
	@Autowired private FormProjectionManager formProjectionManager;
	@Autowired private LookupService lookupService;
	
	@Autowired
	public AbstractFormableService(
			AbstractEntityRepository<E> repository,
			EntityEventManager entityEventManager,
			FormService formService) {
		super(repository, entityEventManager);
		//
		Assert.notNull(formService, "Form service is required for formable service.");
		//
		this.formService = formService;
	}
	
	@Override
	@Transactional
	public EventContext<DTO> publish(EntityEvent<DTO> event, EntityEvent<?> parentEvent, BasePermission... permission) {
		// check access to filled form values
		// access has to be evaluated before event is published - is not available in save internal method
		BasePermission[] permissions = PermissionUtils.trimNull(permission);
		if (!ObjectUtils.isEmpty(permissions)) {
			FormableEntity owner = getOwner(event.getContent());
			FormValueService<FormableEntity> formValueService = formService.getFormValueService(getDtoClass());
			event.getContent().getEavs().forEach(formInstance -> {
				formInstance.getValues().forEach(formValue -> {
					formValue.setOwner(owner); //  set owner is needed for checking access on new values
					Set<String> availablePermissions = formValueService.getPermissions(formValue);
					if (event.hasType(CoreEventType.CREATE)) {
						// Create or update permission, when owner is created.
						if (!PermissionUtils.hasAnyPermission(availablePermissions, IdmBasePermission.CREATE, IdmBasePermission.UPDATE)) {
							throw new ForbiddenEntityException(formValue, IdmBasePermission.CREATE, IdmBasePermission.UPDATE);
						}
					} else {
						// UPDATE is enough for all CUD otherwise.
						if (!PermissionUtils.hasPermission(availablePermissions, IdmBasePermission.UPDATE)) {
							throw new ForbiddenEntityException(formValue, IdmBasePermission.UPDATE);
						}
					}
				});
			});
		}
		//
		return super.publish(event, parentEvent, permission);
	}
	
	@Override
	public DTO saveInternal(DTO dto) {
		final DTO savedDto = super.saveInternal(dto);
		//
		// prevent to lose filled eav values - they are saved after owner is saved (see FormableSaveProcessor).
		// filled values are saved in FormableSaveProcessor - parent event is propagated, when form instance is saved
		savedDto.setEavs(dto.getEavs());
		//
		return savedDto;
	}

	/**
	 * Deletes a given entity with all extended attributes
	 * 
	 * @param dto
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	@Override
	@Transactional
	public void deleteInternal(DTO dto) {
		getFormService().deleteValues(dto);
		//
		super.deleteInternal(dto);
	}
	
	/**
	 * Apply context on given dto.
	 *
	 * If has filter sets field "Add EAV metadata" to True, then we will load the
	 * form instance for every result.
	 *
	 * @param dto
	 * @param context
	 * @param permission
	 *
	 * @return
	 * @since 10.2.0
	 */
	@Override
	protected DTO applyContext(DTO dto, F context, BasePermission... permission) {
		dto = super.applyContext(dto, context, permission);
		if (dto == null) {
			return null;
		}
		//
		if (!(context instanceof FormableFilter)) {
			return dto;
		}
		FormableFilter formableContext = (FormableFilter) context;
		if (formableContext.isAddBasicFields()) {
			IdmFormDefinitionDto basicFields = formProjectionManager.getBasicFieldsDefinition(dto);
			if (basicFields != null) {
				dto.setEavs(Lists.newArrayList(new IdmFormInstanceDto(dto, basicFields, new ArrayList<>())));
			} else {
				dto.setEavs(Lists.newArrayList());
			}
		} else {
			dto.setEavs(Lists.newArrayList());
		}
		//
		// load other for definitions and values
		if (BooleanUtils.isNotTrue(formableContext.getAddEavMetadata())
				&& CollectionUtils.isEmpty(formableContext.getFormDefinitionAttributes())) {
			return dto;
		}
		// load or find other form instances
		List<IdmFormInstanceDto> formInstances;
		if (CollectionUtils.isEmpty(formableContext.getFormDefinitionAttributes())) {
			// backward compatible -> method is overriden e.g. for role attributes
			formInstances = this.getFormInstances(dto, permission);
		} else {
			formInstances = this.findFormInstances(dto, formableContext, permission);
		}
		if (CollectionUtils.isNotEmpty(formInstances)) {
			dto.getEavs().addAll(formInstances);
		}
		//
		return dto;
	}
	
	/**
	 * Returns form instances for given DTO
	 * 
	 * @param dto
	 * @param permission
	 * @return
	 * @deprecated @since 10.3.0 => override {@link #findFormInstances(FormableDto, FormableFilter, BasePermission...)} instead.
	 */
	@Deprecated
	protected List<IdmFormInstanceDto> getFormInstances(DTO dto, BasePermission... permission) {
		return this.findFormInstances(dto, null, permission);
	}
	
	/**
	 * Finds form instances for given DTO.
	 * 
	 * @param dto
	 * @param formableContext
	 * @param permission
	 * @return
	 * @since 10.3.0
	 */
	protected List<IdmFormInstanceDto> findFormInstances(DTO dto, FormableFilter formableContext, BasePermission... permission) {
		return getFormService().findFormInstances(dto, formableContext, permission);
	}
	
	/**
	 * Returns form service
	 * 
	 * @return
	 */
	protected FormService getFormService() {
		return formService;
	}
	
	/**
	 * Prepares new owner instance
	 * 
	 * TODO: move to form service, should be in api?
	 * 
	 * @param formDefinition
	 * @return
	 */
	private FormableEntity getOwner(DTO dto) {
		Assert.notNull(dto, "DTO is required for get owner.");
		//
		FormableEntity formableEntity = null;
		if (dto.getId() != null) {
			formableEntity = (FormableEntity) lookupService.lookupEntity(dto.getClass(), dto.getId());
		}
		// prepare empty owner
		if (formableEntity == null) {
			try {
				formableEntity = (FormableEntity) lookupService.getEntityClass(dto.getClass()).getDeclaredConstructor().newInstance();
				// FIXME: #978 - map dto to entity. Some evaluator could intercept something else than class and identifier ...
				formableEntity.setId(dto.getId());
			} catch (ReflectiveOperationException ex) {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of("identifiableType", dto.getClass()), ex);
			}
		}
		//
		return formableEntity;
	}
	
}
