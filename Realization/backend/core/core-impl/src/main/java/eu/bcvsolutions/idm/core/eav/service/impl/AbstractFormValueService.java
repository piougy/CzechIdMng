package eu.bcvsolutions.idm.core.eav.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormValueFilter;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.eav.service.api.FormValueService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Custom form value service can be registered by spring plugin
 * 
 * @author Radek Tomiška
 *
 * @param <O> values owner
 * @param <E> values entity
 */
public abstract class AbstractFormValueService<O extends FormableEntity, E extends AbstractFormValue<O>> implements FormValueService<O, E> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractFormValueService.class);
	private final Class<O> ownerClass;
	private final Class<E> formValueClass;
	private final ConfidentialStorage confidentialStorage;
	private final AbstractFormValueRepository<O, E> repository;

	@SuppressWarnings("unchecked")
	public AbstractFormValueService(AbstractFormValueRepository<O, E> repository, ConfidentialStorage confidentialStorage) {
		Assert.notNull(repository);
		Assert.notNull(confidentialStorage);
		//
		Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), FormValueService.class);
		this.ownerClass = (Class<O>)genericTypes[0];
		this.formValueClass = (Class<E>)genericTypes[1];
		this.repository = repository;
		this.confidentialStorage = confidentialStorage;
	}
	
	@Override
	public Class<O> getOwnerClass() {
		return ownerClass;
	}
	
	@Override
	public Class<E> getFormValueClass() {
		return formValueClass;
	}
	
	@Override
	public E newValue() {
		try {
			return formValueClass.newInstance();
		} catch(IllegalAccessException | InstantiationException ex) {
			throw new IllegalStateException(MessageFormat.format("New form value instance could not be created. Check class [{0}] and their constructors (no-argument constructor is required)", formValueClass), ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
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
	public E get(UUID id) {
		E formValue = getRepository().findOne(id);
		// TODO: read value from confidential storage? Or leave reading from confidential store to client?
		if (formValue != null && formValue.isConfidential()) {
			LOG.debug("FormValue [{}] is persisted id confidential storage, returning proxy string only", formValue.getId());
		}
		return formValue;
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
	public E save(E entity) {
		Assert.notNull(entity);
		//
		// check, if value has to be persisted in confidentional storage 
		Serializable formValue = entity.getValue();
		if (entity.isConfidential()) {
			entity.clearValues();
			if (formValue != null) {
				// we need only to know, if value was filled
				entity.setStringValue(GuardedString.SECRED_PROXY_STRING);
			}
		}
		entity = getRepository().save(entity);
		
		// save values to confidential storage
		if (entity.isConfidential()) {
			confidentialStorage.save(entity.getId(), entity.getClass(), getConfidentialStorageKey(entity.getFormAttribute()), formValue);
			LOG.debug("FormValue [{}] is persisted in confidential storage", entity.getId());
		}
		return entity;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<E> getValues(O owner, IdmFormDefinition formDefiniton) {
		//
		if (formDefiniton == null) {
			return Lists.newArrayList(getRepository().findByOwner(owner));
		}
		return getRepository().findByOwnerAndFormAttribute_FormDefinitionOrderBySeqAsc(owner, formDefiniton);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<E> getValues(O owner, IdmFormAttribute attribute) {
		Assert.notNull(attribute, "Form attribute definition is required!");
		//
		return getRepository().findByOwnerAndFormAttributeOrderBySeqAsc(owner, attribute);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<E> find(FormValueFilter<O> filter, Pageable pageable) {
		if (filter == null) {
			return getRepository().findAll(pageable);
		}
		return getRepository().find(filter, pageable);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Transactional
	public void deleteValue(E value) {
		Assert.notNull(value);
		//
		LOG.debug("FormValue [{}] will be removed", value.getId());
		if (value.isConfidential()) {
			LOG.debug("FormValue [{}] will be removed from confidential storage", value.getId());
			confidentialStorage.delete(value.getId(), value.getClass(), getConfidentialStorageKey(value.getFormAttribute()));
		}
		getRepository().delete(value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Transactional
	public void deleteValues(O owner, IdmFormDefinition formDefiniton) {
		getValues(owner, formDefiniton).forEach(formValue -> {
			deleteValue(formValue);
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Transactional
	public void deleteValues(O owner, IdmFormAttribute attribute) {
		Assert.notNull(attribute, "Form attribute definition is required!");
		//
		getValues(owner, attribute).forEach(formValue -> {
			deleteValue(formValue);
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getConfidentialStorageKey(IdmFormAttribute attribute) {
		Assert.notNull(attribute);
		//
		return CONFIDENTIAL_STORAGE_VALUE_PREFIX + ":" + attribute.getName();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable getConfidentialPersistentValue(E guardedValue) {
		Assert.notNull(guardedValue);
		IdmFormAttribute attribute = guardedValue.getFormAttribute();
		Assert.notNull(attribute);
		//
		return confidentialStorage.get(guardedValue.getId(), guardedValue.getClass(), getConfidentialStorageKey(attribute));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<O> findOwners(IdmFormAttribute attribute, Serializable persistentValue, Pageable pageable) {
		Assert.notNull(attribute);
		DefaultFormValue value = new DefaultFormValue(attribute);
		value.setValue(persistentValue);
		AbstractFormValueRepository<O, E> repository = getRepository();
		//
		switch (attribute.getPersistentType()) {
			case INT:
			case LONG:
				return repository.findOwnersByLongValue(attribute, value.getLongValue(), pageable);
			case BOOLEAN:
				return repository.findOwnersByBooleanValue(attribute, value.getBooleanValue(), pageable);
			case DATE:
			case DATETIME:
				return repository.findOwnersByDateValue(attribute, value.getDateValue(), pageable);
			case DOUBLE:
			case CURRENCY:
				return repository.findOwnersByDoubleValue(attribute, value.getDoubleValue(), pageable);
			case BYTEARRAY: {
				return repository.findOwnersByByteArrayValue(attribute, value.getByteValue(), pageable);
			}
			default:
				return repository.findOwnersByStringValue(attribute, value.getStringValue(), pageable);
		}
	}
	
	/**
	 * For value conversion only
	 * 
	 * @author Radek Tomiška
	 *
	 */
	@SuppressWarnings({ "serial", "rawtypes" })
	private class DefaultFormValue extends AbstractFormValue {
		
		public DefaultFormValue(IdmFormAttribute formAttribute) {
			super(formAttribute);
		}

		@Override
		public FormableEntity getOwner() {
			return null;
		}

		@Override
		public void setOwner(FormableEntity owner) {
			// nothing
		}		
	}
}
