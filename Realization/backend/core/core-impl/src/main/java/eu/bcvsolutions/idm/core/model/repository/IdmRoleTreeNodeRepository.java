package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;

/**
 * Repository for automatic roles
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "roleTreeNodes", // 
		path = "role-tree-nodes", //
		itemResourceRel = "roleTreeNode", //
		exported = false)
public interface IdmRoleTreeNodeRepository extends AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter>{
	
	@Override
	@Query(value = "select e from #{#entityName} e" +
	        " where"
	        + " ( "
	        	+ "?#{[0].roleId} is null"
	        	+ " or"
	        	+ " e.role.id = ?#{[0].roleId}"
	        + " ) "
	        + " and"
	        + "	("
	        	+ "?#{[0].treeNodeId} is null"
	        	+ " or"
	        	+ " e.treeNode.id = ?#{[0].treeNodeId}"
        	+ " )")
	Page<IdmRoleTreeNode> find(RoleTreeNodeFilter filter, Pageable pageable);
}
