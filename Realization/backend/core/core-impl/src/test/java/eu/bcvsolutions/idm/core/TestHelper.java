package eu.bcvsolutions.idm.core;

import java.util.UUID;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Creates common test entities
 * 
 * TODO: switch entities to dto, move to test-api, include in abstract integration test => then will be usable in other modules
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
	 * @param code
	 * @return
	 */
	IdmTreeType createTreeType(String code);

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
	IdmTreeNode createTreeNode(String code, IdmTreeNode parent);
	IdmTreeNode createTreeNode(IdmTreeType treeType, String code, IdmTreeNode parent);	
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
	 * Creates role with given id and name
	 * 
	 * @param id [optional] if no id is given, then new id is generated
	 * @param name
	 * @return
	 */
	IdmRole createRole(UUID id, String name);

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
	 * Creates identity contract on given position
	 * 
	 * @param identity
	 * @param position
	 * @return
	 */
	IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity, IdmTreeNode position);
	
	/**
	 * Creates identity contract on given position
	 * 
	 * @param identity
	 * @param position
	 * @param validFrom
	 * @param validTill
	 * @return
	 */
	IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity, IdmTreeNode position, LocalDate validFrom, LocalDate validTill);
	
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
