package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Role service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleDtoService extends ReadWriteDtoService<IdmRoleDto, IdmRole, RoleFilter>{

	/**
	 * Return roles by uuids in string
	 * 
	 * @param roles
	 * @return
	 */
	List<IdmRole> getRolesByIds(String roleIds);
}
