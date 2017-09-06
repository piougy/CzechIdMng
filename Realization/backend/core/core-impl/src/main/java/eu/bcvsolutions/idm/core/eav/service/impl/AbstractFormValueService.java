package eu.bcvsolutions.idm.core.eav.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Custom form value service can be registered by spring plugin
 * 
 * @author Radek Tomiška
 *
 * @param <O> values owner
 * @param <E> values entity
 */
public abstract class AbstractFormValueService<O extends FormableEntity, E extends AbstractFormValue<O>> 
		extends AbstractReadWriteDtoService<IdmFormValueDto, E, IdmFormValueFilter<O>>
		implements FormValueService<O> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractFormValueService.class);
	private final Class<O> ownerClass;
	private final Class<E> formValueClass;
	private final ConfidentialStorage confidentialStorage;
	private final AbstractFormValueRepository<O, E> repository;
	
	@SuppressWarnings("unchecked")
	public AbstractFormValueService(AbstractFormValueRepository<O, E> repository, ConfidentialStorage confidentialStorage) {
		super(repository);
		//
		Assert.notNull(repository);
		Assert.notNull(confidentialStorage);
		//
		Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), AbstractFormValueService.class);
		this.ownerClass = (Class<O>)genericTypes[0];
		this.formValueClass = (Class<E>)genericTypes[1];
		this.repository = repository;
		this.confidentialStorage = confidentialStorage;
	}
	
	@Override
	public Class<O> getOwnerClass() {
		return ownerClass;
	}
	
	public Class<E> getFormValueClass() {
		return formValueClass;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		// secured internally by value owner
		return null;
	}
	
	@Override
	protected IdmFormValueDto toDto(E entity) {
		IdmFormValueDto dto = super.toDto(entity);
		dto.setOwnerId(entity.getOwner().getId());
		dto.setOwnerType(entity.getOwner().getClass());
		return dto;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected E toEntity(IdmFormValueDto dto, E entity) {
		entity = super.toEntity(dto, entity);
		entity.setOwner((O) dto.getOwner());
		return entity;
	}

	@Override
	public boolean supports(Class<?> delimiter) {
		return ownerClass.isAssignableFrom(delimiter);
	}
	
	/**
	 * Returns entity repository
	 * 
	 * @return
	 */
	protected AbstractFormValueRepository<O, E> getRepository() {
		return repository;
	}
	
	/**
	 * Returns entity by given id. Returns null, if entity is not exists. For AbstractEntity uuid or string could be given.
	 */
	@Override
	@Transactional(readOnly = true)
	public IdmFormValueDto get(Serializable id, BasePermission... permission) {
		E formValue = getEntity(id, permission);
		// TODO: read value from confidential storage? Or leave reading from confidential store to client?
		if (formValue != null && formValue.isConfidential()) {
			LOG.debug("FormValue [{}] is persisted id confidential storage, returning proxy string only", formValue.getId());
		}
		return toDto(formValue);
	}
	
	@Override
	public IdmFormValueDto save(IdmFormValueDto entity) {
		return this.save(entity, (BasePermission) null);
	}
	
	/**
	 * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
	 * 
	 * @param entity
	 * @return the saved entity
	 */
	@Override
	@Transactional
	public IdmFormValueDto saveInternal(IdmFormValueDto dto) {
		Assert.notNull(dto);
		//
		// check, if value has to be persisted in confidentional storage 
		Serializable formValue = dto.getValue();
		if (dto.isConfidential()) {
			dto.clearValues();
			if (formValue != null) {
				// we need only to know, if value was filled
				dto.setStringValue(GuardedString.SECRED_PROXY_STRING);
			}
		}
		Assert.notNull(dto);
		//
		E persistedEntity = null;
		if (dto.getId() != null) {
			persistedEntity = this.getEntity(dto.getId());
		}
		E entity = getRepository().save(toEntity(dto, persistedEntity));
		//
		// save values to confidential storage
		if (entity.isConfidential()) {
			confidentialStorage.save(entity.getId(), entity.getClass(), getConfidentialStorageKey(entity.getFormAttribute().getId()), formValue);
			LOG.debug("FormValue [{}] is persisted in confidential storage", entity.getId());
		}
		return toDto(entity);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmFormValueFilter<O> filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getDefinitionId() != null) {
			predicates.add(builder.equal(root.get(AbstractFormValue_.formAttribute).get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.id), filter.getDefinitionId()));
		}
		//
		if (filter.getAttributeId() != null) {
			predicates.add(builder.equal(root.get(AbstractFormValue_.formAttribute).get(IdmFormAttribute_.id), filter.getAttributeId()));
		}
		//
		if (filter.getOwner() != null) {
			predicates.add(builder.equal(root.get("owner"), filter.getOwner()));
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmFormValueDto> getValues(O owner, IdmFormDefinitionDto formDefiniton) {
		Assert.notNull(owner);
		Assert.notNull(owner.getId());
		//
		if (formDefiniton == null) {
			return toDtos(Lists.newArrayList(getRepository().findByOwner_Id(owner.getId())), false);
		}
		return toDtos(getRepository().findByOwner_IdAndFormAttribute_FormDefinition_IdOrderBySeqAsc(owner.getId(), formDefiniton.getId()), false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmFormValueDto> getValues(O owner, IdmFormAttributeDto attribute) {
		Assert.notNull(owner);
		Assert.notNull(owner.getId());
		Assert.notNull(attribute, "Form attribute definition is required!");
		//
		return toDtos(getRepository().findByOwner_IdAndFormAttribute_IdOrderBySeqAsc(owner.getId(), attribute.getId()), false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmFormValueDto> find(IdmFormValueFilter<O> filter, Pageable pageable) {
		return super.find(filter, pageable, (BasePermission) null);
	}
	
	@Override
	public void delete(IdmFormValueDto value) {
		this.delete(value, (BasePermission) null);
	}
	
	@Override
	@Transactional
	public void deleteInternal(IdmFormValueDto dto) {
		Assert.notNull(dto);
		//
		LOG.debug("FormValue [{}] will be removed", dto.getId());
		if (dto.isConfidential()) {
			LOG.debug("FormValue [{}] will be removed from confidential storage", dto.getId());
			confidentialStorage.delete(dto.getId(), dto.getClass(), getConfidentialStorageKey(dto.getFormAttribute()));
		}
		super.deleteInternal(dto);
	}
	
	@Transactional
	public void deleteValues(O owner, IdmFormDefinitionDto formDefiniton) {
		getValues(owner, formDefiniton).forEach(formValue -> {
			delete(formValue);
		});
	}
	
	@Transactional
	public void deleteValues(O owner, IdmFormAttributeDto attribute) {
		Assert.notNull(attribute, "Form attribute definition is required!");
		//
		getValues(owner, attribute).forEach(formValue -> {
			delete(formValue);
		});
	}
	
	@Override
	public String getConfidentialStorageKey(UUID formAttributeId) {
		Assert.notNull(formAttributeId);
		//
		return CONFIDENTIAL_STORAGE_VALUE_PREFIX + ":" + formAttributeId;
	}

	@Override
	public Serializable getConfidentialPersistentValue(IdmFormValueDto guardedValue) {
		Assert.notNull(guardedValue);
		//
		return confidentialStorage.get(guardedValue.getId(), getEntityClass(), getConfidentialStorageKey(guardedValue.getFormAttribute()));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<O> findOwners(IdmFormAttributeDto attribute, Serializable persistentValue, Pageable pageable) {
		Assert.notNull(attribute);
		IdmFormValueDto value = new IdmFormValueDto(attribute);
		value.setValue(persistentValue);
		AbstractFormValueRepository<O, E> repository = getRepository();
		//
		switch (attribute.getPersistentType()) {
			case INT:
			case LONG:
				return repository.findOwnersByLongValue(attribute.getId(), value.getLongValue(), pageable);
			case BOOLEAN:
				return repository.findOwnersByBooleanValue(attribute.getId(), value.getBooleanValue(), pageable);
			case DATE:
			case DATETIME:
				return repository.findOwnersByDateValue(attribute.getId(), value.getDateValue(), pageable);
			case DOUBLE:
			case CURRENCY:
				return repository.findOwnersByDoubleValue(attribute.getId(), value.getDoubleValue(), pageable);
			case BYTEARRAY: {
				return repository.findOwnersByByteArrayValue(attribute.getId(), value.getByteValue(), pageable);
			} // texts
			default:
				return repository.findOwnersByStringValue(attribute.getId(), value.getStringValue(), pageable);
		}
	}
}
