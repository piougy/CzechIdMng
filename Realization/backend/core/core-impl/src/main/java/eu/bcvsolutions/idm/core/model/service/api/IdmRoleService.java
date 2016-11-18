package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

public interface IdmRoleService extends ReadWriteEntityService<IdmRole, RoleFilter>, IdentifiableByNameEntityService<IdmRole> {

	List<IdmRole> getRolesByIds(String roles);
}
