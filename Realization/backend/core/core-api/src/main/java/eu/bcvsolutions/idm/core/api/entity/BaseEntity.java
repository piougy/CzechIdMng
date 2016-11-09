package eu.bcvsolutions.idm.core.api.entity;

import java.io.Serializable;
import java.util.UUID;

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
	UUID getId();
	
	/**
	 * Set indentifier
	 *
	 * @param id
	 */
	void setId(UUID id);
}
