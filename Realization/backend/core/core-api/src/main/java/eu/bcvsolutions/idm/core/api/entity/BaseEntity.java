package eu.bcvsolutions.idm.core.api.entity;

import java.io.Serializable;

/**
 * Base entity
 * 
 * @author Radek Tomiška 
 *
 */
public interface BaseEntity extends Serializable {
	
	/**
	 * Returns indentifier
	 *
	 * @return
	 */
	Serializable getId();
	
	/**
	 * Set indentifier
	 *
	 * @param id
	 */
	void setId(Serializable id);
}
