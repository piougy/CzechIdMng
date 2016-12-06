package eu.bcvsolutions.idm.eav.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.eav.dto.FormAttributeFilter;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.repository.IdmFormAttributeRepository;
import eu.bcvsolutions.idm.eav.service.api.IdmFormAttributeService;

/**
 * Form attribute (attribute definition) service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmFormAttributeService extends AbstractReadWriteEntityService<IdmFormAttribute, FormAttributeFilter> implements IdmFormAttributeService{

	@Autowired
	public DefaultIdmFormAttributeService(IdmFormAttributeRepository formAttributeDefinitionRepository) {
		super(formAttributeDefinitionRepository);
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
	
	@Override
	@Transactional
	public void delete(IdmFormAttribute entity) {
		// attribute with filled values cannot be deleted
		
		super.delete(entity);
	}

}
