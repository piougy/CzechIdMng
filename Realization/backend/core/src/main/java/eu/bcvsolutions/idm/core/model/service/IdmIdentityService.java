package eu.bcvsolutions.idm.core.model.service;

import org.activiti.engine.runtime.ProcessInstance;

import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

public interface IdmIdentityService {

	IdmIdentity getByUsername(String username);

	String getNiceLabel(IdmIdentity identity);

	IdmIdentity get(Long id);

	boolean addRole(IdmIdentityRole identityRole, boolean startApproveWorkflow);
	
	boolean addRoleByDto(IdmIdentityRoleDto identityRoleDto, boolean startApproveWorkflow);

	ProcessInstance changePermissions(IdmIdentity identity);
}
