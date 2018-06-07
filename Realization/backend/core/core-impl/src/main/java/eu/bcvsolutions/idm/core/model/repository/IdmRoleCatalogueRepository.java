package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.forest.index.repository.BaseForestContentRepository;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

/**
 * Role catalogue repository
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public interface IdmRoleCatalogueRepository extends 
		AbstractEntityRepository<IdmRoleCatalogue>, 
		BaseForestContentRepository<IdmRoleCatalogue, UUID> {
	
	IdmRoleCatalogue findOneByCode(@Param("code") String code);
	
	/**
	 * Find direct children
	 * 
	 * @param parentId
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
			" where" +
			" (:parentId is null and e.parent.id IS NULL) or (e.parent.id = :parentId)")
	Page<IdmRoleCatalogue> findChildren(@Param(value = "parentId") UUID parentId, Pageable pageable);
	
	/**
	 * Finds roots 
	 * 
	 * @param treeTypeCode
	 * @param pageable
	 * @return
	 */
	@Query("select e from #{#entityName} e where e.parent is null")
	Page<IdmRoleCatalogue> findRoots(Pageable pageable);
	
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
	@Query("select e from #{#entityName} e join e.forestIndex i where i.forestTreeType = '" + IdmRoleCatalogue.FOREST_TREE_TYPE + "'"
			+ " and i.lft BETWEEN ?#{[0].lft + 1} and ?#{[0].rgt - 1}")
	Page<IdmRoleCatalogue> findAllChildren(IdmRoleCatalogue parentContent, Pageable pageable);
	
	/**
	 * {@inheritDoc}
	 * 
	 * Adds fix for possible null pointers.
	 * 
	 * @param content
	 * @return
	 */
	@Override
	@Query("select e from #{#entityName} e join e.forestIndex i where i.forestTreeType = '" + IdmRoleCatalogue.FOREST_TREE_TYPE + "' "
			+ " and i.lft < ?#{[0].lft} and i.rgt > ?#{[0].rgt}")
	List<IdmRoleCatalogue> findAllParents(IdmRoleCatalogue content, Sort sort);
}
