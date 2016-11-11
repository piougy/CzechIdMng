package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmTreeNodeExcerpt;

/**
 * Tree strusture nodes
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RepositoryRestResource(
	collectionResourceRel = "treenodes",
	path = "treeNodes",
	itemResourceRel = "treenode",
	collectionResourceDescription = @Description("Tree nodes"),
	itemResourceDescription = @Description("Tree nodes"),
	excerptProjection = IdmTreeNodeExcerpt.class,
	exported = false
)
public interface IdmTreeNodeRepository extends AbstractEntityRepository<IdmTreeNode, TreeNodeFilter> {
	
	@Override
	@Query(value = "select e from IdmTreeNode e"
	        + " where"
			// name and code
	        + " ("
	        	+ " ?#{[0].text} is null"
	        	+ " or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        	+ " or lower(e.code) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
        	+ " )"
	        + " and (?#{[0].treeTypeId} is null or e.treeType.id = ?#{[0].treeTypeId})"
	        + " and (?#{[0].treeNodeId} is null or e.parent.id = ?#{[0].treeNodeId})")
	Page<IdmTreeNode> find(TreeNodeFilter filter, Pageable pageable);

	/**
	 * If parent is not given, then roots of given tree type is returned
	 * 
	 * @param treeTypeId
	 * @param parentId
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
			" where"
			+ " ((:parentId is null and e.parent.id is null) or (e.parent.id = :parentId))"
			+ " and"
			+ " (:treeTypeId is null or e.treeType.id = :treeTypeId)")
	Page<IdmTreeNode> findChildren(@Param(value = "treeTypeId") UUID treeTypeId, @Param(value = "parentId") UUID parentId, Pageable pageable);
}
