package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with IdmTreeNode
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Service
public interface IdmTreeNodeService extends 
		EventableDtoService<IdmTreeNodeDto, IdmTreeNodeFilter>, 
		ScriptEnabled, 
		AuthorizableService<IdmTreeNodeDto> {
	
	/**
	 * Method return all roots - @param treeType = null, or one root for treeType.
	 * 
	 * @param treeType Long
	 * @param pageable
	 * @return Page of roots
	 */
	Page<IdmTreeNodeDto> findRoots(UUID treeTypeId, Pageable pageable);
	
	/**
	 * Method return children by parent id
	 * 
	 * @param parent
	 * @return Page of children
	 */
	Page<IdmTreeNodeDto> findChildrenByParent(UUID parentId, Pageable pageable);
	
	/**
	 * Returns all node's parents
	 * 
	 * @param treeNodeId
	 * @param sort
	 * @return
	 */
	List<IdmTreeNodeDto> findAllParents(UUID treeNodeId, Sort sort);
	
	/**
	 * Rebuild (drop and create) all indexes for given treeType.
	 * 
	 * @param treeType
	 * @return long running task id
	 */
	UUID rebuildIndexes(UUID treeType);
	
	/**
	 * Returns default tree node or {@code null}, if no default tree node is defined
	 * 
	 * @return
	 */
	IdmTreeNodeDto getDefaultTreeNode();
}
