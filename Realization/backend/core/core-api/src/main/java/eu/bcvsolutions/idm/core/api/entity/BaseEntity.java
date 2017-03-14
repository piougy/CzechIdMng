package eu.bcvsolutions.idm.core.api.entity;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;

/**
 * Base entity
 * 
 * @author Radek Tomiška 
 *
 */
public interface BaseEntity extends Identifiable, Serializable {
	
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
