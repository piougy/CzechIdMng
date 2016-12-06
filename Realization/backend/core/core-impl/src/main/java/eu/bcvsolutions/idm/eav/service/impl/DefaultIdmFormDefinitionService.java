package eu.bcvsolutions.idm.eav.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.IdmFormAttributeRepository;
import eu.bcvsolutions.idm.eav.repository.IdmFormDefinitionRepository;
import eu.bcvsolutions.idm.eav.service.api.IdmFormDefinitionService;

/**
 * Default implementation of form definition service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmFormDefinitionService extends AbstractReadWriteEntityService<IdmFormDefinition, EmptyFilter> implements IdmFormDefinitionService {

	private final IdmFormDefinitionRepository formDefinitionRepository;
	private final IdmFormAttributeRepository formAttributeRepository;

	@Autowired
	public DefaultIdmFormDefinitionService(IdmFormDefinitionRepository formDefinitionRepository,
			IdmFormAttributeRepository formAttributeDefinitionRepository) {
		super(formDefinitionRepository);
		Assert.notNull(formAttributeDefinitionRepository);
		//
		this.formDefinitionRepository = formDefinitionRepository;
		this.formAttributeRepository = formAttributeDefinitionRepository;
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
		formAttributeRepository.deleteByFormDefinition(entity);
		//
		// TODO: disable definition remove with filled form instances (values)
		// ... or remove relation to deleted definition? Requires get all
		// formable object classes / tables ...
		//
		super.delete(entity);
	}
}
