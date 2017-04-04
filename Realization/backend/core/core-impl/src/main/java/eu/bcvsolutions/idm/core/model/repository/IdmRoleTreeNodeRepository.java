package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Repository for automatic roles
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleTreeNodeRepository extends AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter>{
	
	@Override
	@Query(value = "select e from #{#entityName} e" +
	        " where"
	        + " ("
	        	+ " ?#{[0].roleId} is null"
	        	+ " or"
	        	+ " e.role.id = ?#{[0].roleId}"
	        + " ) "
	        + " and"
	        + "	("
	        	+ " ?#{[0].treeNodeId} is null"
	        	+ " or"
	        	+ " e.treeNode.id = ?#{[0].treeNodeId}"
        	+ " )")
	Page<IdmRoleTreeNode> find(RoleTreeNodeFilter filter, Pageable pageable);
	
	/**
	 * Returns all automatic roles for given tree node by recursion
	 * 
	 * @param treeNode
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e"
			+ " where"
			+ " (e.treeNode = ?#{[0]})" // takes all recursion
			+ " or"
			+ " (e.recursionType = 'DOWN' and ?#{[0].lft} between e.treeNode.forestIndex.lft and e.treeNode.forestIndex.rgt)"
			+ " or"
			+ " (e.recursionType = 'UP' and e.treeNode.forestIndex.lft between ?#{[0].lft} and ?#{[0].rgt})")
	List<IdmRoleTreeNode> findAutomaticRoles(IdmTreeNode treeNode);
}
