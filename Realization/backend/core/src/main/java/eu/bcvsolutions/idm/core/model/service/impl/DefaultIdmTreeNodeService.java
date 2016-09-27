package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.IdmTreeNodeService;

@Service
public class DefaultIdmTreeNodeService extends AbstractReadWriteEntityService<IdmTreeNode, QuickFilter> implements IdmTreeNodeService {
	
	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;
	
	@Autowired
	private IdmIdentityWorkingPositionRepository workingPositionRepository;

	@Override
	protected BaseRepository<IdmTreeNode> getRepository() {
		return this.treeNodeRepository;
	}
	
	@Override
	public IdmTreeNode save(IdmTreeNode entity) {
		// this.workingPositionRepository.findAllByTreeNode(entity);
		return super.save(entity);
	}

}
