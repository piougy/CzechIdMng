package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.exception.TreeTypeException;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
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
	private IdmIdentityContractRepository workingPositionRepository;
	
	@Override
	protected BaseRepository<IdmTreeType, QuickFilter> getRepository() {
		return this.treeTypeRepository;
	}
	
	@Override
	public void delete(IdmTreeType entity) {
		List<IdmIdentityContract> listWorkingPositions = workingPositionRepository.findAllByTreeType(entity);
		Page<IdmTreeNode> nodes = treeNodeRepository.findChildren(entity.getId(), null, new PageRequest(0, 2));
		
		if (!listWorkingPositions.isEmpty() || nodes.getTotalElements() != 0) {
			throw new TreeTypeException(CoreResultCode.TREE_TYPE_CANNOT_DELETE,  ImmutableMap.of("node", entity.getName()));
		}
		
		super.delete(entity);
	}

}
