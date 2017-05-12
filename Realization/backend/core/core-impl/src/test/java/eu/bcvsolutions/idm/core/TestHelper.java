package eu.bcvsolutions.idm.core;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.dto.IdmContractGuaranteeDto;
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
	
	/**
	 * Creates test identity with random username
	 * 
	 * @return
	 */
	IdmIdentityDto createIdentity();

	/**
	 * Creates test identity with given username
	 * 
	 * @param name
	 * @return
	 */
	IdmIdentityDto createIdentity(String username);
	
	/**
	 * Deletes identity
	 * 
	 * @param id
	 */
	void deleteIdentity(UUID id);

	/**
	 * Creates tree type with random name and code
	 * @return
	 */
	IdmTreeType createTreeType();
	
	/**
	 * Creates tree type with given name = code
	 * 
	 * @param name
	 * @return
	 */
	IdmTreeType createTreeType(String name);

	/**
	 * Creates tree node with random name and code
	 * 
	 * @return
	 */
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
	IdmTreeNode createTreeNode(IdmTreeType treeType, IdmTreeNode parent);

	void deleteTreeNode(UUID id);

	/**
	 * Creates role with random name
	 * 
	 * @return
	 */
	IdmRole createRole();

	/**
	 * Creates role with given name
	 * 
	 * @param name
	 * @return
	 */
	IdmRole createRole(String name);

	/**
	 * Deletes role
	 * 
	 * @param id
	 */
	void deleteRole(UUID id);

	/**
	 * Creates automatic role
	 * 
	 * @param role
	 * @param treeNode
	 * @param skipLongRunningTask
	 * @return
	 */
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
	IdmIdentityRoleDto createIdentityRole(IdmIdentityDto identity, IdmRole role);
	
	/**
	 * Creates assigned identity's role directly (without approving etc.)
	 * 
	 * @param identityContract
	 * @param role
	 * @return
	 */
	IdmIdentityRoleDto createIdentityRole(IdmIdentityContractDto identityContract, IdmRole role);
	
	/**
	 * Creates simple identity contract
	 * 
	 * @param identity
	 * @return
	 */
	IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity);
	
	/**
	 * Deletes identity's contract
	 * 
	 * @param id
	 */
	void deleteIdentityContact(UUID id);
	
	/**
	 * Creates identity contract's guarantee
	 * 
	 * @param identityContractId
	 * @param identityId
	 * @return
	 */
	IdmContractGuaranteeDto createContractGuarantee(UUID identityContractId, UUID identityId);

}
