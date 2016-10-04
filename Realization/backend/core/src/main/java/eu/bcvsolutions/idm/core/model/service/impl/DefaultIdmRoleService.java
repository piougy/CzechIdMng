package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.dto.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.IdmRoleService;

@Service
public class DefaultIdmRoleService extends AbstractReadWriteEntityService<IdmRole, RoleFilter>  implements IdmRoleService {

	@Autowired
	private IdmRoleRepository idmRoleRepository;

	@Override
	@Transactional(readOnly = true)
	public Page<IdmRole> find(RoleFilter filter, Pageable pageable) {
		if (filter == null) {
			return find(pageable);
		}
		return idmRoleRepository.findQuick(filter.getText(), filter.getRoleType(), pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public IdmRole getByName(String name) {
		return idmRoleRepository.findOneByName(name);
	}

	@Override
	protected BaseRepository<IdmRole> getRepository() {
		return idmRoleRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
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
