package eu.bcvsolutions.idm.core.model.repository.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;

/**
 * Securing organization, adds validations (TODO move to validator - e.g. {@link eu.bcvsolutions.idm.core.model.validator.IdmRoleValidator)
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
@Component
@RepositoryEventHandler(IdmTreeNode.class)
public class IdmTreeNodeEventHandler {
	
	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmRoleEventHandler.class);
	
	@HandleBeforeSave
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREENODE_WRITE + "')")
	public void handleBeforeSave(IdmTreeNode node) {
		
		testNode(node);
		
		log.debug("1 Node [{}] will be saved", node);
	}
	
	@HandleBeforeCreate
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREENODE_WRITE + "')")
	public void handleBeforeCreate(IdmTreeNode node) {
		
		testNode(node);
		
		log.debug("1 Node [{}] will be created", node);
	}
	
	@HandleBeforeDelete
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREENODE_DELETE + "')")
	public void handleBeforeDelete(IdmTreeNode treeNode) {	
		// nothing, just security
	}
	
	private void testNode(IdmTreeNode node) {
		if (checkParents(node)) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,  "TreeNode ["+node.getName() +"] have bad parent.");
		}

		if (checkEmptyParent(node)) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,  "TreeNode ["+node.getName() +"] have bad parent.");
		}
		
		if (checkChildren(node)) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,  "TreeNode ["+node.getName() +"] have bad parent.");
		}
		
		if (checkCorrectType(node)) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,  "TreeNode ["+node.getName() +"] have bad type.");
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
	 * Method check if parent of organization isnt her children.
	 * @param organization
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
	 * Method check if organization have same id as parent.id
	 * @param organization
	 * @return true if parent.id and id is same
	 */
	private boolean checkParents(IdmTreeNode treeNode) {
		return treeNode.getParent() != null && (treeNode.getId() == treeNode.getParent().getId());
	}
}
