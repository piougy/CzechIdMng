package eu.bcvsolutions.idm.core;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.model.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.UuidEvaluator;

/**
 * Creates common test entities
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class DefaultTestHelper implements TestHelper {
	
	@Autowired
	private IdmTreeNodeService treeNodeService;
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired
	private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired
	private IdmRoleTreeNodeRepository roleTreeNodeReposiotry;
	@Autowired
	private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	
	@Override
	public IdmIdentity createIdentity() {
		return createIdentity("test");
	}
	
	@Override
	public IdmIdentity createIdentity(String name) {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername(name + "_" + System.currentTimeMillis());
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		return identity;
	}
	
	@Override
	public void deleteIdentity(UUID id) {
		identityService.deleteById(id);
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.TestHelper#createTreeType(java.lang.String)
	 */
	@Override
	public IdmTreeType createTreeType(String name) {
		IdmTreeType treeType = new IdmTreeType();
		treeType.setCode(name + "-" + System.currentTimeMillis());
		treeType.setName(name);
		return treeTypeService.save(treeType);
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.TestHelper#createTreeNode()
	 */
	@Override
	public IdmTreeNode createTreeNode() {
		return createTreeNode("test", null);
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.TestHelper#createTreeNode(java.lang.String, eu.bcvsolutions.idm.core.model.entity.IdmTreeNode)
	 */
	@Override
	public IdmTreeNode createTreeNode(String name, IdmTreeNode parent) {
		return createTreeNode(treeTypeService.getDefaultTreeType(), name, parent);
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.TestHelper#createTreeNode(eu.bcvsolutions.idm.core.model.entity.IdmTreeType, java.lang.String, eu.bcvsolutions.idm.core.model.entity.IdmTreeNode)
	 */
	@Override
	public IdmTreeNode createTreeNode(IdmTreeType treeType, String name, IdmTreeNode parent) {
		Assert.notNull(treeType, "Tree type is required - test environment is wrong configured, test data is not prepared!");
		//
		IdmTreeNode node = new IdmTreeNode();
		node.setParent(parent);
		node.setCode(name + "-" + System.currentTimeMillis());
		node.setName(name);
		node.setTreeType(treeType);
		return treeNodeService.save(node);
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.TestHelper#deleteTreeNode(java.util.UUID)
	 */
	@Override
	public void deleteTreeNode(UUID id) {
		treeNodeService.deleteById(id);
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.TestHelper#createRole()
	 */
	@Override
	public IdmRole createRole() {
		return createRole("test");
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.TestHelper#createRole(java.lang.String)
	 */
	@Override
	public IdmRole createRole(String name) {
		IdmRole roleC = new IdmRole();
		roleC.setName(name + "-" + System.currentTimeMillis());
		return roleService.save(roleC);
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.TestHelper#deleteRole(java.util.UUID)
	 */
	@Override
	public void deleteRole(UUID id) {
		roleService.deleteById(id);
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.TestHelper#createRoleTreeNode(eu.bcvsolutions.idm.core.model.entity.IdmRole, eu.bcvsolutions.idm.core.model.entity.IdmTreeNode)
	 */
	@Override
	public IdmRoleTreeNodeDto createRoleTreeNode(IdmRole role, IdmTreeNode treeNode, boolean skipLongRunningTask) {
		IdmRoleTreeNodeDto roleTreeNode = new IdmRoleTreeNodeDto();
		roleTreeNode.setRole(role.getId());
		roleTreeNode.setTreeNode(treeNode.getId());
		if (skipLongRunningTask) {
			UUID id = roleTreeNodeReposiotry.save(roleTreeNodeService.toEntity(roleTreeNode, null)).getId();
			return roleTreeNodeService.getDto(id);
		}
		return roleTreeNodeService.save(roleTreeNode);
	}
	
	@Override
	public IdmAuthorizationPolicyDto createBasePolicy(UUID role, BasePermission... permission) {
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role);
		dto.setEvaluator(BasePermissionEvaluator.class);
		dto.setPermissions(permission);
		return authorizationPolicyService.save(dto);
	}
	
	@Override
	public IdmAuthorizationPolicyDto createUuidPolicy(UUID role, UUID authorizableEntity, BasePermission... permission){
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role);
		dto.setEvaluator(UuidEvaluator.class);
		dto.getEvaluatorProperties().put(UuidEvaluator.PARAMETER_UUID, authorizableEntity);
		dto.setPermissions(permission);
		return authorizationPolicyService.save(dto);
	}
	
	@Override
	public IdmIdentityRoleDto createIdentityRole(IdmIdentity identity, IdmRole role) {
		return createIdentityRole(identityContractService.getPrimeContract(identity.getId()), role);
	}
	
	@Override
	public IdmIdentityRoleDto createIdentityRole(IdmIdentityContractDto identityContract, IdmRole role) {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(identityContract.getId());
		identityRole.setRole(role.getId());
		return identityRoleService.save(identityRole);
	}

	@Override
	public IdmIdentityContractDto createIdentityContact(IdmIdentity identity) {
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setPosition("position-" + System.currentTimeMillis());
		return identityContractService.save(contract);
	}
	
	@Override
	public void deleteIdentityContact(UUID id) {
		identityContractService.deleteById(id);		
	}
	
	@Override
	public IdmContractGuaranteeDto createContractGuarantee(UUID identityContractId, UUID identityId) {
		return contractGuaranteeService.save(new IdmContractGuaranteeDto(identityContractId, identityId));
	}
}
