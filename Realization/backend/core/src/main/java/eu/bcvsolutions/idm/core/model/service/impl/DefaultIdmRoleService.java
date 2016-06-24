package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.IdmRoleService;

@Service
public class DefaultIdmRoleService implements IdmRoleService {

	@Autowired
	private IdmRoleRepository idmRoleRepository;

	@Override
	@Transactional
	public IdmRole get(Long id) {
		IdmRole entity = idmRoleRepository.findOne(id);
		return entity;
	}

}
