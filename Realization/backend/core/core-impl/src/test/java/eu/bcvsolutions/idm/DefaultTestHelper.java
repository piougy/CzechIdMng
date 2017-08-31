package eu.bcvsolutions.idm;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
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
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.UuidEvaluator;
import eu.bcvsolutions.idm.test.api.TestHelper;

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
	@Autowired private IdmRoleCatalogueService idmRoleCatalogueService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	
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
	public IdmRoleCatalogueDto createRoleCatalogue(){
		return createRoleCatalogue(null);
	}

	@Override
	public IdmRoleCatalogueDto createRoleCatalogue(String code){
		IdmRoleCatalogueDto roleCatalogue = new IdmRoleCatalogueDto();
		code = code == null ? createName() : code;
		roleCatalogue.setName(code);
		roleCatalogue.setCode(code);
		return idmRoleCatalogueService.save(roleCatalogue);
	}
	
	@Override
	public IdmTreeTypeDto createTreeType() {
		return createTreeType(null);
	}
	
	@Override
	public IdmTreeTypeDto createTreeType(String name) {
		IdmTreeTypeDto treeType = new IdmTreeTypeDto();
		name = name == null ? createName() : name;
		treeType.setCode(name);
		treeType.setName(name);
		return treeTypeService.save(treeType);
	}
	
	@Override
	public IdmTreeNodeDto createTreeNode() {
		return createTreeNode((String) null, null);
	}
	
	@Override
	public IdmTreeNodeDto createTreeNode(String name, IdmTreeNodeDto parent) {
		return createTreeNode(treeTypeService.getDefaultTreeType(), name, parent);
	}
	
	@Override
	public IdmTreeNodeDto createTreeNode(IdmTreeTypeDto treeType, IdmTreeNodeDto parent) {
		return createTreeNode(treeType, null, parent);
	}
	
	@Override
	public IdmTreeNodeDto createTreeNode(IdmTreeTypeDto treeType, String name, IdmTreeNodeDto parent) {
		Assert.notNull(treeType, "Tree type is required - test environment is wrong configured, test data is not prepared!");
		//
		name = name == null ? createName() : name;
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setParent(parent == null ? null : parent.getId());
		node.setCode(name);
		node.setName(name);
		node.setTreeType(treeType.getId());
		//
		return treeNodeService.save(node);
	}
	
	@Override
	public void deleteTreeNode(UUID id) {
		treeNodeService.deleteById(id);
	}

	@Override
	public IdmRoleDto createRole() {
		return createRole(null);
	}

	@Override
	public IdmRoleDto createRole(String name) {
		return createRole(null, name);
	}
	
	@Override
	public IdmRoleDto createRole(UUID id, String name) {
		IdmRoleDto role = new IdmRoleDto();
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
	public IdmRoleTreeNodeDto createRoleTreeNode(IdmRoleDto role, IdmTreeNodeDto treeNode, boolean skipLongRunningTask) {
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
		return createBasePolicy(role, null, null, permission);
	}
	
	@Override
	public IdmAuthorizationPolicyDto createBasePolicy(UUID role, GroupPermission groupPermission, Class<?> authorizableType, BasePermission... permission) {
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role);
		dto.setEvaluator(BasePermissionEvaluator.class);
		dto.setGroupPermission(groupPermission == null ? null : groupPermission.getName());
		dto.setAuthorizableType(authorizableType == null ? null : authorizableType.getCanonicalName());
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
	public IdmIdentityRoleDto createIdentityRole(IdmIdentityDto identity, IdmRoleDto role) {
		return createIdentityRole(getPrimeContract(identity.getId()), role);
	}
	
	@Override
	public IdmIdentityRoleDto createIdentityRole(IdmIdentityContractDto identityContract, IdmRoleDto role) {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(identityContract.getId());
		identityRole.setRole(role.getId());
		return identityRoleService.save(identityRole);
	}
	
	@Override
	public IdmIdentityContractDto getPrimeContract(UUID identityId) {
		return identityContractService.getPrimeContract(identityId);
	}

	@Override
	public IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity) {
		return createIdentityContact(identity, null);
	}
	
	@Override
	public IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity, IdmTreeNodeDto position) {
		return createIdentityContact(identity, position, null, null);
	}
	
	@Override
	public IdmIdentityContractDto createIdentityContact(IdmIdentityDto identity, IdmTreeNodeDto position, LocalDate validFrom, LocalDate validTill) {
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
	
	@Override
	public IdmRoleRequestDto assignRoles(IdmIdentityContractDto contract, IdmRoleDto... roles) {
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(contract.getIdentity());
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest.setExecuteImmediately(true);
		roleRequest = roleRequestService.save(roleRequest);
		//
		for (IdmRoleDto role : roles) {
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(roleRequest.getId());
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setValidFrom(contract.getValidFrom());
			conceptRoleRequest.setValidTill(contract.getValidTill());
			conceptRoleRequest.setRole(role.getId());
			//
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
			//
			conceptRoleRequestService.save(conceptRoleRequest);
		}
		//
		return roleRequestService.startRequest(roleRequest.getId(), false);		
	}
	
	private String createName() {
		return "test" + "-" + UUID.randomUUID();
	}

	@Override
	public IdmRoleCatalogueDto createRoleCatalogue(String code, UUID parentId) {
		IdmRoleCatalogueDto roleCatalogue = new IdmRoleCatalogueDto();
		code = code == null ? createName() : code;
		roleCatalogue.setName(code);
		roleCatalogue.setParent(parentId);
		roleCatalogue.setCode(code);
		return idmRoleCatalogueService.save(roleCatalogue);
	}
}
