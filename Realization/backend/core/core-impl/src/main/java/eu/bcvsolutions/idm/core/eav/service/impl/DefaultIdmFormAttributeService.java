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
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormAttributeRepository;

/**
 * Form attribute (attribute definition) service
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmFormAttributeService 
		extends AbstractReadWriteDtoService<IdmFormAttributeDto, IdmFormAttribute, IdmFormAttributeFilter> 
		implements IdmFormAttributeService {

	private final IdmFormAttributeRepository repository;
	private final PluginRegistry<FormValueService<?>, Class<?>> formValueServices;
	
	@Autowired
	public DefaultIdmFormAttributeService(
			IdmFormAttributeRepository repository,
			List<? extends FormValueService<?>> formValueServices) {
		super(repository);
		//
		Assert.notNull(formValueServices);
		//
		this.repository = repository;
		this.formValueServices = OrderAwarePluginRegistry.create(formValueServices);
	}
	
	@Override
	@Transactional
	public IdmFormAttributeDto saveInternal(IdmFormAttributeDto dto) {
		// default seq
		if (dto.getSeq() == null) {
			dto.setSeq((short) 0);
		}
		// check seq
		return super.saveInternal(dto);
	}
	
	@Override
	@Transactional
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void deleteInternal(IdmFormAttributeDto dto) {
		Assert.notNull(dto);
		// attribute with filled values cannot be deleted
		IdmFormValueFilter filter = new IdmFormValueFilter();
		filter.setFormAttributeId(dto.getId());
		formValueServices.getPlugins().forEach(formValueService -> {
			if (formValueService.find(filter, new PageRequest(0, 1)).getTotalElements() > 0) {
				throw new ResultCodeException(CoreResultCode.FORM_ATTRIBUTE_DELETE_FAILED_HAS_VALUES, ImmutableMap.of("formAttribute", dto.getCode()));
			}
		});
		//
		super.deleteInternal(dto);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormAttributeDto findAttribute(String definitionType, String definitionCode, String attributeName) {
		return toDto(repository.findOneByFormDefinition_typeAndFormDefinition_codeAndCode(definitionType, definitionCode, attributeName));
	}

}
