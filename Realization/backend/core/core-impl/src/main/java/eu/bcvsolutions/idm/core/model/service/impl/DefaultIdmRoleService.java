package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.IdmRoleService;

@Service
public class DefaultIdmRoleService extends AbstractReadWriteEntityService<IdmRole, RoleFilter>  implements IdmRoleService {

	@Autowired
	private IdmRoleRepository idmRoleRepository;
	
	@Override
	protected BaseRepository<IdmRole, RoleFilter> getRepository() {
		return idmRoleRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public IdmRole getByName(String name) {
		return idmRoleRepository.findOneByName(name);
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
	
	@Override
	public void delete(IdmRole entity) {
		try {
			super.delete(entity);
		} catch(DataIntegrityViolationException ex) {
			// TODO: constraint name - result code mapping and move lower to AbstractReadWriteEntityService
			if (ex.getMessage().contains("fk_idm_identity_role_role")) {
				throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_IDENTITY_ASSIGNED, ImmutableMap.of("role", entity.getName()));
			}
			throw ex;
		}
	}
}
