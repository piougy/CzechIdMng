package eu.bcvsolutions.idm.core.model.service.api;

import java.util.UUID;

import eu.bcvsolutions.forest.index.service.api.ForestContentService;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Index and search tree nodes by forest index
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmTreeNodeForestContentService extends ForestContentService<IdmTreeNode, IdmForestIndexEntity, UUID> {

}
