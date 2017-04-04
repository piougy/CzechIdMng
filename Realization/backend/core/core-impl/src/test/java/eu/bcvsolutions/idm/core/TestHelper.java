package eu.bcvsolutions.idm.core;

import java.util.UUID;

import eu.bcvsolutions.idm.core.model.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Creates common test entities
 * 
 * TODO: switch entities to dto, move to api, include in abstract integration test
 * 
 * @author Radek Tomiška
 *
 */
public interface TestHelper {
	
	IdmIdentity createIdentity();

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
	
	/**
	 * Creates uuid permission evaluator authorization policy 
	 * 
	 * @param role
	 * @param permission
	 * @return
	 */
	IdmAuthorizationPolicyDto createUuidPolicy(UUID role, UUID authorizableEntity, BasePermission... permission);
	
	/**
	 * Creates base permission evaluator authorization policy 
	 * 
	 * @param role
	 * @param permission
	 * @return
	 */
	IdmAuthorizationPolicyDto createBasePolicy(UUID role, BasePermission... permission);
	
	/**
	 * Creates assigned identity's role directly (without approving etc.)
	 * 
	 * @param identity
	 * @param role
	 * @return
	 */
	IdmIdentityRole createIdentityRole(IdmIdentity identity, IdmRole role);
	
	/**
	 * Creates assigned identity's role directly (without approving etc.)
	 * 
	 * @param identityContract
	 * @param role
	 * @return
	 */
	IdmIdentityRole createIdentityRole(IdmIdentityContract identityContract, IdmRole role);
	
	/**
	 * Creates simple identity contract
	 * 
	 * @param identity
	 * @return
	 */
	IdmIdentityContract createIdentityContact(IdmIdentity identity);

}