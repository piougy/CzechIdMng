package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
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
		this.testNode(entity);
		return super.save(entity);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmTreeNode> findRoots(Long treeType) {
		return this.treeNodeRepository.findRoots(treeType);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmTreeNode> findChildrenByParent(Long parent) {
		return this.treeNodeRepository.findChildrenByParent(parent);
	}
	
	private void testNode(IdmTreeNode node) {
		if (checkParents(node)) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_PARRENT,  "TreeNode ["+node.getName() +"] have bad parent.");
		}

		if (checkEmptyParent(node)) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_PARRENT,  "TreeNode ["+node.getName() +"] have bad parent.");
		}
		
		if (checkChildren(node)) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_PARRENT,  "TreeNode ["+node.getName() +"] have bad parent.");
		}
		
		if (checkCorrectType(node)) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_PARRENT,  "TreeNode ["+node.getName() +"] have bad type.");
		}
	}
	
	private boolean checkEmptyParent(IdmTreeNode treeNode) {
		boolean isNewRoot =  this.treeNodeRepository.findRoots(treeNode.getTreeType().getId()).isEmpty();
		
		if (isNewRoot) {
			return false;
		}
		
		List<?> root = this.treeNodeRepository.findChildrenByParent(null);
		
		if (treeNode.getParent() == null && root.isEmpty() || treeNode.getParent() != null) {
			return false;
		}
		return true;
	}

	/**
	 * Method check type of current node and saved node.
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
	
	/**
	 * Method check if parent of node isnt her children.
	 * @param treeNode
	 * @return 
	 */
	private boolean checkChildren(IdmTreeNode treeNode) {
		IdmTreeNode tmp = treeNode;
		List<Long> listIds = new ArrayList<Long>(); 
		while (tmp.getParent() != null) {
			if	(listIds.contains(tmp.getId())) {
				return true;
			}
			listIds.add(tmp.getId());
			tmp = tmp.getParent();
		}
		return false;
	}
	
	/**
	 * Method check if tree node have same id as parent.id
	 * @param treeNode
	 * @return true if parent.id and id is same
	 */
	private boolean checkParents(IdmTreeNode treeNode) {
		return treeNode.getParent() != null && (treeNode.getId() == treeNode.getParent().getId());
	}
}
