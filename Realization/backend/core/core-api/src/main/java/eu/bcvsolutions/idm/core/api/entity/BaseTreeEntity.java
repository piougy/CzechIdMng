package eu.bcvsolutions.idm.core.api.entity;

/**
 * Default interface class for entity with tree structure
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <E> extends from @AbstractEntity
 */
public interface BaseTreeEntity<E>  {
	
	/**
	 * Getter for parent of node
	 * 
	 * @return <E>
	 */
	public E getParent();
	
	/**
	 * Setter for parent
	 * 
	 * @param parent
	 */
	public void setParent(E parent);
	
}
