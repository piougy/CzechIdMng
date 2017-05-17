package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.forest.index.repository.TypeableForestContentRepository;
import eu.bcvsolutions.idm.core.api.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.rest.projection.IdmTreeNodeExcerpt;

/**
 * Tree structures nodes
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RepositoryRestResource(
	collectionResourceRel = "treeNodes",
	path = "tree-nodes",
	itemResourceRel = "treeNode",
	collectionResourceDescription = @Description("Tree nodes"),
	itemResourceDescription = @Description("Tree nodes"),
	excerptProjection = IdmTreeNodeExcerpt.class,
	exported = false
)
public interface IdmTreeNodeRepository extends AbstractEntityRepository<IdmTreeNode, TreeNodeFilter>, TypeableForestContentRepository<IdmTreeNode, UUID> {

	// Obsolete - IdmTreeNodeService.find now uses Criteria
	@Deprecated
	@Override
	@Query(value = "select e from IdmTreeNode e left join e.forestIndex fi"
	        + " where"
			// name and code
	        + " ("
	        	+ " ?#{[0].text} is null"
	        	+ " or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        	+ " or lower(e.code) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
        	+ " )"
        	+ " and (?#{[0].treeTypeId} is null or e.treeType.id = ?#{[0].treeTypeId})"
         		// on selected tree node recursively - given node is included true
	        + " and (?#{[0].treeNode} is null or ?#{[0].recursively == true ? 'true' : 'false'} = 'false' " +
			"or fi.lft BETWEEN ?#{[0].treeNode == null ? null : [0].treeNode.lft + 1} and ?#{[0].treeNode == null ? null : [0].treeNode.rgt - 1})"
	        	// on selected tree node
        	+ " and (?#{[0].treeNode} is null or ?#{[0].recursively == false ? 'true' : 'false'} = 'false' or e.parent = ?#{[0].treeNode})"
	        + " and (?#{[0].defaultTreeType} is null or e.treeType = (select tt from IdmTreeType tt where tt.defaultTreeType = ?#{[0].defaultTreeType}))"
	        + " and "
	  	    + " ("
	  	    	+ " ?#{[0].property} is null "
	  	    	+ " or (?#{[0].property} = 'name' and e.name = ?#{[0].value})"
				+ " or (?#{[0].property} = 'code' and e.code = ?#{[0].value})"
				+ " or (?#{[0].property} = 'externalId' and e.externalId = ?#{[0].value})"
	        + " )")
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
	
	
	/**
	 * Finds roots 
	 * 
	 * @param treeTypeCode
	 * @param pageable
	 * @return
	 */
	@Query("select e from #{#entityName} e where e.parent is null and e.treeType.id = :treeTypeId")
	Page<IdmTreeNode> findRoots(@Param("treeTypeId") UUID treeTypeId, Pageable pageable);
	
	/**
	 * Finds root (indexed tree can have onlz one root)
	 * 
	 * @param forestTreeType
	 * @return
	 */
	@Override
	@Query("select e from #{#entityName} e where e.parent is null and e.treeType.id = ?#{T(eu.bcvsolutions.idm.core.model.entity.IdmTreeNode).toTreeTypeId([0])}")
	Page<IdmTreeNode> findRoots(String forestTreeType, Pageable pageable);
	
	
	/**
	 * {@inheritDoc}
	 * 
	 * Adds fix for possible null pointers.
	 * 
	 * @param parentContent
	 * @param pageable
	 * @return
	 */
	@Override
	@Query("select e from #{#entityName} e join e.forestIndex i where i.forestTreeType = ?#{[0].forestTreeType} and i.lft BETWEEN ?#{[0].lft + 1} and ?#{[0].rgt - 1}")
	Page<IdmTreeNode> findAllChildren(IdmTreeNode parentContent, Pageable pageable);
	
	/**
	 * {@inheritDoc}
	 * 
	 * Adds fix for possible null pointers.
	 * 
	 * @param content
	 * @return
	 */
	@Override
	@Query("select e from #{#entityName} e join e.forestIndex i where i.forestTreeType = ?#{[0].forestTreeType} and i.lft < ?#{[0].lft} and i.rgt > ?#{[0].rgt}")
	List<IdmTreeNode> findAllParents(IdmTreeNode content, Sort sort);
}
