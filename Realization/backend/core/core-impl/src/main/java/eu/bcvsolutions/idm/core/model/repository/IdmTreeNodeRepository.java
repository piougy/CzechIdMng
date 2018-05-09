package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.forest.index.repository.TypeableForestContentRepository;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.repository.ExternalIdentifiableRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Tree structures nodes
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmTreeNodeRepository extends 
		AbstractEntityRepository<IdmTreeNode>, 
		TypeableForestContentRepository<IdmTreeNode, UUID>,
		ExternalIdentifiableRepository<IdmTreeNode, UUID> {
	
	/**
	 * Nodes by tree type.
	 * 
	 * @param treeTypeId
	 * @return
	 */
	Page<IdmTreeNode> findByTreeType_Id(UUID treeTypeId, Pageable pageable);

	/**
	 * If parent is not given, then roots of given tree type is returned. Returns direct children only.
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
	 * Finds root (indexed tree can have only one root)
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
