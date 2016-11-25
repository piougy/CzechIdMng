package eu.bcvsolutions.idm.eav.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

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
 * TODO: save confidential values to securedRepository
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultFormService implements FormService {
	
	private final IdmFormDefinitionService formDefinitionService;
	private final IdmFormAttributeService formAttributeService;
	private final PluginRegistry<FormValueService<?, ?>, Class<?>> formValueServices;
	
	@Autowired
	public DefaultFormService(
			IdmFormDefinitionService formDefinitionService,
			IdmFormAttributeService formAttributeService,
			List<? extends FormValueService<?, ?>> formValueServices) {
		Assert.notNull(formDefinitionService);
		Assert.notNull(formAttributeService);
		Assert.notNull(formValueServices);
		//
		this.formDefinitionService = formDefinitionService;
		this.formAttributeService = formAttributeService;
		this.formValueServices = OrderAwarePluginRegistry.create(formValueServices);
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
	 * 
	 * TODO: validations by given form definitions
	 */
	@Transactional
	@SuppressWarnings("unchecked")
	public <O extends FormableEntity, E extends AbstractFormValue<O>> void saveValues(O owner, IdmFormDefinition formDefinition, List<E> values) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(values, "Form values are required!");
		//
		FormValueService<O, E> formValueService = (FormValueService<O, E>) getFormValueService(owner);
		//
		Map<UUID, E> previousValues = new HashMap<>();
		formValueService.getValues(owner, formDefinition).forEach(formValue -> {
			previousValues.put(formValue.getId(), formValue);
		});
		//
		values.forEach(value -> {
			Assert.notNull(value.getFormAttribute(), "Form attribute is required");
			// 
			value.setOwner(owner);
			// set attribute values
			value.setPersistentType(value.getFormAttribute().getPersistentType());
			value.setConfidential(value.getFormAttribute().isConfidential());
			// find values to be removed
			if (value.getId() != null) {
				E previousValue = previousValues.get(value.getId());
				if (previousValue != null) {
					// TODO: fix created and creator audit handler
					value.setCreator(previousValue.getCreator());
					value.setCreated(previousValue.getCreated());
					previousValues.remove(value.getId());
				}
			}
			//
			formValueService.save(value);
		});
		//
		// remove unsaved values by attribute definition (patch method is not implemented now)
		previousValues.values().forEach(formValue -> {
			formValueService.deleteValue(formValue);
		});
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
		//
		@SuppressWarnings("unchecked")
		FormValueService<O, ?> formValueService = (FormValueService<O, ?>) getFormValueService(owner);
		//
		if (formDefinition == null) {
			return Lists.newArrayList(formValueService.getValues(owner, null));
		}
		return Lists.newArrayList(formValueService.getValues(owner, formDefinition));
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
		//
		@SuppressWarnings("unchecked")
		FormValueService<O, ?> formValueService = (FormValueService<O, ?>) getFormValueService(owner);
		formValueService.deleteValues(owner, formDefinition);
	}
	
	/**
	 * Returns form values as map, where key is attribute name
	 * 
	 * @param values
	 * @return
	 */
	@Override
	public <O extends FormableEntity, E extends AbstractFormValue<O>> Map<String, List<E>> toAttributeMap(final List<E> values) {
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
	 * Returns FormValueService for given owner 
	 * 
	 * @param owner
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	private FormValueService getFormValueService(FormableEntity owner) {
		FormValueService formValueService = formValueServices.getPluginFor(owner.getClass());
		if (formValueService == null) {
			throw new IllegalStateException(MessageFormat.format("FormValueService for class [{0}] not found, please check configuration", owner.getClass()));
		}
		return formValueService;
	}
}
