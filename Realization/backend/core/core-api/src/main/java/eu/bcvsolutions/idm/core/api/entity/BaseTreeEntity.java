package eu.bcvsolutions.idm.core.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Default interface class for entity with tree structure
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <E>
 */
public interface BaseTreeEntity<E> {
	
	/**
	 * Getter for parent of node
	 * 
	 * @return <E>
	 */
	E getParent();
	
	/**
	 * Setter for parent
	 * 
	 * @param parent
	 */
	@JsonIgnore
	void setParent(E parent);
	
	/**
	 * Getter for name of node
	 * - name is unique for all children of parent.
	 * 
	 * @return String
	 */
	String getName();
}
