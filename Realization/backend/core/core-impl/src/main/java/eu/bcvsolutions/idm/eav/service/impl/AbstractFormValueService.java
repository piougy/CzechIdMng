package eu.bcvsolutions.idm.eav.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.eav.dto.FormValueFilter;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.eav.service.api.FormValueService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Custom form value service can be registered by spring plugin
 * 
 * @author Radek Tomiška
 *
 * @param <O> values owner
 * @param <E> values entity
 * @param <F> filter
 */
public abstract class AbstractFormValueService<O extends FormableEntity, E extends AbstractFormValue<O>> implements FormValueService<O, E> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractFormValueService.class);
	private final Class<O> ownerClass;
	private final ConfidentialStorage confidentialStorage;

	@Autowired
	@SuppressWarnings("unchecked")
	public AbstractFormValueService(ConfidentialStorage confidentialStorage) {
		Assert.notNull(confidentialStorage);
		//
		ownerClass = (Class<O>) GenericTypeResolver.resolveTypeArguments(getClass(), AbstractFormValueService.class)[0];
		this.confidentialStorage = confidentialStorage;
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
	protected abstract AbstractFormValueRepository<O, E> getRepository();
	
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
			entity.clear();
			if (formValue != null) {
				// we need only to know, if value was filled
				entity.setStringValue(GuardedString.SECRED_PROXY_STRING);
			}
		}
		entity = getRepository().save(entity);
		
		// save values to confidential storage
		if (entity.isConfidential()) {
			LOG.debug("FormValue [{}] is persisted id confidential storage", entity.getId());
			confidentialStorage.save(entity, getConfidentialStorageKey(entity.getFormAttribute()), formValue);
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
			confidentialStorage.delete(value, getConfidentialStorageKey(value.getFormAttribute()));
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
		Assert.notNull(guardedValue.getFormAttribute());
		//
		return confidentialStorage.get(guardedValue, getConfidentialStorageKey(guardedValue.getFormAttribute()));
	}
}
