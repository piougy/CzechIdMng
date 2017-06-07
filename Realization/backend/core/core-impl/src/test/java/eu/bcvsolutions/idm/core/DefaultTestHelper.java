package eu.bcvsolutions.idm.core;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
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
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
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
	
	@Autowired private IdmTreeNodeService treeNodeService;
	@Autowired private IdmTreeTypeService treeTypeService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	
	@Override
	public IdmIdentityDto createIdentity() {
		return createIdentity(null);
	}
	
	@Override
	public IdmIdentityDto createIdentity(String name) {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(name == null ? createName() : name);
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity.setPassword(new GuardedString("password"));
		return identityService.save(identity);
	}
	
	@Override
	public void deleteIdentity(UUID id) {
		identityService.deleteById(id);
	}
	
	@Override
	public IdmTreeType createTreeType() {
		return createTreeType(null);
	}
	
	@Override
	public IdmTreeType createTreeType(String name) {
		IdmTreeType treeType = new IdmTreeType();
		name = name == null ? createName() : name;
		treeType.setCode(name);
		treeType.setName(name);
		return treeTypeService.save(treeType);
	}
	
	@Override
	public IdmTreeNode createTreeNode() {
		return createTreeNode((String) null, null);
	}
	
	@Override
	public IdmTreeNode createTreeNode(String name, IdmTreeNode parent) {
		return createTreeNode(treeTypeService.getDefaultTreeType(), name, parent);
	}
	
	@Override
	public IdmTreeNode createTreeNode(IdmTreeType treeType, IdmTreeNode parent) {
		return createTreeNode(treeType, null, parent);
	}
	
	@Override
	public IdmTreeNode createTreeNode(IdmTreeType treeType, String name, IdmTreeNode parent) {
		Assert.notNull(treeType, "Tree type is required - test environment is wrong configured, test data is not prepared!");
		//
		name = name == null ? createName() : name;
		IdmTreeNode node = new IdmTreeNode();
		node.setParent(parent);
		node.setCode(name);
		node.setName(name);
		node.setTreeType(treeType);
		return treeNodeService.save(node);
	}
	
	@Override
	public void deleteTreeNode(UUID id) {
		treeNodeService.deleteById(id);
	}

	@Override
	public IdmRole createRole() {
		return createRole(null);
	}

	@Override
	public IdmRole createRole(String name) {
		return createRole(null, name);
	}
	
	@Override
	public IdmRole createRole(UUID id, String name) {
		IdmRole role = new IdmRole();
		if (id != null) {
			role.setId(id);
		}
		role.setName(name == null ? createName() : name);
		return roleService.save(role);
	}
	
	@Override
	public void deleteRole(UUID id) {
		roleService.deleteById(id);
	}
	
	@Override
	public IdmRoleTreeNodeDto createRoleTreeNode(IdmRole role, IdmTreeNode treeNode, boolean skipLongRunningTask) {
		IdmRoleTreeNodeDto roleTreeNode = new IdmRoleTreeNodeDto();
		roleTreeNode.setRole(role.getId());
		roleTreeNode.setTreeNode(treeNode.getId());
		if (skipLongRunningTask) {
			return roleTreeNodeService.saveInternal(roleTreeNode);
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
	public IdmIdentityRoleDto createIdentityRole(IdmIdentityDto identity, IdmRole role) {
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
	public IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity) {
		return createIdentityContact(identity, null);
	}
	
	@Override
	public IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity, IdmTreeNode position) {
		return createIdentityContact(identity, position, null, null);
	}
	
	@Override
	public IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity, IdmTreeNode position, LocalDate validFrom, LocalDate validTill) {
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setPosition(createName());
		contract.setWorkPosition(position == null ? null : position.getId());
		contract.setValidFrom(validFrom);
		contract.setValidTill(validTill);
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
	
	private String createName() {
		return "test" + "-" + UUID.randomUUID();
	}
}
