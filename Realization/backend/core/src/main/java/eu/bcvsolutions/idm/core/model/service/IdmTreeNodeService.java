package eu.bcvsolutions.idm.core.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
	 * @return List of roots
	 */
	List<IdmTreeNode> findRoots(Long treeType);
	
	/**
	 * Method return children by parent id
	 * @param parent id
	 * @return List of children
	 */
	List<IdmTreeNode> findChildrenByParent(Long parent);
}
