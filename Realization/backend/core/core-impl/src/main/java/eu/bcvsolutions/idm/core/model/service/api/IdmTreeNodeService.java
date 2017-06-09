package eu.bcvsolutions.idm.core.model.service.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Operations with IdmTreeNode
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Service
public interface IdmTreeNodeService extends ReadWriteEntityService<IdmTreeNode, TreeNodeFilter>, ScriptEnabled {
	
	/**
	 * Method return all roots - @param treeType = null, or one root for treeType.
	 * @param treeType Long
	 * @param pageable
	 * @return Page of roots
	 */
	Page<IdmTreeNode> findRoots(UUID treeTypeId, Pageable pageable);
	
	/**
	 * Method return children by parent id
	 * 
	 * @param parent
	 * @return Page of children
	 */
	Page<IdmTreeNode> findChildrenByParent(UUID parentId, Pageable pageable);
	
	
	/**
	 * Rebuild (drop and create) all indexes for given treeType.
	 * 
	 * @param treeType
	 * @return long running task id
	 */
	UUID rebuildIndexes(IdmTreeType treeType);
}
