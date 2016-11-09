package eu.bcvsolutions.idm.eav.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.IdmFormAttributeDefinitionRepository;
import eu.bcvsolutions.idm.eav.repository.IdmFormDefinitionRepository;
import eu.bcvsolutions.idm.eav.service.IdmFormDefinitionService;

/**
 * Default implementation of form definition service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmFormDefinitionService extends AbstractReadWriteEntityService<IdmFormDefinition, EmptyFilter> implements IdmFormDefinitionService {

	private final IdmFormDefinitionRepository formDefinitionRepository;
	private final IdmFormAttributeDefinitionRepository formAttributeDefinitionRepository;

	@Autowired
	public DefaultIdmFormDefinitionService(IdmFormDefinitionRepository formDefinitionRepository,
			IdmFormAttributeDefinitionRepository formAttributeDefinitionRepository) {
		Assert.notNull(formDefinitionRepository);
		Assert.notNull(formAttributeDefinitionRepository);
		//
		this.formDefinitionRepository = formDefinitionRepository;
		this.formAttributeDefinitionRepository = formAttributeDefinitionRepository;
	}

	@Override
	protected BaseRepository<IdmFormDefinition, EmptyFilter> getRepository() {
		return formDefinitionRepository;
	}

	@Override
	@Transactional
	public void delete(IdmFormDefinition entity) {
		// delete all attributes in definition
		formAttributeDefinitionRepository.deleteByFormDefinition(entity);
		//
		// TODO: disable definition remove with filled form instances (values)
		// ... or remove relation to deleted definition? Requires get all
		// formable object classes / tables ...
		//
		super.delete(entity);
	}
}
