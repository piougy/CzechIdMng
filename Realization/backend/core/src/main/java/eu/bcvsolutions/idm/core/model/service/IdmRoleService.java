package eu.bcvsolutions.idm.core.model.service;

import java.util.List;

import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

public interface IdmRoleService extends ReadWriteEntityService<IdmRole, QuickFilter>, IdentifiableByNameEntityService<IdmRole> {

	List<IdmRole> getRolesByIds(String roles);
}
