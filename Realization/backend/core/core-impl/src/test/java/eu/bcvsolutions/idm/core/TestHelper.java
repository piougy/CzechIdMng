package eu.bcvsolutions.idm.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Creates common test entities
 * 
 * TODO: interface and move to api
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class TestHelper {
	
	@Autowired
	private IdmTreeNodeService treeNodeService;
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityService identityService;

	public IdmIdentity createIdentity(String name) {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername(name + "_" + System.currentTimeMillis());
		identity.setPassword(new GuardedString("heslo"));
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		return identity;
	}
	
	public IdmTreeType createTreeType(String name) {
		IdmTreeType treeType = new IdmTreeType();
		treeType.setCode(name + "-" + System.currentTimeMillis());
		treeType.setName(name);
		return treeTypeService.save(treeType);
	}
	
	public IdmTreeNode createTreeNode(IdmTreeType treeType, String name, IdmTreeNode parent) {
		IdmTreeNode node = new IdmTreeNode();
		node.setParent(parent);
		node.setCode(name + "-" + System.currentTimeMillis());
		node.setName(name);
		node.setTreeType(treeType);
		return treeNodeService.save(node);
	}
	
	public IdmRole createRole(String name) {
		IdmRole roleC = new IdmRole();
		roleC.setName(name + "-" + System.currentTimeMillis());
		return roleService.save(roleC);
	}
	
}
