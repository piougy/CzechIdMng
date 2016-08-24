package eu.bcvsolutions.idm.core.model.service;

import java.util.List;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;

public interface IdmRoleService {
	
	IdmRole get(Long id);

	List<IdmRole> getRolesByIds(String roles);

	IdmRole getByName(String name);
}
