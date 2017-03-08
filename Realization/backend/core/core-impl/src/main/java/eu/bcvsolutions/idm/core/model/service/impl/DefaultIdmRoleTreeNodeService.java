package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;

/**
 * Automatic role service
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleTreeNodeService extends AbstractReadWriteEntityService<IdmRoleTreeNode, RoleTreeNodeFilter> implements IdmRoleTreeNodeService {

	public DefaultIdmRoleTreeNodeService(AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter> repository) {
		super(repository);
	}

}
