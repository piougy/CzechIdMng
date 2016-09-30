package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmTreeNodeExcerpt;

@RepositoryRestResource(
	collectionResourceRel = "treenodes",
	path = "treenodes",
	itemResourceRel = "treenode",
	collectionResourceDescription = @Description("Tree nodes"),
	itemResourceDescription = @Description("Tree nodes"),
	excerptProjection = IdmTreeNodeExcerpt.class,
	exported = false
)
public interface IdmTreeNodeRepository extends BaseRepository<IdmTreeNode> {
	
	@Query(value = "select e from IdmTreeNode e" +
	        " where" +
	        "(:text is null or lower(e.name) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')})" + 
	        "and (:treeType is null or e.treeType.id = :treeType)" +
	        " and (:parent is null or e.parent.id = :parent)")
	@RestResource(path = "quick", rel = "quick")
	Page<IdmTreeNode> findQuick(@Param(value = "text") String text, @Param(value = "parent") Long parentId, 
			@Param(value = "treeType") Long treeTypeId, Pageable pageable);

	@Query(value = "select e from IdmTreeNode e" +
			" where" +
			" (:parent is null and e.parent.id IS NULL) or (e.parent.id = :parent)")
	@RestResource(path = "children", rel = "children")
	List<IdmTreeNode> findChildrenByParent(@Param(value = "parent") Long parent);
	
	/**
	 * Query return all roots (find by parent = null)
	 * or find one root by treeType
	 * @param treeType
	 * @return List of roots
	 */
	@Query(value = "select e from IdmTreeNode e" +
			" where" +
			" (e.parent.id IS NULL)" +
			" and (:treeType is null or e.treeType.id = :treeType)")
	@RestResource(path = "roots", rel = "roots")
	List<IdmTreeNode> findRoots(@Param(value = "treeType") Long treeType);
}
