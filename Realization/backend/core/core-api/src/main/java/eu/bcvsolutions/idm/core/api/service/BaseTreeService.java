package eu.bcvsolutions.idm.core.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.entity.BaseTreeEntity;

/**
 * Defautl interface for implementation tree service.
 * <p>
 * TODO: refactoring tree. Add connection to left/right
 * 
 * @param <E>
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
public interface BaseTreeService<E extends BaseTreeEntity<E>> {
	
	/**
	 * Method validate parents of node. By 
	 * Method is @Deprecated, in future will be implement by left right
	 * tree connection
	 * 
	 * @param treeNode
	 * @return boolean - true if treenode is not himself parent
	 */
	@Deprecated
	boolean validateTreeNodeParents(E treeNode);
	
	/**
	 * For tree structure is important to unique NAME for all children of parent.
	 * This method check names of tree nodes in list and check their unique.
	 * 
	 * @param treeNode
	 * @return boolean true if name isn't unique
	 */
	boolean validateUniqueName(List<E> treeNodes, E newSavedNode);
	
}
