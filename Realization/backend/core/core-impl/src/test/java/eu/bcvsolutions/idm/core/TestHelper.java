package eu.bcvsolutions.idm.core;

import java.util.UUID;

import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;

/**
 * Creates common test entities
 * 
 * TODO: switch entities to dto, move to api, include in abstract integration test
 * 
 * @author Radek Tomiška
 *
 */
public interface TestHelper {

	IdmIdentity createIdentity(String name);

	IdmTreeType createTreeType(String name);

	IdmTreeNode createTreeNode();

	/**
	 * Creates tree node under default tree structure
	 * 
	 * @see {@link IdmTreeTypeService#getDefaultTreeType()}
	 * @param name
	 * @param parent
	 * @return
	 */
	IdmTreeNode createTreeNode(String name, IdmTreeNode parent);

	IdmTreeNode createTreeNode(IdmTreeType treeType, String name, IdmTreeNode parent);

	void deleteTreeNode(UUID id);

	IdmRole createRole();

	IdmRole createRole(String name);

	void deleteRole(UUID id);

	IdmRoleTreeNodeDto createRoleTreeNode(IdmRole role, IdmTreeNode treeNode, boolean skipLongRunningTask);

}