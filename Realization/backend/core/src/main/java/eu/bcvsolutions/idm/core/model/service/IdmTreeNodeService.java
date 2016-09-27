package eu.bcvsolutions.idm.core.model.service;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Operations with IdmTreeNode
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public interface IdmTreeNodeService extends ReadWriteEntityService<IdmTreeNode, QuickFilter> {

}
