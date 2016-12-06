package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Role service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleService extends ReadWriteEntityService<IdmRole, RoleFilter>, IdentifiableByNameEntityService<IdmRole> {

	/**
	 * Return roles by uuids in string
	 * 
	 * @param roles
	 * @return
	 */
	List<IdmRole> getRolesByIds(String roleIds);
}
