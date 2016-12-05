package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;

/**
 * Default role service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmRoleService extends AbstractReadWriteEntityService<IdmRole, RoleFilter>  implements IdmRoleService {

	private final IdmRoleRepository roleRepository;
	private final IdmIdentityRoleRepository identityRoleRepository;
	
	@Autowired
	public DefaultIdmRoleService(
			IdmRoleRepository roleRepository,
			IdmIdentityRoleRepository identityRoleRepository) {
		super(roleRepository);
		//
		Assert.notNull(identityRoleRepository);
		//
		this.roleRepository = roleRepository;
		this.identityRoleRepository = identityRoleRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public IdmRole getByName(String name) {
		return roleRepository.findOneByName(name);
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
			// TODO: try - catch ...
			idmRoles.add(get(UUID.fromString(id)));
		}
		return idmRoles;
	}
	
	@Override
	public void delete(IdmRole role) {
		// role assigned to identity could not be deleted
		if(identityRoleRepository.countByRole(role) > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_IDENTITY_ASSIGNED, ImmutableMap.of("role", role.getName()));
		}
		// guarantees and compositions are deleted by hibernate mapping		
		super.delete(role);
	}
}
