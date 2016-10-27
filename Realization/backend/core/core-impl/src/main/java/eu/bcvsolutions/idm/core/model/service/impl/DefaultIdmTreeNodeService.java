package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.dto.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.IdmTreeNodeService;

@Service
public class DefaultIdmTreeNodeService extends AbstractReadWriteEntityService<IdmTreeNode, TreeNodeFilter> implements IdmTreeNodeService {
	
	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;
	
	@Autowired
	private IdmIdentityContractRepository identityContractRepository;
	
	@Autowired
	private DefaultBaseTreeService<IdmTreeNode> baseTreeSevice;

	@Override
	protected BaseRepository<IdmTreeNode, TreeNodeFilter> getRepository() {
		return this.treeNodeRepository;
	}
	
	@Override
	public void delete(IdmTreeNode entity) {
		List<IdmIdentityContract> listWorkingPosition = this.identityContractRepository.findAllByTreeNode(entity);
		
		if (!listWorkingPosition.isEmpty()) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_CANNOT_DELETE,  ImmutableMap.of("node", entity.getName()));
		}
		
		super.delete(entity);
	}
	
	@Override
	public IdmTreeNode save(IdmTreeNode entity) {
		this.validate(entity);
		return super.save(entity);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNode> findRoots(Long treeType, Pageable pageable) {
		return this.treeNodeRepository.findRoots(treeType, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNode> findChildrenByParent(Long parent, Pageable pageable) {
		return this.treeNodeRepository.findChildrenByParent(parent, pageable);
	}
	
	private void validate(IdmTreeNode node) {		
		if (this.baseTreeSevice.validateTreeNodeParents(node)){
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
