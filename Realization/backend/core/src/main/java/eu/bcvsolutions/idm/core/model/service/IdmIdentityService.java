package eu.bcvsolutions.idm.core.model.service;

import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

public interface IdmIdentityService {

	IdmIdentity getByUsername(String username);

	String getNiceLabel(IdmIdentity identity);

	IdmIdentity get(Long id);

	boolean addRole(IdmIdentityRole identityRole, boolean startApproveWorkflow);
	
	boolean addRole(IdmIdentityRoleDto identityRoleDto, boolean startApproveWorkflow);
}
