package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormValueFilter;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormAttributeRepository;
import eu.bcvsolutions.idm.core.eav.service.api.FormValueService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormAttributeService;

/**
 * Form attribute (attribute definition) service
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmFormAttributeService extends AbstractReadWriteEntityService<IdmFormAttribute, FormAttributeFilter> implements IdmFormAttributeService{

	private final IdmFormAttributeRepository repository;
	private final PluginRegistry<FormValueService<?, ?>, Class<?>> formValueServices;
	
	@Autowired
	public DefaultIdmFormAttributeService(
			IdmFormAttributeRepository repository,
			List<? extends FormValueService<?, ?>> formValueServices) {
		super(repository);
		//
		Assert.notNull(formValueServices);
		//
		this.repository = repository;
		this.formValueServices = OrderAwarePluginRegistry.create(formValueServices);
	}
	
	@Override
	@Transactional
	public IdmFormAttribute save(IdmFormAttribute entity) {
		// default seq
		if (entity.getSeq() == null) {
			entity.setSeq((short) 0);
		}
		// check seq
		return super.save(entity);
	}
	
	@Override
	@Transactional
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void delete(IdmFormAttribute entity) {
		Assert.notNull(entity);
		// attribute with filled values cannot be deleted
		FormValueFilter filter = new FormValueFilter();
		filter.setFormAttribute(entity);
		formValueServices.getPlugins().forEach(formValueService -> {
			if (formValueService.find(filter, new PageRequest(0, 1)).getTotalElements() > 0) {
				throw new ResultCodeException(CoreResultCode.FORM_ATTRIBUTE_DELETE_FAILED_HAS_VALUES, ImmutableMap.of("formAttribute", entity.getName()));
			}
		});
		//
		super.delete(entity);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormAttribute findAttribute(String definitionType, String definitionName, String attributeName) {
		return repository.findOneByFormDefinition_typeAndFormDefinition_nameAndName(definitionType, definitionName, attributeName);
	}

}
