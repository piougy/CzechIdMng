package eu.bcvsolutions.idm.core.model.service;

import java.util.List;

import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

public interface IdmIdentityRoleService {
	
	IdmIdentityRole get(Long id);

	List<IdmIdentityRole> getByIds(List<String> ids);

	IdmIdentityRole updateByDto(Long id, IdmIdentityRoleDto dto);

	IdmIdentityRole addByDto(IdmIdentityRoleDto dto);

	void delete(Long id);
}
