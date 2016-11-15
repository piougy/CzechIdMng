package eu.bcvsolutions.idm.eav.service.impl;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.FormService;
import eu.bcvsolutions.idm.eav.service.FormValueService;
import eu.bcvsolutions.idm.eav.service.IdmFormDefinitionService;

/**
 * Work with form definitions, attributes and their values
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultFormService implements FormService {
	
	private final IdmFormDefinitionService formDefinitionService;
	private final PluginRegistry<FormValueService<?, ?>, Class<?>> formValueServices;
	
	@Autowired
	public DefaultFormService(IdmFormDefinitionService formDefinitionService, List<? extends FormValueService<?, ?>> formValueServices) {
		Assert.notNull(formDefinitionService);
		Assert.notNull(formValueServices);
		//
		this.formDefinitionService = formDefinitionService;
		this.formValueServices = OrderAwarePluginRegistry.create(formValueServices);
	}

	@Override
	public IdmFormDefinition getDefinition(String type, String name) {
		return formDefinitionService.get(type, name);
	}
	
	@Transactional
	@SuppressWarnings("unchecked")
	public <O extends FormableEntity, E extends AbstractFormValue<O>> void saveValues(O owner, List<E> values) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(values, "Form values are required!");
		//
		values.forEach(value -> {
			((FormValueService<O, E>) getFormValueService(owner)).save(value);
		});		
	}

	@Override
	public <O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner) {		
		return getValues(owner, null);
	}
	
	@Override
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
	
	@Override
	public <O extends FormableEntity> void deleteValues(O owner) {
		deleteValues(owner, null);
	}
	
	@Override
	public <O extends FormableEntity> void deleteValues(O owner, IdmFormDefinition formDefinition) {
		Assert.notNull(owner, "Form values owner is required!");
		//
		@SuppressWarnings("unchecked")
		FormValueService<O, ?> formValueService = (FormValueService<O, ?>) getFormValueService(owner);
		formValueService.deleteValues(owner, formDefinition);
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
