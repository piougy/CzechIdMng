package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

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
	
	@Override
	@Transactional
	public IdmRole getByName(String name) {
		IdmRole entity = idmRoleRepository.findOneByName(name);
		return entity;
	}

	@Override
	public List<IdmRole> getRolesByIds(String roles) {
		if (roles == null) {
			return null;
		}
		List<IdmRole> idmRoles = new ArrayList<>();
		String[] rolesArray = roles.split(",");
		for (String id : rolesArray) {
			idmRoles.add(get(Long.parseLong(id)));
		}
		return idmRoles;
	}
}
