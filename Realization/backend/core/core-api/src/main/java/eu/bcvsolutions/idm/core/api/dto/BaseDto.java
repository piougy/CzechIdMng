package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Any dto has identifier
 * 
 * Template for id type
 * 
 * @author Radek Tomiška 
 */
public interface BaseDto extends Identifiable, Serializable {
	
	String PROPERTY_ID = BaseEntity.PROPERTY_ID;
	
	/**
	 * Returns identifier
	 *
	 * @return
	 */
	Serializable getId();

	/**
	 * Set identifier
	 *
	 * @param id
	 */
	void setId(Serializable id);

}
