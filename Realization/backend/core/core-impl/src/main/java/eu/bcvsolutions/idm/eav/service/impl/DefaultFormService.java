package eu.bcvsolutions.idm.eav.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.eav.service.api.FormValueService;
import eu.bcvsolutions.idm.eav.service.api.IdmFormAttributeService;
import eu.bcvsolutions.idm.eav.service.api.IdmFormDefinitionService;

/**
 * Work with form definitions, attributes and their values
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultFormService implements FormService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultFormService.class);
	private final IdmFormDefinitionService formDefinitionService;
	private final IdmFormAttributeService formAttributeService;
	private final PluginRegistry<FormValueService<?, ?>, Class<?>> formValueServices;
	private final EntityEventManager entityEventManager;
	
	@Autowired
	public DefaultFormService(
			IdmFormDefinitionService formDefinitionService,
			IdmFormAttributeService formAttributeService,
			List<? extends FormValueService<?, ?>> formValueServices,
			EntityEventManager entityEventManager) {
		Assert.notNull(formDefinitionService);
		Assert.notNull(formAttributeService);
		Assert.notNull(formValueServices);
		Assert.notNull(entityEventManager);
		//
		this.formDefinitionService = formDefinitionService;
		this.formAttributeService = formAttributeService;
		this.formValueServices = OrderAwarePluginRegistry.create(formValueServices);
		this.entityEventManager = entityEventManager;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinition getDefinition(String type) {
		return this.getDefinition(type, null);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinition getDefinition(String type, String name) {
		return formDefinitionService.get(type, name);		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinition getDefinition(Class<? extends FormableEntity> ownerClass) {
		Assert.notNull(ownerClass, "Owner class is required!");
		//
		return formDefinitionService.get(getDefaultDefinitionType(ownerClass), null);		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDefaultDefinitionType(Class<? extends FormableEntity> ownerClass) {
		Assert.notNull(ownerClass, "Owner class is required!");
		//
		return ownerClass.getCanonicalName();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public IdmFormDefinition createDefinition(String type, String name, List<IdmFormAttribute> formAttributes) {
		Assert.hasLength(type);
		//
		// create definition
		IdmFormDefinition formDefinition = new  IdmFormDefinition();
		formDefinition.setType(type);	
		formDefinition.setName(name);
		formDefinition = formDefinitionService.save(formDefinition);
		//
		// and their attributes
		if (formAttributes != null) {
			Short seq = 0;
			for (IdmFormAttribute formAttribute : formAttributes) {
				// default attribute order
				if (formAttribute.getSeq() == null) {
					formAttribute.setSeq(seq);
					seq++;
				}
				formAttribute.setFormDefinition(formDefinition);
				formDefinition.addFormAttribute(formAttributeService.save(formAttribute));
			}
		}
		return formDefinition;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public IdmFormDefinition createDefinition(Class<? extends FormableEntity> ownerClass, List<IdmFormAttribute> formAttributes) {
		Assert.notNull(ownerClass, "Owner class is required!");
		//
		return createDefinition(getDefaultDefinitionType(ownerClass), null, formAttributes);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * TODO: validations by given form definition? I don't think, it will not be useful in synchronization etc. - only FE validations will be enough ...
	 */
	@Transactional
	public <O extends FormableEntity, E extends AbstractFormValue<O>> void saveValues(O owner, IdmFormDefinition formDefinition, List<E> values) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(formDefinition, "Form definition is required!");
		Assert.notNull(values, "Form values are required!");
		//
		FormValueService<O, E> formValueService = getFormValueService(owner);
		//
		Map<UUID, E> previousValues = new HashMap<>();
		formValueService.getValues(owner, formDefinition).forEach(formValue -> {
			previousValues.put(formValue.getId(), formValue);
		});
		//
		values.forEach(value -> {
			// value could contant attribute id only
			IdmFormAttribute attributeId = value.getFormAttribute();
			Assert.notNull(attributeId, "Form attribute is required");
			IdmFormAttribute attribute = formDefinition.getMappedAttribute(attributeId.getId());
			Assert.notNull(attribute, "Form attribute is required");
			// 
			value.setFormAttribute(attribute);
			value.setOwner(owner);
			// set attribute values
			value.setPersistentType(attribute.getPersistentType());
			value.setConfidential(attribute.isConfidential());
			//
			E previousValue = value.getId() == null ? null :previousValues.get(value.getId());
			if (previousValue != null) {
				// saved values will nod be removed
				previousValues.remove(value.getId());
				// the same value should not be updated 
				// confidential value is always updated - only new values are sent from client
				// TODO: implement isEquals method in FormValue (dates are now working now - date from FE vs DB has different chronology - use isEquals method)
				if (value.isConfidential() || !java.util.Objects.equals(previousValue.getValue(), value.getValue())) {
					// update value
					formValueService.save(value);
					LOG.trace("FormValue [{}:{}] for owner [{}] was updated", attribute.getName(), value.getId(), owner);
				}
			} else {
				// create new value
				formValueService.save(value);
				LOG.trace("FormValue [{}:{}] for owner [{}] was created", attribute.getName(), value.getId(), owner);
			}
		});
		//
		// remove unsaved values by attribute definition (patch method is not implemented now)
		previousValues.values().stream()
			.filter(formValue -> {
				// confidential values has to removed directly
				// they could not be sent with form (only changed values)
				return !formValue.isConfidential();
			})
			.forEach(value -> {
				formValueService.deleteValue(value);
				LOG.trace("FormValue [{}:{}] for owner [{}] was deleted", value.getFormAttribute().getName(), value.getId(), owner);
			});
		//
		// publish event - eav was saved
		// TODO: this whole method could be moved to processor (=> could be overriden in some module)
		entityEventManager.process(new CoreEvent<O>(CoreEventType.EAV_SAVE, owner)); 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public <O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner) {		
		return getValues(owner, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public <O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner, IdmFormDefinition formDefinition) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		//
		FormValueService<O, ?> formValueService = getFormValueService(owner);
		//
		if (formDefinition == null) {
			// values from default form definition
			formDefinition = getDefinition(owner.getClass());
			Assert.notNull(formDefinition, MessageFormat.format("Default form definition for ownerClass [{0}] not found and is required, fix appliaction configuration!", owner.getClass()));
		}
		return Lists.newArrayList(formValueService.getValues(owner, formDefinition));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public <O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner, IdmFormDefinition formDefinition, String attributeName) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		Assert.hasLength(attributeName, "Attribute name is required");
		// TODO: possible optimalization - repository - find single value
		List<AbstractFormValue<O>> rawFormValues = this.getValues(owner, formDefinition);
		Map<String, List<AbstractFormValue<O>>> formValues = toValueMap(rawFormValues);
		if (!formValues.containsKey(attributeName)) {
			return Collections.<AbstractFormValue<O>>emptyList();
		}
		return formValues.get(attributeName);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public <O extends FormableEntity> void deleteValues(O owner) {
		deleteValues(owner, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public <O extends FormableEntity> void deleteValues(O owner, IdmFormDefinition formDefinition) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		//
		FormValueService<O, ?> formValueService = getFormValueService(owner);
		formValueService.deleteValues(owner, formDefinition);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <O extends FormableEntity, E extends AbstractFormValue<O>> Map<String, List<E>> toValueMap(final List<E> values) {
		Assert.notNull(values);
		//
		Map<String, List<E>> results = new HashMap<>();
		for(E value : values) {
			String key = value.getFormAttribute().getName();
			if (!results.containsKey(key)) {
				results.put(key, new ArrayList<>());
			}
			results.get(key).add(value);
		}
		
		return results;
	}
	
	/**
	 * Returns raw FormValue values as map, where key is attribute name
	 * 
	 * @param values
	 * @return
	 */
	@Override
	public Map<String, List<Object>> toPersistentValueMap(final List<AbstractFormValue<FormableEntity>> values) {
		Assert.notNull(values);
		//
		Map<String, List<Object>> results = new HashMap<>();
		for(AbstractFormValue<?> value : values) {
			String key = value.getFormAttribute().getName();
			if (!results.containsKey(key)) {
				results.put(key, new ArrayList<>());
			}
			results.get(key).add(value.getValue());
		}
		
		return results;
	}
	
	/**
	 * Returns raw values - usable for single multi attribute values
	 * 
	 * @param values
	 * @return
	 */
	@Override
	public List<Object> toPersistentValues(final List<AbstractFormValue<FormableEntity>> values) {
		Assert.notNull(values);
		//
		return values.stream()
				.map(value -> {
					return value.getValue();
					})
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns single FormValue by persistent type - usable for single attribute value
	 * 
	 * @see {@link PersistentType}
	 * @param values
	 * @return
	 * @throws IllegalArgumentException if attributte has multi values
	 */
	@Override
	public Object toSinglePersistentValue(final List<AbstractFormValue<FormableEntity>> values) {
		Assert.notNull(values);
		if (values.size() > 1) {
			throw new IllegalArgumentException(MessageFormat.format("Attribute [{}] has mutliple values [{}]", values.get(0).getFormAttribute().getName(), values.size()));
		}
		return values.get(0).getValue();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getConfidentialStorageKey(FormableEntity owner, IdmFormAttribute attribute) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		Assert.notNull(attribute, "Form attribute is required!");
		//
		FormValueService<FormableEntity, ?> formValueService = getFormValueService(owner);
		String key = formValueService.getConfidentialStorageKey(attribute);
		LOG.debug("Confidential storage key for attribute [{}] is [{}].", attribute.getName(), key);
		return key;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <O extends FormableEntity, E extends AbstractFormValue<O>> Serializable getConfidentialPersistentValue(E guardedValue) {
		Assert.notNull(guardedValue);
		Assert.notNull(guardedValue.getOwner());
		//
		FormValueService<O, E> formValueService = getFormValueService(guardedValue.getOwner());
		return formValueService.getConfidentialPersistentValue(guardedValue);
	}
	
	/**
	 * Returns FormValueService for given owner 
	 * 
	 * @param owner
	 * @param <O> values owner
	 * @param <E> values entity
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	private <O extends FormableEntity, E extends AbstractFormValue<O>> FormValueService<O, E> getFormValueService(O owner) {
		FormValueService<O, E> formValueService = (FormValueService<O, E>)formValueServices.getPluginFor(owner.getClass());
		if (formValueService == null) {
			throw new IllegalStateException(MessageFormat.format("FormValueService for class [{0}] not found, please check configuration", owner.getClass()));
		}
		return formValueService;
	}
}
