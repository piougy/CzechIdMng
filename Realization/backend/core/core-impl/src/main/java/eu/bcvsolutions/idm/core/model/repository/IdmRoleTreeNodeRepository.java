package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Repository for automatic roles
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleTreeNodeRepository extends AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter>{
	
	/**
	 * @deprecated use IdmRoleTreeNodeService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmRoleTreeNode> find(RoleTreeNodeFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmRoleTreeNodeService (uses criteria api)");
	}
	
	/**
	 * Returns all automatic roles for given tree node by recursion
	 * 
	 * @param treeNode
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e join e.treeNode n"
			+ " where"
			+ " (n.treeType = ?#{[0].treeType})" // more tree types
			+ " and"
			+ " ("
				+ " (n = ?#{[0]})" // takes all recursion
				+ " or"
				+ " (e.recursionType = 'DOWN' and ?#{[0].lft} between n.forestIndex.lft and n.forestIndex.rgt)"
				+ " or"
				+ " (e.recursionType = 'UP' and n.forestIndex.lft between ?#{[0].lft} and ?#{[0].rgt})"
			+ " )")
	List<IdmRoleTreeNode> findAutomaticRoles(IdmTreeNode treeNode);
}
