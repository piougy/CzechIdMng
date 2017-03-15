package eu.bcvsolutions.idm.core.eav.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormDefinitionRepository;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormDefinitionService;

/**
 * Default implementation of form definition service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmFormDefinitionService extends AbstractReadWriteEntityService<IdmFormDefinition, QuickFilter> implements IdmFormDefinitionService {

	private final IdmFormDefinitionRepository formDefinitionRepository;
	private final IdmFormAttributeService formAttributeService;

	@Autowired
	public DefaultIdmFormDefinitionService(IdmFormDefinitionRepository formDefinitionRepository,
			IdmFormAttributeService formAttributeService) {
		super(formDefinitionRepository);
		//
		Assert.notNull(formAttributeService);
		//
		this.formDefinitionRepository = formDefinitionRepository;
		this.formAttributeService = formAttributeService;
	}
	
	/**
	 * Fill default definition name, if no name is given
	 */
	@Override
	public IdmFormDefinition save(IdmFormDefinition entity) {
		if (StringUtils.isEmpty(entity.getName())) {
			entity.setName(DEFAULT_DEFINITION_NAME);
		}
		return super.save(entity);
	}
	
	@Override
	public IdmFormDefinition get(String type, String name) {
		return formDefinitionRepository.findOneByTypeAndName(type, name != null ? name : DEFAULT_DEFINITION_NAME);
	}

	@Override
	@Transactional
	public void delete(IdmFormDefinition entity) {
		// delete all attributes in definition
		FormAttributeFilter filter = new FormAttributeFilter();
		filter.setFormDefinition(entity);
		formAttributeService.find(filter, null).forEach(formAttribute -> {
			formAttributeService.delete(formAttribute);
		});
		entity.setFormAttributes(null); // prevent cascade - duplicit removal
		//
		super.delete(entity);
	}
}
