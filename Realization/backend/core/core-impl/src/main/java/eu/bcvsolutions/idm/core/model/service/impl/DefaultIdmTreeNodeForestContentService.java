package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.forest.index.service.impl.AbstractForestContentService;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeForestContentService;

/**
 * Index and search tree nodes by forest index
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmTreeNodeForestContentService 
		extends AbstractForestContentService<IdmTreeNode, IdmForestIndexEntity, UUID>
		implements IdmTreeNodeForestContentService {
	
	@Autowired
	public DefaultIdmTreeNodeForestContentService(
			ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService,
			IdmTreeNodeRepository repository) {
		super(forestIndexService, repository);
	}
	
	@Override
	public void rebuildIndexes(String forestTreeType) {
		throw new UnsupportedOperationException("Use TreeNodeService.rebuildIndexes instead - added long running task support");
	}
}
