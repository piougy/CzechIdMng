package eu.bcvsolutions.idm.core.model.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Trimmed treeNode - projection is used in collections (search etc.)
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Projection(name = "excerpt", types = IdmTreeNode.class)
public interface IdmTreeNodeExcerpt extends AbstractDtoProjection {
	
	String getName();
	
	boolean isDisabled();
	
	IdmTreeNode getParent();
	
	IdmTreeType getTreeType();
}
