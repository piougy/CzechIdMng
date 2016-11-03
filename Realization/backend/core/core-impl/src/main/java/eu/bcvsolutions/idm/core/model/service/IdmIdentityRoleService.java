package eu.bcvsolutions.idm.core.model.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

public interface IdmIdentityRoleService {
	
	IdmIdentityRole get(UUID id);

	List<IdmIdentityRole> getByIds(List<String> ids);

	IdmIdentityRole updateByDto(UUID id, IdmIdentityRoleDto dto);

	IdmIdentityRole addByDto(IdmIdentityRoleDto dto);

	void delete(UUID id);
}
