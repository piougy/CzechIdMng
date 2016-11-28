package eu.bcvsolutions.idm.eav.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.eav.dto.FormAttributeDefinitionFilter;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.repository.IdmFormAttributeDefinitionRepository;
import eu.bcvsolutions.idm.eav.service.api.IdmFormAttributeService;

/**
 * Form attribute (attribute definition) service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmFormAttributeService extends AbstractReadWriteEntityService<IdmFormAttribute, FormAttributeDefinitionFilter> implements IdmFormAttributeService{

	private final IdmFormAttributeDefinitionRepository formAttributeDefinitionRepository;

	@Autowired
	public DefaultIdmFormAttributeService(IdmFormAttributeDefinitionRepository formAttributeDefinitionRepository) {
		Assert.notNull(formAttributeDefinitionRepository);
		//
		this.formAttributeDefinitionRepository = formAttributeDefinitionRepository;
	}
	
	@Override
	protected AbstractEntityRepository<IdmFormAttribute, FormAttributeDefinitionFilter> getRepository() {
		return formAttributeDefinitionRepository;
	}
	
	@Override
	@Transactional
	public IdmFormAttribute save(IdmFormAttribute entity) {
		// default seq
		if (entity.getSeq() == null) {
			entity.setSeq((short) 0);
		}
		return super.save(entity);
	}

}
