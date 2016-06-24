package eu.bcvsolutions.idm.core.model.entity;

import java.io.Serializable;

/**
 * Base entity
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface BaseEntity extends Serializable {
	
	/**
	 * Returns indentifier
	 *
	 * @return
	 */
	Long getId();
	
	/**
	 * Set indentifier
	 *
	 * @param id
	 */
	void setId(Long id);
}
