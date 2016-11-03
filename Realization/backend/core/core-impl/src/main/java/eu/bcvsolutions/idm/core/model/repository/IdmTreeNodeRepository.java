package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.dto.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmTreeNodeExcerpt;

@RepositoryRestResource(
	collectionResourceRel = "treenodes",
	path = "tree/nodes",
	itemResourceRel = "treenode",
	collectionResourceDescription = @Description("Tree nodes"),
	itemResourceDescription = @Description("Tree nodes"),
	excerptProjection = IdmTreeNodeExcerpt.class,
	exported = false
)
public interface IdmTreeNodeRepository extends BaseRepository<IdmTreeNode, TreeNodeFilter> {
	
	@Override
	@Query(value = "select e from IdmTreeNode e" +
	        " where" +
	        "(?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})" + 
	        "and (?#{[0].treeType} is null or e.treeType.id = ?#{[0].treeType})" +
	        " and (?#{[0].treeNode} is null or e.parent.id = ?#{[0].treeNode})")
	Page<IdmTreeNode> find(TreeNodeFilter filter, Pageable pageable);

	@Query(value = "select e from IdmTreeNode e" +
			" where" +
			" (:parentId is null and e.parent.id IS NULL) or (e.parent.id = :parentId)")
	Page<IdmTreeNode> findChildrenByParent(@Param(value = "parent") UUID parentId, Pageable pageable);
	
	/**
	 * Query return all roots (find by parent = null)
	 * or find one root by treeType
	 * @param treeType
	 * @return List of roots
	 */
	@Query(value = "select e from IdmTreeNode e" +
			" where" +
			" (e.parent.id IS NULL)" +
			" and (:treeTypeId is null or e.treeType.id = :treeTypeId)")
	Page<IdmTreeNode> findRoots(@Param(value = "treeTypeId") UUID treeTypeId, Pageable pageable);
}
