package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeTypeFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Repository for tree types
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public interface IdmTreeTypeRepository extends AbstractEntityRepository<IdmTreeType, IdmTreeTypeFilter> {
	
	IdmTreeType findOneByCode(@Param("code") String code);
	
	@Override
	@Query(value = "select e from #{#entityName} e" +
	        " where"
	        + " ("
		        + " ?#{[0].text} is null"
		        + " or lower(e.code) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
		        + " or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        + " )")
	Page<IdmTreeType> find(IdmTreeTypeFilter filter, Pageable pageable);
	
	/**
	 * Returns default tree type
	 * 
	 * @return
	 */
	IdmTreeType findOneByDefaultTreeTypeIsTrue();
	
	/**
	 * Clears default tree type for all tree types instead given updatedEntityId
	 * 
	 * @param updatedEntityId
	 */
	@Modifying
	@Query("update #{#entityName} e set e.defaultTreeType = false where (:updatedEntityId is null or e.id != :updatedEntityId)")
	@Deprecated
	void clearDefaultTreeType(@Param("updatedEntityId") UUID updatedEntityId);
	
	/**
	 * Clear default tree node, when node is deleted etc.
	 * 
	 * @param defaultTreeNode
	 * @return
	 */
	@Modifying
	@Query("update #{#entityName} e set e.defaultTreeNode = null where e.defaultTreeNode.id = :defaultTreeNodeId")
	@Deprecated
	int clearDefaultTreeNode(@Param("defaultTreeNodeId") UUID defaultTreeNodeId);
}
