package eu.bcvsolutions.idm.core.model.repository;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.support.EntityLookupSupport;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Lookup for treeNode
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
public class IdmTreeNodeLookup extends EntityLookupSupport<IdmTreeNode> {
	
	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;
	
	@Override
	public Serializable getResourceIdentifier(IdmTreeNode treeNode) {
		return treeNode.getId();
	}

	@Override
	public Object lookupEntity(Serializable id) {
		return treeNodeRepository.findOne(Long.parseLong(id.toString()));
	}
	
	
}
