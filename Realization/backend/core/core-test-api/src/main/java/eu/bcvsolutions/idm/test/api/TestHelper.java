package eu.bcvsolutions.idm.test.api;

import java.util.UUID;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;

/**
 * Creates common test entities
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
	 * @param username
	 * @return
	 */
	IdmIdentityDto createIdentity(String username);
	
	/**
	 * Creates test RoleCatalogue with random code and name
	 *
	 * @return
	 */
	IdmRoleCatalogueDto createRoleCatalogue();
	
	/**
	 * Creates test RoleCatalogue with given code = name
	 *
	 * @param code
	 * @return
	 */
	IdmRoleCatalogueDto createRoleCatalogue(String code);
	
	/**
	 * Creates test RoleCatalogue with given code = name and parent.
	 * @param code
	 * @param parentId
	 * @return
	 */
	IdmRoleCatalogueDto createRoleCatalogue(String code, UUID parentId);
	
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
	IdmTreeTypeDto createTreeType();
	
	/**
	 * Creates tree type with given name = code
	 * 
	 * @param code
	 * @return
	 */
	IdmTreeTypeDto createTreeType(String code);

	/**
	 * Creates tree node with random name and code
	 * 
	 * @return
	 */
	IdmTreeNodeDto createTreeNode();

	/**
	 * Creates tree node under default tree structure
	 * 
	 * @see {@link IdmTreeTypeService#getDefaultTreeType()}
	 * @param name
	 * @param parent
	 * @return
	 */
	IdmTreeNodeDto createTreeNode(String code, IdmTreeNodeDto parent);
	IdmTreeNodeDto createTreeNode(IdmTreeTypeDto treeType, String code, IdmTreeNodeDto parent);	
	IdmTreeNodeDto createTreeNode(IdmTreeTypeDto treeType, IdmTreeNodeDto parent);

	void deleteTreeNode(UUID id);

	/**
	 * Creates role with random name
	 * 
	 * @return
	 */
	IdmRoleDto createRole();

	/**
	 * Creates role with given name
	 * 
	 * @param name
	 * @return
	 */
	IdmRoleDto createRole(String name);
	
	/**
	 * Creates role with given id and name
	 * 
	 * @param id [optional] if no id is given, then new id is generated
	 * @param name
	 * @return
	 */
	IdmRoleDto createRole(UUID id, String name);

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
	IdmRoleTreeNodeDto createRoleTreeNode(IdmRoleDto role, IdmTreeNodeDto treeNode, boolean skipLongRunningTask);
	
	/**
	 * Creates uuid permission evaluator authorization policy 
	 * 
	 * @param role
	 * @param permission
	 * @return
	 */
	IdmAuthorizationPolicyDto createUuidPolicy(UUID roleId, UUID authorizableEntity, BasePermission... permission);
	
	/**
	 * Creates base permission evaluator authorization policy 
	 * 
	 * @param role
	 * @param permission
	 * @return
	 */
	IdmAuthorizationPolicyDto createBasePolicy(UUID role, BasePermission... permission);
	
	/**
	 * Creates base permission evaluator authorization policy 
	 * 
	 * @param role
	 * @param groupPermission
	 * @param authorizableType
	 * @param permission
	 * @return
	 */
	IdmAuthorizationPolicyDto createBasePolicy(UUID role, GroupPermission groupPermission, Class<?> authorizableType, BasePermission... permission);
	
	/**
	 * Creates specific permission evaluator authorization policy 
	 * @param role
	 * @param groupPermission
	 * @param authorizableType
	 * @param evaluatorType
	 * @param permission
	 * @return
	 */
	IdmAuthorizationPolicyDto createSpecificPolicy(UUID role, GroupPermission groupPermission, Class<?> authorizableType, String evaluatorType, BasePermission... permission);
	
	/**
	 * Creates assigned identity's role directly (without approving etc.)
	 * 
	 * @param identity
	 * @param role
	 * @return
	 */
	IdmIdentityRoleDto createIdentityRole(IdmIdentityDto identity, IdmRoleDto role);
	
	/**
	 * Creates assigned identity's role directly (without approving etc.)
	 * 
	 * @param identityContract
	 * @param role
	 * @return
	 */
	IdmIdentityRoleDto createIdentityRole(IdmIdentityContractDto identityContract, IdmRoleDto role);
	
	/**
	 * Returns prime identity contract
	 * 
	 * @param identityId
	 * @return
	 */
	IdmIdentityContractDto getPrimeContract(UUID identityId);
	
	/**
	 * Creates simple identity contract
	 * 
	 * @param identity
	 * @return
	 */
	IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity);
	
	/**
	 * Creates identity contract on given position
	 * 
	 * @param identity
	 * @param position
	 * @return
	 */
	IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity, IdmTreeNodeDto position);
	
	/**
	 * Creates identity contract on given position
	 * 
	 * @param identity
	 * @param position
	 * @param validFrom
	 * @param validTill
	 * @return
	 */
	IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity, IdmTreeNodeDto position, LocalDate validFrom, LocalDate validTill);
	
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

	/**
	 * Assign roles through role request (manual, execute immediately)
	 * 
	 * @param contract
	 * @param roles roles to add
	 * @return
	 */
	IdmRoleRequestDto assignRoles(IdmIdentityContractDto contract, IdmRoleDto... roles);

	/**
	 * Assign roles through role request (manual, execute immediately)
	 * @param contract
	 * @param startInNewTransaction
	 * @param roles
	 * @return
	 */
	IdmRoleRequestDto assignRoles(IdmIdentityContractDto contract, boolean startInNewTransaction, IdmRoleDto... roles);
	
	/**
	 * Enables given processor
	 * 
	 * @param processorType
	 */
	void enable(Class<? extends EntityEventProcessor<?>> processorType);
	
	/**
	 * Disables given processor
	 * 
	 * @param processorType
	 */
	void disable(Class<? extends EntityEventProcessor<?>> processorType);

}
