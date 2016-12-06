package eu.bcvsolutions.idm.core.model.service.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Operations with IdmTreeNode
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public interface IdmTreeNodeService extends ReadWriteEntityService<IdmTreeNode, TreeNodeFilter> {
	
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
}
