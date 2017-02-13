package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.exception.TreeTypeException;
import eu.bcvsolutions.idm.core.model.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;

@Service
public class DefaultIdmTreeNodeService extends AbstractReadWriteEntityService<IdmTreeNode, TreeNodeFilter> implements IdmTreeNodeService {
	
	private final IdmTreeNodeRepository treeNodeRepository;
	private final IdmTreeTypeRepository treeTypeRepository;
	private final IdmIdentityContractRepository identityContractRepository;
	private final DefaultBaseTreeService<IdmTreeNode> baseTreeService;

	@Autowired
	public DefaultIdmTreeNodeService(
			IdmTreeNodeRepository treeNodeRepository,
			IdmTreeTypeRepository treeTypeRepository,
			IdmIdentityContractRepository identityContractRepository,
			DefaultBaseTreeService<IdmTreeNode> baseTreeService) {
		super(treeNodeRepository);
		//
		Assert.notNull(treeTypeRepository);
		Assert.notNull(identityContractRepository);
		Assert.notNull(baseTreeService);
		//
		this.treeNodeRepository = treeNodeRepository;
		this.treeTypeRepository = treeTypeRepository;
		this.identityContractRepository = identityContractRepository;
		this.baseTreeService = baseTreeService;
	}
	
	@Override
	@Transactional
	public void delete(IdmTreeNode treeNode) {
		Assert.notNull(treeNode);
		//
		Page<IdmTreeNode> nodes = treeNodeRepository.findChildren(null, treeNode.getId(), new PageRequest(0, 1));
		if (nodes.getTotalElements() > 0) {
			throw new TreeTypeException(CoreResultCode.TREE_NODE_DELETE_FAILED_HAS_CHILDREN,  ImmutableMap.of("treeNode", treeNode.getName()));
		}		
		if (this.identityContractRepository.countByWorkingPosition(treeNode) > 0) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_DELETE_FAILED_HAS_CONTRACTS,  ImmutableMap.of("treeNode", treeNode.getName()));
		}
		// clear default tree nodes from type
		treeTypeRepository.clearDefaultTreeNode(treeNode);
		//
		super.delete(treeNode);
	}
	
	@Override
	@Transactional
	public IdmTreeNode save(IdmTreeNode entity) {
		this.validate(entity);
		return super.save(entity);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNode> findRoots(UUID treeTypeId, Pageable pageable) {
		return this.treeNodeRepository.findChildren(treeTypeId, null, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNode> findChildrenByParent(UUID parentId, Pageable pageable) {
		return this.treeNodeRepository.findChildren(null, parentId, pageable);
	}
	
	private void validate(IdmTreeNode node) {		
		if (this.baseTreeService.validateTreeNodeParents(node)){
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_PARENT,  "TreeNode ["+node.getName() +"] have bad parent.");
		}
		
		if (checkCorrectType(node)) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_TYPE,  "TreeNode ["+node.getName() +"] have bad type.");
		}
	}

	/**
	 * Method check type of current node and saved node.
	 * TODO: bug - this will work only in update. If node is created, then parent from dif	erent type could be given
	 * 
	 * @param treeNode
	 * @return bool. True - if current and saved node is not same, false - if everything ist OK. When is node new return false;
	 */
	private boolean checkCorrectType(IdmTreeNode treeNode) {
		if (treeNode.getId() == null) {
			return false;
		}
		
		IdmTreeNode currentNode = treeNodeRepository.findOne(treeNode.getId());
		
		if (currentNode != null) {
			return !currentNode.getTreeType().equals(treeNode.getTreeType());
		} else {
			return false;
		}
	}
}
