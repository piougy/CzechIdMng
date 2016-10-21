package eu.bcvsolutions.idm.core.api.service;

/**
 * Defautl interface for implementation tree service.
 * 
 * TODO: refactoring tree. Add connection to left/right
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <E>
 */
public interface BaseTreeService <E> {
	
	/**
	 * Method validate parents of node. By 
	 * Method is @Deprecated, in future will be implement by left right
	 * tree connection
	 * 
	 * @param treeNode
	 * @return boolean - true if treenode is not himself parent
	 */
	@Deprecated
	public boolean validateTreeNodeParents(E treeNode);
	
}
