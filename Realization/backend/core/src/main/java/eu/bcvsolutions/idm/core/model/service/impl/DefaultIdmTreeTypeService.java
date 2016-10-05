package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.DefaultTreeTypeErrorModel;
import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.service.IdmTreeTypeService;

@Service
public class DefaultIdmTreeTypeService extends AbstractReadWriteEntityService<IdmTreeType, QuickFilter> implements IdmTreeTypeService{
	
	@Autowired
	private IdmTreeTypeRepository treeTypeRepository;
	
	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;
	
	@Autowired
	private IdmIdentityWorkingPositionRepository workingPositionRepository;
	
	@Override
	protected BaseRepository<IdmTreeType, QuickFilter> getRepository() {
		return this.treeTypeRepository;
	}
	
	@Override
	public void delete(IdmTreeType entity) {
		List<IdmIdentityWorkingPosition> listWorkingPositions = workingPositionRepository.findAllByTreeType(entity);
		List<IdmTreeNode> listNodes = treeNodeRepository.findChildrenByParent(entity.getId());
		
		if (!listWorkingPositions.isEmpty() || !listNodes.isEmpty()) {
			throw new DefaultTreeTypeErrorModel(CoreResultCode.TREE_TYPE_CANNOT_DELETE,  ImmutableMap.of("node", entity.getName()));
		}
		
		super.delete(entity);
	}

}
