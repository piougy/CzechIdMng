package eu.bcvsolutions.idm.test.api;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Function;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
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
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationEvaluator;

/**
 * Creates common test entities
 *
 * @author Radek Tomiška
 *
 */
public interface TestHelper {
	
	String DEFAULT_AUTOMATIC_ROLE_NAME = "default";
	String DEFAULT_PASSWORD = "password";
	
	/**
	 * Login as given identity.
	 * Identity has to exists, assigned identity roles and permissions will be used.
	 * 
	 * @param username
	 * @param password
	 */
	LoginDto login(String username, String password);
	
	/**
	 * Logout current logged identity ~ clear secutity context
	 */
	void logout();
	
	/**
	 * Get dto service from context
	 * 
	 * @param dtoServiceType
	 * @return
	 */
	<T extends ReadDtoService<?, ?>> T getService(Class<T> dtoServiceType);

	/**
	 * Creates random unique name
	 *
	 * @return
	 */
	String createName();

	/**
	 * Creates test identity with random username  and default "password"
	 *
	 * @return
	 */
	IdmIdentityDto createIdentity();

	/**
	 * Creates test identity with given username and default "password"
	 *
	 * @param username
	 * @return
	 */
	IdmIdentityDto createIdentity(String username);
	
	/**
	 * Creates test identity with random username and given password
	 *
	 * @param password [optional] when password is not given, then identity password will not be saved - useful when password is not needed
	 * @return
	 */
	IdmIdentityDto createIdentity(GuardedString password);
	
	/**
	 * Creates test identity given username and given password
	 *
	 * @param username
	 * @param password [optional] when password is not given, then identity password will not be saved - usefull when password is not needed
	 * @return
	 */
	IdmIdentityDto createIdentity(String username, GuardedString password);

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
	 * @deprecated use {@link #createAuthorizationPolicy(UUID, GroupPermission, Class, Class, BasePermission...)}
	 */
	@Deprecated
	IdmAuthorizationPolicyDto createSpecificPolicy(UUID role, GroupPermission groupPermission, Class<?> authorizableType, String evaluatorType, BasePermission... permission);
	
	/**
	 * Creates authorization policy
	 * 
	 * @param role
	 * @param groupPermission
	 * @param authorizableType
	 * @param evaluatorType
	 * @param permission
	 * @return
	 */
	IdmAuthorizationPolicyDto createAuthorizationPolicy(
			UUID role, 
			GroupPermission groupPermission, 
			Class<? extends AbstractEntity> authorizableType, 
			Class<? extends AuthorizationEvaluator<? extends AbstractEntity>> evaluatorType, 
		    BasePermission... permission);

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
	 * @param identity
	 * @param role
	 * @param validFrom
	 * @param validTill
	 * @return
	 */
	IdmIdentityRoleDto createIdentityRole(IdmIdentityDto identity, IdmRoleDto role, LocalDate validFrom, LocalDate validTill);

	/**
	 * Creates assigned identity's role directly (without approving etc.)
	 *
	 * @param identityContract
	 * @param role
	 * @return
	 */
	IdmIdentityRoleDto createIdentityRole(IdmIdentityContractDto identityContract, IdmRoleDto role);
	
	/**
	 * Creates assigned identity's role directly (without approving etc.)
	 * 
	 * @param identityContract
	 * @param role
	 * @param validFrom
	 * @param validTill
	 * @return
	 */
	IdmIdentityRoleDto createIdentityRole(IdmIdentityContractDto identityContract, IdmRoleDto role, LocalDate validFrom, LocalDate validTill);

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
	 * Prepare processed item instance by given LRT
	 *
	 * @param lrt IdmLongRunningTaskDto
	 * @return
	 */
	IdmProcessedTaskItemDto prepareProcessedItem(IdmLongRunningTaskDto lrt);

	/**
	 * Prepare processed item instance by given scheduled task
	 *
	 * @param d IdmScheduledTaskDto
	 * @return
	 */
	IdmProcessedTaskItemDto prepareProcessedItem(IdmScheduledTaskDto d);

	/**
	 * Prepare processed item instance by given scheduled task and given state
	 *
	 * @param d IdmScheduledTaskDto
	 * @param state OperationState
	 * @return
	 */
	IdmProcessedTaskItemDto prepareProcessedItem(IdmScheduledTaskDto d, OperationState state);

	/**
	 * Disables / enables given configuration property
	 *
	 * @param configurationPropertyName
	 */
	void setConfigurationValue(String configurationPropertyName, boolean value);

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

	/**
	 * Wait for result - usable for asynchronous tests
	 *
	 * @param continueFunction
	 */
	void waitForResult(Function<String, Boolean> continueFunction);
	
	/**
	 * Wait for result - usable for asynchronous tests
	 * 
	 * @param continueFunction
	 * @param interationWaitMilis [optional] default 300ms
	 * @param iterationCount [optional] default 50 => max wait 300ms x 50 = 15s
	 */
	void waitForResult(Function<String, Boolean> continueFunction, Integer interationWaitMilis, Integer iterationCount);

	/**
	 * Create schedulable task instance
	 * 
	 * @return
	 */
	IdmScheduledTaskDto createSchedulableTask();
	
	/**
	 * Create eav attribute, wit given code, owner class and type
	 * 
	 * @param code
	 * @param clazz
	 * @param type
	 * @return
	 */
	IdmFormAttributeDto createEavAttribute(String code, Class<? extends Identifiable> clazz, PersistentType type);
	
	/**
	 * Save value to eav with code
	 * 
	 * @param ownerId
	 * @param code
	 * @param clazz
	 * @param value
	 */
	void setEavValue(Identifiable owner, IdmFormAttributeDto attribute, Class<? extends Identifiable> clazz, Serializable value, PersistentType type);
	
	/**
	 * Method create new automatic role by attribute for role id
	 * 
	 * @param roleId
	 * @return
	 */
	IdmAutomaticRoleAttributeDto createAutomaticRole(UUID roleId);
	
	/**
	 * Create new rule with given informations. See params.
	 * And remove concept state from automatic role by attribute, without recalculation!
	 * 
	 * @param automaticRoleId
	 * @param comparsion
	 * @param type
	 * @param attrName
	 * @param formAttrId
	 * @param value
	 * @return
	 */
	IdmAutomaticRoleAttributeRuleDto createAutomaticRoleRule(UUID automaticRoleId,
			AutomaticRoleAttributeRuleComparison comparsion, AutomaticRoleAttributeRuleType type, String attrName,
			UUID formAttrId, String value);
}
